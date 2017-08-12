package me.au2001.lightcitizens.pathfinder;

import java.util.ArrayList;
import java.util.List;

public interface Node {

    public double distance(Node goal);
    public double distanceSquared(Node goal);

    public List<Node> getNeighbors();

    public String toString();
    public boolean equals(Object other);

    public static class Node1D implements Node {

        public double x;

        public Node1D(double x) {
            this.x = x;
        }

        public double distanceSquared(Node goal) {
            if (!(goal instanceof Node1D)) return -1;
            return Math.pow(distance(goal), 2);
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

        public String toString() {
            return getClass().getName() + "(x=" + x + ")";
        }

        public boolean equals(Object other) {
            if (!(other instanceof Node1D)) return false;
            return x == ((Node1D) other).x;
        }
    }

    public static class Node2D implements Node {

        public double x, y;

        public Node2D(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double distanceSquared(Node goal) {
            if (!(goal instanceof Node2D)) return -1;
            return Math.pow(this.x - ((Node2D) goal).x, 2) + Math.pow(this.y - ((Node2D) goal).y, 2);
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

        public String toString() {
            return getClass().getName() + "(x=" + x + ", y=" + y + ")";
        }

        public boolean equals(Object other) {
            if (!(other instanceof Node2D)) return false;
            return x == ((Node2D) other).x && y == ((Node3D) other).y;
        }
    }

    public static class Node3D implements Node {

        public double x, y, z;

        public Node3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double distanceSquared(Node goal) {
            if (!(goal instanceof Node3D)) return -1;
            return Math.pow(this.x - ((Node3D) goal).x, 2) + Math.pow(this.y - ((Node3D) goal).y, 2) + Math.pow(this.z - ((Node3D) goal).z, 2);
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

        public String toString() {
            return getClass().getName() + "(x=" + x + ", y=" + y + ", z=" + z + ")";
        }

        public boolean equals(Object other) {
            if (!(other instanceof Node3D)) return false;
            return x == ((Node3D) other).x && y == ((Node3D) other).y && z == ((Node3D) other).z;
        }
    }

}
