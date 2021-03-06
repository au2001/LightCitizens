package me.au2001.lightcitizens.pathfinder;

import me.au2001.lightcitizens.pathfinder.Node.Node3D;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class MCGoal implements Iterable<MCGoal.Step>, Iterator<MCGoal.Step>, Closeable {

    private List<MCGoal.Step> steps = null;
    private int currentIndex = -1;

    private World world;
    private Location start;
    private Location goal;
    private double distance;
    private double height;
    private int precision;
    private double maxJump;
    private double maxFall;
    private MCGraph graph;
    private PathFinder pathFinder;
    private Thread thread;

    public MCGoal(Location start, Location goal) {
        this(start, goal, 2, 0, 0, 0);
    }

    public MCGoal(Location start, Location goal, double distance) {
        this(start, goal, distance, 2, 0, 0, 0);
    }

    public MCGoal(Location start, Location goal, double height, double distance) {
        this(start, goal, height, distance, 0, 0, 0);
    }

    public MCGoal(Location start, Location goal, int precision) {
        this(start, goal, 2.0, precision, 0, 0);
    }

    public MCGoal(Location start, Location goal, double height, int precision) {
        this(start, goal, height, precision, 0, 0);
    }

    public MCGoal(Location start, Location goal, double height, double maxJump, double maxFall) {
        this(start, goal, height, 0, maxJump, maxFall);
    }

    public MCGoal(Location start, Location goal, int precision, double maxJump, double maxFall) {
        this(start, goal, 0, 2.0, precision, maxJump, maxFall);
    }

    public MCGoal(Location start, Location goal, double distance, double height, double maxJump, double maxFall) {
        this(start, goal, distance, height, 0, maxJump, maxFall);
    }

    public MCGoal(Location start, Location goal, double distance, int precision, double maxJump, double maxFall) {
        this(start, goal, distance, 2.0, precision, maxJump, maxFall);
    }

    public MCGoal(Location start, Location goal, double distance, double height, int precision, double maxJump, double maxFall) {
        if (start.getWorld() == null && goal.getWorld() != null) {
            start = start.clone();
            start.setWorld(goal.getWorld());
        } else if (goal.getWorld() == null && start.getWorld() != null) {
            goal = goal.clone();
            goal.setWorld(start.getWorld());
        } else if (!start.getWorld().equals(goal.getWorld()))
            throw new IllegalArgumentException("Start and goal locations must be in the same world!");

        this.world = start.getWorld();
        this.start = start.clone();
        this.goal = goal.clone();
        this.distance = distance;
        this.height = height;
        this.precision = precision > 0? precision : 20;
        this.maxJump = maxJump;
        this.maxFall = maxFall;
        this.thread = Thread.currentThread();

        Node3D startNode = new Node3D(start.getBlockX(), start.getBlockY(), start.getBlockZ());
        Node3D goalNode = new Node3D(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ());

        Location center = new Location(world, (startNode.x + goalNode.x)/2, (startNode.y + goalNode.y)/2, (startNode.z + goalNode.z)/2);
        graph = new MCGraph(center, startNode.distance(goalNode) * 1.5, height, maxJump, maxFall);
        pathFinder = new PathFinder(graph, startNode, goalNode);

        calculate();
    }

    private void calculate() {
        steps = null;
        pathFinder.findAsynchronously(new Runnable() {
            public void run() {
                List<MCGoal.Step> tmp = new ArrayList<MCGoal.Step>();

                Queue<Node> path = pathFinder.getPath();
                if (path != null) {
                    Node from = path.poll();
                    while (!path.isEmpty()) {
                        Node to = from;
                        from = path.poll();
                        if (from instanceof Node.Node3D && to instanceof Node.Node3D)
                            tmp.add(new MCGoal.Step(world, (Node.Node3D) from, (Node.Node3D) to, precision));
                    }
                }

                steps = tmp;
                synchronized(thread) {
                    thread.notifyAll();
                }
            }
        }, distance);
    }

    public void close() {
        graph.close();
    }

    public double getHeight() {
        return distance;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getAcceptableDistance() {
        return distance;
    }

    public void setAcceptableDistance(double distance) {
        if (distance != this.distance) {
            this.distance = distance;
            calculate();
        }
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        if (precision != this.precision) {
            this.precision = precision;
            calculate();
        }
    }

    public double getMaximumJump() {
        return maxJump;
    }

    public void setMaximumJump(double maxJump) {
        if (maxJump != this.maxJump) {
            this.maxJump = maxJump;
            calculate();
        }
    }

    public double getMaximumFall() {
        return maxFall;
    }

    public void setMaximumFall(double maxFall) {
        if (maxFall != this.maxFall) {
            this.maxFall = maxFall;
            calculate();
        }
    }

    public boolean isReady() {
        return steps != null;
    }

    public Iterator<MCGoal.Step> iterator() {
        return this;
    }

    public boolean hasNext() {
        while (steps == null) {
            synchronized(thread) {
                try {
                    thread.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return currentIndex < steps.size() - 1;
    }

    public MCGoal.Step next() {
        return hasNext()? steps.get(++currentIndex) : null;
    }

    private static List<Double> jump(double from, double to, double speed) {
        List<Double> heights = new ArrayList<Double>();

        double min = Math.min(from, to), max = Math.max(from, to);
        double height = max - min + Math.min(max - min, 1) * 0.2; // Jump 20% higher (and 0.2 blocks higher max.)

        double y = from;
        while (y < min + height) { // Start from beggining and go up until we reach the highest point (about end + 20%)
            heights.add(y);
            y = Math.min(y + speed, min + height);
        }

        y = min + height;
        while (y > to) { // Start from highest point (about end + 20%) and go down until we reach the end
            heights.add(y);
            y = Math.max(y - speed, to);
        }

        heights.add(to);
        return heights;
    }

    public static class Step implements Iterable<Location>, Iterator<Location> {

        private List<Location> locations = new ArrayList<Location>();
        private int currentIndex = -1;
        private World world;
        private Node.Node3D from;
        private Node.Node3D to;
        private int precision;
        private Vector direction;

        public Step(World world, Node.Node3D from, Node.Node3D to) {
            this(world, from, to, 0);
        }

        public Step(World world, Node.Node3D from, Node.Node3D to, int precision) {
            this.world = world;
            this.from = from;
            this.to = to;
            this.precision = precision > 0? precision : 25;
            this.direction = new Vector(to.x - from.x, to.y - from.y, to.z - from.z);

            calculate();
        }

        private void calculate() {
            Vector direction = this.direction.clone();
            Location location = new Location(world, from.x + 0.5, from.y, from.z + 0.5);

            if (from.y != to.y && (from.x != to.x || from.z != to.z)) {
                List<Double> heights = jump(from.y, to.y, 1.0 / (double) precision);
                direction.setY(0).multiply(1.0 / (double) heights.size());
                for (double y : heights) {
                    location.add(direction);
                    location.setY(y);
                    location.setDirection(direction);
                    locations.add(location.clone());
                }
            } else {
                direction.multiply(1.0 / (double) precision);
                for (int i = 0; i < precision; i++) {
                    location.add(direction);
                    location.setDirection(direction);
                    locations.add(location.clone());
                }
            }
        }

        public int getPrecision() {
            return precision;
        }

        public void setPrecision(int precision) {
            if (precision != this.precision) {
                this.precision = precision;
                calculate();
            }
        }

        public Iterator<Location> iterator() {
            return this;
        }

        public boolean hasNext() {
            return currentIndex < locations.size() - 1;
        }

        public Location next() {
            return hasNext()? locations.get(++currentIndex) : null;
        }
    }

}
