package me.au2001.lightcitizens.managers;

import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.pathfinder.MCGraph;
import me.au2001.lightcitizens.pathfinder.MCSnapshot;
import me.au2001.lightcitizens.pathfinder.MCSnapshot.MCLiveSnapshot;
import me.au2001.lightcitizens.pathfinder.Node.Node3D;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public class WalkTowardsEntityManager extends Manager {

	private final static double DECELERATION_RATE = 0.02D;
	private final static double GRAVITY_CONSTANT = 0.25D;
	private final static double MAX_GRAVITY = 3.0D;
	private final static double SPEED_MODIFIER = 0.1D;
	private final static double GRAVITY_MODIFIER = 0.3D;
	private final static double HEIGHT = 1.85D;

	protected double speed = 1;
	private double distance = 0;
	private double velocity = 0;
	private Entity target = null;

	public WalkTowardsEntityManager(FakeEntity entity) {
		super(entity);
	}

	public void onManagerRemoved() {
		walkTowardsEntity(null);
	}

	public void syncTick() {
		if (target == null) return;
		if (target.isDead() || !target.isValid() || target.getWorld() == null || !target.getWorld().equals(entity.getLocation().getWorld())) {
			target = null;
			return;
		}

		Location target = this.target.getLocation();
		Location location = entity.getLocation();
		Node3D node = new Node3D(location.getX(), location.getY(), location.getZ());
//		if (!isValidLocation(snapshot, location)) return; // Blocked in a block

		MCSnapshot snapshot = new MCLiveSnapshot(location.getWorld());

		boolean arrived = Math.pow(location.getY() - target.getY(), 2) + Math.pow(location.getZ() - target.getZ(), 2) <= distance * distance; // Close enough

		Vector direction = target.clone().subtract(location).toVector().setY(0).normalize();

		if (!arrived) {
			boolean blocked = false;
			location.add(direction.clone().multiply(speed * SPEED_MODIFIER));
			node = new Node3D(location.getX(), location.getY(), location.getZ());
			if (!isValidLocation(snapshot, node)) { // Blocked by a wall
				double upperHeight = MCGraph.getUpperHeight(snapshot, node, null, 2);
				if (upperHeight <= location.getY() + 0.5) {
					// Step up
					location.setY(upperHeight);
					node.y = location.getY();
				} else {
					location.subtract(direction.clone().multiply(speed * SPEED_MODIFIER));
					node = new Node3D(location.getX(), location.getY(), location.getZ());
					blocked = true;
				}
			} else if (MCGraph.getUpperHeight(snapshot, node, null, 4) < location.getY() - 3) { // Going to fall
				location.subtract(direction.clone().multiply(speed * SPEED_MODIFIER));
				node = new Node3D(location.getX(), location.getY(), location.getZ());
				blocked = true;
			}

			if (blocked) {
				double step = Math.PI / 4;
				for (int i = 1; i < 2 * Math.PI / step; i++) {
					double angle = step * (i % 2 == 0? i / 2 : (i - 1) / 2);
					double cos = Math.cos(angle), sin = Math.sin(angle);
					double x = direction.getX(), z = direction.getZ();
					Vector rotated = new Vector(x * cos - z * sin, 0, x * sin + z * cos).normalize();
					Location newLocation = location.clone().add(rotated.clone().multiply(speed * SPEED_MODIFIER));
					Node3D newNode = new Node3D(newLocation.getX(), newLocation.getY(), newLocation.getZ());
					if (isValidLocation(snapshot, newNode)) {
						direction = rotated;
						location = newLocation;
						node = newNode;
						break;
					}
				}
			}
		}

		velocity = (velocity - GRAVITY_CONSTANT) * (1 - DECELERATION_RATE); // Apply gravity and deceleration
		if (velocity < -MAX_GRAVITY) velocity = -MAX_GRAVITY;

		boolean onground = false;
		location.add(0, velocity * GRAVITY_MODIFIER, 0);
		node = new Node3D(location.getX(), location.getY(), location.getZ());
		if (!isValidLocation(snapshot, node)) { // Blocked by ceiling/floor
			if (velocity <= 0) {
				location.setY(MCGraph.getUpperHeight(snapshot, node, null, 2));
				node.y = location.getY();
				onground = true;
			} else {
				location.setY(MCGraph.getLowerHeight(snapshot, new Node3D(node.x, node.y + 2, node.z), null, 2) - HEIGHT);
				node.y = location.getY();
			}
			velocity = 0;
		}

		if (!arrived && location.clone().add(direction).getBlock().getType().isSolid()) {
			// Going into a wall

			if (onground && !location.clone().add(direction).add(0, 1, 0).getBlock().getType().isSolid()) {
				// Jump
				double newVelocity = 0.6;
				Location newLocation = location.clone();
				newLocation.subtract(0, velocity * GRAVITY_MODIFIER, 0);
				newLocation.add(0, newVelocity * GRAVITY_MODIFIER, 0);
				Node3D newNode = new Node3D(newLocation.getX(), newLocation.getY(), newLocation.getZ());
				if (isValidLocation(snapshot, newNode)) {
					location = newLocation;
					node = newNode;
					velocity = newVelocity;
				}
			}
		}

		location.setDirection(location.clone().subtract(target).toVector());
		entity.setLocation(location);
	}

	private boolean isValidLocation(MCSnapshot snapshot, Node3D node) {
		if (MCGraph.getUpperHeight(snapshot, node, null, 1) > node.y)
			return false;
		if (MCGraph.getLowerHeight(snapshot, new Node3D(node.x, node.y + HEIGHT, node.z), null, 1) < node.y + HEIGHT)
			return false;

		if (Math.floor(node.y + HEIGHT) >= (int) node.y + 2) {
			int end = (int) Math.floor(node.y + HEIGHT) - 1;
			for (int y = (int) node.y + 1; y <= end; y++) {
				MaterialData data = snapshot.get(new Node3D(node.x, node.y + y, node.z));
				if (data != null && data.getItemType().isSolid()) return false;
			}
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public void walkTowardsEntity(Entity entity) {
		this.target = entity;
	}

	public boolean isWalking() {
		return target != null;
	}

	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event) {
		if (target != null && event.getEntity().equals(target))
			walkTowardsEntity(null);
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		if (speed <= 0) throw new IllegalArgumentException("Speed must be greater than zero (strictly)!");
		this.speed = speed;
	}

	public double getAcceptableDistance() {
		return distance;
	}

	public void setAcceptableDistance(double distance) {
		if (distance < 0) throw new IllegalArgumentException("Distance must be greater or equal to zero!");
		this.distance = distance;
	}

}
