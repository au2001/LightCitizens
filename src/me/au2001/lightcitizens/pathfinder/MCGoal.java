package me.au2001.lightcitizens.pathfinder;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MCGoal implements Iterable<MCGoal.Step>, Iterator<MCGoal.Step> {

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
    private Graph.MCGraph graph;
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
        if (!start.getWorld().equals(goal.getWorld()))
            throw new IllegalArgumentException("Start and goal locations must be in the same world!");

        this.world = start.getWorld();
        this.start = start;
        this.goal = goal;
        this.distance = distance;
        this.height = height;
        this.precision = precision > 0? precision : 20;
        this.maxJump = maxJump;
        this.maxFall = maxFall;
        this.thread = Thread.currentThread();

        Location center = new Location(world, (start.getX() + goal.getX())/2, (start.getY() + goal.getY())/2, (start.getZ() + goal.getZ())/2);
        graph = new Graph.MCGraph(center, (int) (start.distanceSquared(goal) * 1.5 * 1.5), height, maxJump, maxFall);
        pathFinder = new PathFinder(graph, graph.getNode(start), graph.getNode(goal));

        calculate();
    }

    private void calculate() {
        steps = null;
        pathFinder.findAsynchronously(new Runnable() {
            public void run() {
                List<MCGoal.Step> tmp = new ArrayList<MCGoal.Step>();

                List<Node> path = pathFinder.getPath();
                if (path != null) {
                    for (int i = 1; i < path.size(); i++) {
                        Node from = path.get(i - 1), to = path.get(i);
                        if (from instanceof Node.Node3D && to instanceof Node.Node3D)
                            tmp.add(new MCGoal.Step((Node.Node3D) from, (Node.Node3D) to, precision));
                    }
                }

                steps = tmp;
                synchronized(thread) {
                    thread.notifyAll();
                }
            }
        }, distance);
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

    public class Step implements Iterable<Location>, Iterator<Location> {

        private List<Location> locations = new ArrayList<Location>();
        private int currentIndex = -1;
        private Node.Node3D from;
        private Node.Node3D to;
        private int precision;
        private Vector direction;

        public Step(Node.Node3D from, Node.Node3D to) {
            this(from, to, 0);
        }

        public Step(Node.Node3D from, Node.Node3D to, int precision) {
            this.from = from;
            this.to = to;
            this.precision = precision > 0? precision : 25;
            this.direction = new Vector(to.x - from.x, to.y - from.y, to.z - from.z);

            calculate();
        }

        private void calculate() {
            Vector direction = this.direction.clone();
            Location location = graph.getBlock(from).getLocation().add(0.5, 0, 0.5);

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
