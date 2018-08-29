package me.au2001.lightcitizens.pathfinder;

import me.au2001.lightcitizens.pathfinder.Node.Node2D;
import me.au2001.lightcitizens.pathfinder.Node.Node3D;
import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;

import java.io.Closeable;
import java.util.HashMap;

public class MCSnapshot extends BlockSource implements Closeable {

	protected static HashMap<World, HashMap<Node2D, MCChunkCache>> cache = new HashMap<World, HashMap<Node2D, MCChunkCache>>();

	protected World world;
	protected Node3D center;
	protected double distance;
	private boolean init = false;

	public MCSnapshot(Location center, double distance) {
		this.world = center.getWorld();
		this.center = new Node3D(center.getX(), center.getY(), center.getZ());
		this.distance = distance;

		this.update();
	}

	public void update() {
		int minX = (int) Math.floor((center.x - distance) / 16), maxX = (int) Math.floor((center.x + distance) / 16);
		int minZ = (int) Math.floor((center.z - distance) / 16), maxZ = (int) Math.floor((center.z + distance) / 16);

		HashMap<Node2D, MCChunkCache> worldCache = cache.get(world);
		if (worldCache == null) worldCache = new HashMap<Node2D, MCChunkCache>();
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				Node2D node = new Node2D(x, z);
				Chunk chunk = world.getChunkAt(x, z);
				ChunkSnapshot snapshot = chunk != null? chunk.getChunkSnapshot() : null;

				MCChunkCache chunkCache = worldCache.get(node);
				if (chunkCache == null) {
					chunkCache = new MCChunkCache(snapshot, 1);
					worldCache.put(node, chunkCache);
				} else {
					chunkCache.snapshot = snapshot;
					if (!init) chunkCache.hitCount++;
				}
			}
		}
		cache.put(world, worldCache);
		init = true;
	}

	public void close() {
		if (!init) return;
		int minX = (int) Math.floor((center.x - distance) / 16), maxX = (int) Math.floor((center.x + distance) / 16);
		int minZ = (int) Math.floor((center.z - distance) / 16), maxZ = (int) Math.floor((center.z + distance) / 16);

		HashMap<Node2D, MCChunkCache> worldCache = cache.get(world);
		if (worldCache == null) return;
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				MCChunkCache chunkCache = worldCache.get(new Node2D(x, z));
				if (--chunkCache.hitCount <= 0) worldCache.remove(chunkCache);
			}
		}
		init = false;
	}

	public World getWorld() {
		return world;
	}

	public boolean isInBound(Node3D node) {
		if (distance < 0) return true;
		double distX = Math.pow(node.x - center.x, 2);
		double distZ = Math.pow(node.z - center.z, 2);
		return distX + distZ <= distance * distance;
	}

	public Material getMaterialAt(int x, int y, int z) {
		MaterialData data = get(new Node3D(x, y, z));
		return data != null? data.getItemType() : null;
	}

	public MaterialData get(Node3D node) {
		int cx = (int) Math.floor(node.x / 16), cz = (int) Math.floor(node.z / 16);
		Node2D chunk = new Node2D(cx, cz);
		HashMap<Node2D, MCChunkCache> worldCache = cache.get(world);
		MCChunkCache chunkCache = worldCache.get(chunk);

		if (chunkCache == null || chunkCache.snapshot == null) return null;

		int x = (int) node.x % 16, z = (int) node.z % 16;

		while (x < 0) x += 16;
		while (x >= 16) x -= 16;
		while (z < 0) z += 16;
		while (z >= 16) z -= 16;

		int type = chunkCache.snapshot.getBlockTypeId(x, (int) node.y, z);
		byte data = (byte) chunkCache.snapshot.getBlockData(x, (int) node.y, z);

		return new MaterialData(type, data);
	}

	private static class MCChunkCache {
		private ChunkSnapshot snapshot;
		private int hitCount;

		private MCChunkCache(ChunkSnapshot snapshot, int hitCount) {
			this.snapshot = snapshot;
			this.hitCount = hitCount;
		}
	}

	public static class MCLiveSnapshot extends MCSnapshot {

		public MCLiveSnapshot(World world) {
			super(world.getSpawnLocation(), -1);
		}

		public void update() {}
		public void close() {}

		public boolean isInBound(Node3D node) {
			return true;
		}

		public MaterialData get(Node3D node) {
			Block block = world.getBlockAt((int) node.x, (int) node.y, (int) node.z);
			return block != null && block.getState() != null? block.getState().getData() : null;
		}

	}

}
