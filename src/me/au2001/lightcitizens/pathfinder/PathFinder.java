package me.au2001.lightcitizens.pathfinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathFinder {

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

    private Graph graph;
    private Node start, goal, last;
    private Map<Node, Node> cameFrom;
    private Map<Node, Double> gScore;
    private Map<Node, Double> fScore;
    private Thread thread = null;

    public PathFinder(Graph graph, Node start, Node goal) {
        this.graph = graph;
        this.start = start;
        this.goal = goal;

        reset();
    }

    private void reset() {
        this.cameFrom = new HashMap<Node, Node>();

        this.gScore = new HashMap<Node, Double>();
        this.gScore.put(start, 0.0);

        this.fScore = new HashMap<Node, Double>();
        this.fScore.put(start, start.distanceSquared(goal));
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

    public List<Node> find() {
        return find(0.0);
    }

    public List<Node> find(double distance) {
        if (start == null || goal == null) return null;

        List<Node> closedSet = new ArrayList<Node>();
        List<Node> openSet = new ArrayList<Node>();
        openSet.add(start);

        double distanceSquared = distance * distance;

        while (!openSet.isEmpty()) {
            Node current = openSet.remove(0);

            if (current.distanceSquared(goal) <= distanceSquared) {
                last = current;
                break;
            } else if (closedSet.contains(current)) continue;

            closedSet.add(current);

            for (Node neighbor : graph.getNeighbors(current)) {
                if (closedSet.contains(neighbor)) continue;
                if (!openSet.contains(neighbor)) openSet.add(neighbor);

                double tentative_gScore = gScore.get(current) + current.distance(neighbor);
                if (gScore.containsKey(neighbor) && tentative_gScore >= gScore.get(neighbor)) continue;

                cameFrom.put(neighbor, current);
                gScore.put(neighbor, tentative_gScore);
                fScore.put(neighbor, gScore.get(neighbor) + neighbor.distance(goal));
            }
        }

        return getPath();
    }

    public List<Node> getPath() {
        if (start == null || last == null) return null;
        if (!cameFrom.containsKey(last)) return null;

        List<Node> path = new ArrayList<Node>();
        for (Node current = last; cameFrom.get(current) != null; current = cameFrom.get(current))
            path.add(0, current);
        return path;
    }

}
