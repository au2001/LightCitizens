package me.au2001.lightcitizens.pathfinder;

import java.util.HashMap;
import java.util.Map;

public interface Node {

    public double distance(Node goal);
    public double distanceSquared(Node goal);

    public Map<Node, Double> getNeighbors();

    public String toString();
    public boolean equals(Object other);
    public int hashCode();

    public static class Node1D implements Node {

        public double x;
        private int hash = 0;

        public Node1D(double x) {
            this.x = x;
        }

        public double distanceSquared(Node goal) {
            if (goal == null || !(goal instanceof Node1D)) return -1;
            return Math.pow(distance(goal), 2);
        }

        public double distance(Node goal) {
            if (goal == null || !(goal instanceof Node1D)) return -1;
            return Math.abs(this.x - ((Node1D) goal).x);
        }

        public Map<Node, Double> getNeighbors() {
            Map<Node, Double> neighbors = new HashMap<Node, Double>();
            neighbors.put(new Node1D(x - 1), 1.0);
            neighbors.put(new Node1D(x + 1), 1.0);
            return neighbors;
        }

        public String toString() {
            return getClass().getSimpleName() + "(x=" + x + ")";
        }

        public boolean equals(Object other) {
            if (other == null || !(other instanceof Node1D)) return false;
            return x == ((Node1D) other).x;
        }

        public int hashCode() {
            if (hash == 0) {
                long bits = 7;
                bits = 31 * bits + Double.doubleToLongBits(x);
                hash = (int) (bits ^ (bits >> 32));
            }
            return hash;
        }
    }

    public static class Node2D implements Node {

        public double x, y;
        private int hash = 0;

        public Node2D(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double distanceSquared(Node goal) {
            if (goal == null || !(goal instanceof Node2D)) return -1;
            return Math.pow(this.x - ((Node2D) goal).x, 2) + Math.pow(this.y - ((Node2D) goal).y, 2);
        }

        public double distance(Node goal) {
            if (goal == null || !(goal instanceof Node2D)) return -1;
            return Math.sqrt(distanceSquared(goal));
        }

        public Map<Node, Double> getNeighbors() {
            Map<Node, Double> neighbors = new HashMap<Node, Double>();
            neighbors.put(new Node2D(x - 1, y), 1.0);
            neighbors.put(new Node2D(x + 1, y), 1.0);
            neighbors.put(new Node2D(x, y - 1), 1.0);
            neighbors.put(new Node2D(x, y + 1), 1.0);
            return neighbors;
        }

        public String toString() {
            return getClass().getSimpleName() + "(x=" + x + ", y=" + y + ")";
        }

        public boolean equals(Object other) {
            if (other == null || !(other instanceof Node2D)) return false;
            return x == ((Node2D) other).x && y == ((Node2D) other).y;
        }

        public int hashCode() {
            if (hash == 0) {
                long bits = 7;
                bits = 31 * bits + Double.doubleToLongBits(x);
                bits = 31 * bits + Double.doubleToLongBits(y);
                hash = (int) (bits ^ (bits >> 32));
            }
            return hash;
        }
    }

    public static class Node3D implements Node {

        public double x, y, z;
        private int hash = 0;

        public Node3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double distanceSquared(Node goal) {
            if (goal == null || !(goal instanceof Node3D)) return -1;
            return Math.pow(this.x - ((Node3D) goal).x, 2) + Math.pow(this.y - ((Node3D) goal).y, 2) + Math.pow(this.z - ((Node3D) goal).z, 2);
        }

        public double distance(Node goal) {
            if (goal == null || !(goal instanceof Node3D)) return -1;
            return Math.sqrt(distanceSquared(goal));
        }

        public Map<Node, Double> getNeighbors() {
            Map<Node, Double> neighbors = new HashMap<Node, Double>();
            neighbors.put(new Node3D(x - 1, y, z), 1.0);
            neighbors.put(new Node3D(x + 1, y, z), 1.0);
            neighbors.put(new Node3D(x, y - 1, z), 1.0);
            neighbors.put(new Node3D(x, y + 1, z), 1.0);
            neighbors.put(new Node3D(x, y, z - 1), 1.0);
            neighbors.put(new Node3D(x, y, z + 1), 1.0);
            return neighbors;
        }

        public String toString() {
            return getClass().getSimpleName() + "(x=" + x + ", y=" + y + ", z=" + z + ")";
        }

        public boolean equals(Object other) {
            if (other == null || !(other instanceof Node3D)) return false;
            return x == ((Node3D) other).x && y == ((Node3D) other).y && z == ((Node3D) other).z;
        }

        public int hashCode() {
            if (hash == 0) {
                long bits = 7;
                bits = 31 * bits + Double.doubleToLongBits(x);
                bits = 31 * bits + Double.doubleToLongBits(y);
                bits = 31 * bits + Double.doubleToLongBits(z);
                hash = (int) (bits ^ (bits >> 32));
            }
            return hash;
        }
    }

}
