package me.au2001.lightcitizens.pathfinder;

import java.util.ArrayList;
import java.util.List;

public interface Node {

    public double distance(Node goal);
    public int distanceSquared(Node goal);

    public List<Node> getNeighbors();

    public static class Node1D implements Node {

        public int x;

        public Node1D(int x) {
            this.x = x;
        }

        public int distanceSquared(Node goal) {
            if (!(goal instanceof Node1D)) return -1;
            return (int) Math.pow(distanceSquared(goal), 2);
        }

        public double distance(Node goal) {
            if (!(goal instanceof Node1D)) return -1;
            return Math.abs(this.x - ((Node1D) goal).x);
        }

        public List<Node> getNeighbors() {
            List<Node> neighbors = new ArrayList<Node>();
            neighbors.add(new Node1D(x - 1));
            neighbors.add(new Node1D(x + 1));
            return neighbors;
        }
    }

    public static class Node2D implements Node {

        public int x, y;

        public Node2D(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int distanceSquared(Node goal) {
            if (!(goal instanceof Node2D)) return -1;
            return (int) (Math.pow(this.x - ((Node2D) goal).x, 2) + Math.pow(this.y - ((Node2D) goal).y, 2));
        }

        public double distance(Node goal) {
            if (!(goal instanceof Node2D)) return -1;
            return Math.sqrt(distanceSquared(goal));
        }

        public List<Node> getNeighbors() {
            List<Node> neighbors = new ArrayList<Node>();
            neighbors.add(new Node2D(x - 1, y));
            neighbors.add(new Node2D(x + 1, y));
            neighbors.add(new Node2D(x, y - 1));
            neighbors.add(new Node2D(x, y + 1));
            return neighbors;
        }
    }

    public static class Node3D implements Node {

        public int x, y, z;

        public Node3D(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int distanceSquared(Node goal) {
            if (!(goal instanceof Node3D)) return -1;
            return (int) (Math.pow(this.x - ((Node3D) goal).x, 2) + Math.pow(this.y - ((Node3D) goal).y, 2) + Math.pow(this.z - ((Node3D) goal).z, 2));
        }

        public double distance(Node goal) {
            if (!(goal instanceof Node3D)) return -1;
            return Math.sqrt(distanceSquared(goal));
        }

        public List<Node> getNeighbors() {
            List<Node> neighbors = new ArrayList<Node>();
            neighbors.add(new Node3D(x - 1, y, z));
            neighbors.add(new Node3D(x + 1, y, z));
            neighbors.add(new Node3D(x, y - 1, z));
            neighbors.add(new Node3D(x, y + 1, z));
            neighbors.add(new Node3D(x, y, z - 1));
            neighbors.add(new Node3D(x, y, z + 1));
            return neighbors;
        }
    }

}
