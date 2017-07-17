package me.au2001.lightcitizens.pathfinder;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.material.Openable;

import java.util.ArrayList;
import java.util.List;

public abstract class Graph {

    public abstract boolean isValid(Node node);

    public List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<Node>();
        for (Node neighbor : node.getNeighbors())
            if (isValid(neighbor)) neighbors.add(neighbor);
        return neighbors;
    }

    public static class MCGraph extends Graph {

        private World world;
        private int maxJump, maxFall;

        public MCGraph(World world) {
            this(world, 1, 3);
        }

        public MCGraph(World world, int maxJump, int maxFall) {
            this.world = world;
            this.maxJump = maxJump;
            this.maxFall = maxFall;
        }

        private Block getBlock(Node node) {
            if (!(node instanceof Node.Node3D)) return null;
            return world.getBlockAt(((Node.Node3D) node).x, ((Node.Node3D) node).y, ((Node.Node3D) node).z);
        }

        private boolean isBlocking(Block block) {
            if (block == null) return false;

            if (block.getType().isOccluding()) return true;

            if (block.isEmpty() || block.isLiquid() || block.getType().isTransparent()) return false;

            if (block.getState() instanceof Openable) return ((Openable) block.getState()).isOpen();

            // TODO: Slabs
            // TODO: Stairs
            // TODO: Fences

            return true;
        }

        private boolean isWalkable(Block block) {
            // TODO: Flying?

            if (block == null) return false;

            if (block.getType().isOccluding()) return true;

            if (block.isEmpty() || block.isLiquid() || block.getType().isTransparent()) return false;

            // TODO

            return true;
        }

        public boolean isValid(Node node) {
            return isValid(getBlock(node));
        }

        public boolean isValid(Block block) {
            if (block == null) return false;

            Block above = block.getRelative(0, 1, 0);
            Block below = block.getRelative(0, -1, 0);

            return !isBlocking(block) && !isBlocking(above) && isWalkable(below);
        }

        public List<Node> getNeighbors(Node node) {
            List<Node> neighbors = new ArrayList<Node>();
            neighbors.add(new Node.Node3D(((Node.Node3D) node).x - 1, ((Node.Node3D) node).y, ((Node.Node3D) node).z));
            neighbors.add(new Node.Node3D(((Node.Node3D) node).x + 1, ((Node.Node3D) node).y, ((Node.Node3D) node).z));
            neighbors.add(new Node.Node3D(((Node.Node3D) node).x, ((Node.Node3D) node).y, ((Node.Node3D) node).z - 1));
            neighbors.add(new Node.Node3D(((Node.Node3D) node).x, ((Node.Node3D) node).y, ((Node.Node3D) node).z + 1));
            neighbors.add(new Node.Node3D(((Node.Node3D) node).x, ((Node.Node3D) node).y - 1, ((Node.Node3D) node).z));
            neighbors.add(new Node.Node3D(((Node.Node3D) node).x, ((Node.Node3D) node).y + 1, ((Node.Node3D) node).z));

            for (Node neighbor : neighbors) {
                Block block = getBlock(neighbor);
                Block above = block.getRelative(0, 1, 0);
                Block below = block.getRelative(0, -1, 0);

                if (!isBlocking(block) && !isBlocking(above) && isWalkable(below)) continue;
                else neighbors.remove(neighbor);

                if (isBlocking(block)) {
                    for (int jump = 1; jump <= maxJump; jump++) {
                        if (isValid(block.getRelative(0, jump, 0))) {
                            neighbors.add(new Node.Node3D(block.getX(), block.getY() + jump, block.getZ()));
                            break;
                        }
                    }
                } else if (!isBlocking(above)) {
                    for (int fall = 1; fall <= maxFall; fall++) {
                        if (isValid(block.getRelative(0, -fall, 0))) {
                            neighbors.add(new Node.Node3D(block.getX(), block.getY() - fall, block.getZ()));
                            break;
                        }
                    }
                }
            }

            return neighbors;
        }
    }

}
