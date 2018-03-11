package me.au2001.lightcitizens.pathfinder;

import java.util.*;
import java.util.Map.Entry;

/*
 *
 * function A*(start, goal)
 *     // The set of nodes already evaluated
 *     closedSet := {}
 *
 *     // The set of currently discovered nodes that are not evaluated yet.
 *     // Initially, only the start node is known.
 *     openSet := {start}
 *
 *     // For each node, which node it can most efficiently be reached from.
 *     // If a node can be reached from many nodes, cameFrom will eventually contain the
 *     // most efficient previous step.
 *     cameFrom := the empty map
 *
 *     // For each node, the cost of getting from the start node to that node.
 *     gScore := map with default value of Infinity
 *
 *     // The cost of going from start to start is zero.
 *     gScore[start] := 0
 *
 *     // For each node, the total cost of getting from the start node to the goal
 *     // by passing by that node. That value is partly known, partly heuristic.
 *     fScore := map with default value of Infinity
 *
 *     // For the first node, that value is completely heuristic.
 *     fScore[start] := heuristic_cost_estimate(start, goal)
 *
 *     while openSet is not empty
 *         current := the node in openSet having the lowest fScore[] value
 *         if current = goal
 *             return reconstruct_path(cameFrom, current)
 *
 *         openSet.Remove(current)
 *         closedSet.Add(current)
 *
 *         for each neighbor of current
 *             if neighbor in closedSet
 *                 continue		// Ignore the neighbor which is already evaluated.
 *
 *             if neighbor not in openSet	// Discover a new node
 *                 openSet.Add(neighbor)
 *
 *             // The distance from start to a neighbor
 *             tentative_gScore := gScore[current] + dist_between(current, neighbor)
 *             if tentative_gScore >= gScore[neighbor]
 *                 continue		// This is not a better path.
 *
 *             // This path is the best until now. Record it!
 *             cameFrom[neighbor] := current
 *             gScore[neighbor] := tentative_gScore
 *             fScore[neighbor] := gScore[neighbor] + heuristic_cost_estimate(neighbor, goal)
 *
 *     return failure
 *
 * function reconstruct_path(cameFrom, current)
 *     total_path := [current]
 *     while current in cameFrom.Keys:
 *         current := cameFrom[current]
 *         total_path.append(current)
 *     return total_path
 *
 */

public class PathFinder {

    private Graph graph;
    private Node start, goal;
    private PathNode last;
    private Thread thread = null;
    private Map<Node, PathNode> pathNodes;

    public PathFinder(Graph graph, Node start, Node goal) {
        this.graph = graph;
        this.start = start;
        this.goal = goal;

        this.pathNodes = new HashMap<Node, PathNode>();
    }

    public void findAsynchronously(Runnable completion) {
        findAsynchronously(completion, 0.0);
    }

    public void findAsynchronously(Runnable completion, double distance) {
        cancelFind();
        thread = new Thread(new Runnable() {
            public void run() {
                find(distance);
                completion.run();
            }
        });
        thread.start();
    }

    public void cancelFind() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public Queue<Node> find() {
        return find(0.0);
    }

    public Queue<Node> find(double distance) {
        if (start == null || goal == null) return null;

        Set<PathNode> closedSet = new HashSet<PathNode>();
        PriorityQueue<PathNode> openSet = new PriorityQueue<PathNode>(new Comparator<PathNode>() {
            public int compare(PathNode node1, PathNode node2) {
                // return Double.compare(node1.g + node1.dist, node2.g + node2.dist);
                return Double.compare(node1.g, node2.g);
            }
        });
        PathNode startNode = getPathNode(start);
        openSet.add(startNode);

        double distanceSquared = distance * distance;

        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();

            if (current.dist <= distanceSquared) {
                last = current;
                break;
            }

            closedSet.add(current);

            if (current.neighbors == null) {
                current.neighbors = new HashMap<PathNode, Double>();
                for (Entry<Node, Double> entry : graph.getNeighbors(current.node).entrySet())
                    current.neighbors.put(getPathNode(entry.getKey()), entry.getValue());
            }

            for (Entry<PathNode, Double> entry : current.neighbors.entrySet()) {
                PathNode neighbor = entry.getKey();
                if (closedSet.contains(neighbor)) continue;

                double tentativeG = current.g + entry.getValue();
                if (tentativeG >= neighbor.g) {
                    openSet.add(neighbor);
                    continue;
                }

                neighbor.from = current;
                neighbor.g = tentativeG;

                openSet.add(neighbor);
            }
        }

        return getPath();
    }

    public Queue<Node> getPath() {
        if (start == null || last == null) return null;
        if (last.node.equals(start)) return new ArrayDeque<Node>();
        if (last.from == null) return null;

        Deque<Node> path = new ArrayDeque<Node>();
        for (PathNode current = last; current != null; current = current.from)
            path.addFirst(current.node);

        if (!path.peek().equals(start)) return null;
        return path;
    }

    private PathNode getPathNode(Node node) {
        PathNode pathNode = pathNodes.get(node);
        if (pathNode != null) return pathNode;
        return new PathNode(node);
    }

    private class PathNode {

        private Node node;
        private double dist, g = Double.POSITIVE_INFINITY;
        private Map<PathNode, Double> neighbors = null;
        private PathNode from = null;

        private PathNode(Node node) {
            this.node = node;

            this.dist = node.distanceSquared(goal);
            if (node.equals(start)) this.g = 0;

            pathNodes.put(node, this);
        }

        public boolean equals(Object other) {
            if (other == null || !(other instanceof PathNode)) return false;
            return node.equals(((PathNode) other).node);
        }

        public int hashCode() {
            return node.hashCode();
        }

    }

}
