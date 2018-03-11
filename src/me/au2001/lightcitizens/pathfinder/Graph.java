package me.au2001.lightcitizens.pathfinder;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class Graph {

    public abstract boolean canMove(Node from, Node to);

    public Map<Node, Double> getNeighbors(Node node) {
        Map<Node, Double> neighbors = new HashMap<Node, Double>();
        for (Entry<Node, Double> entry : node.getNeighbors().entrySet())
            if (canMove(node, entry.getKey())) neighbors.put(entry.getKey(), entry.getValue());
        return neighbors;
    }

}
