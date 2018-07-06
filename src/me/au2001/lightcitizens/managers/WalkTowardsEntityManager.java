package me.au2001.lightcitizens.managers;

import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.pathfinder.MCGraph;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.util.Vector;

public class WalkTowardsEntityManager extends Manager {

	private final static double DECELERATION_RATE = 0.02D;
	private final static double GRAVITY_CONSTANT = 0.08D;
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
		Location target = this.target.getLocation();
		if (target.getWorld() == null || !target.getWorld().equals(entity.getLocation().getWorld())) {
			walkTowardsEntity(null);
			return;
		}

		Location location = entity.getLocation();
//		if (!isValidLocation(location)) return; // Blocked in a block

		boolean arrived = Math.pow(location.getY() - target.getY(), 2) + Math.pow(location.getZ() - target.getZ(), 2) <= distance * distance; // Close enough

		Vector direction = target.clone().subtract(location).toVector().setY(0).normalize();

		if (!arrived) {
			location.add(direction.clone().multiply(speed * SPEED_MODIFIER));
			if (!isValidLocation(location)) { // Blocked by a wall
				location.subtract(direction.clone().multiply(speed * SPEED_MODIFIER));
				// TODO: Find another way around

				this.target.sendMessage("U: " + MCGraph.getUpperHeight(location.getBlock(), null, 1) + " <= " + (location.getY() + 0.2));
				this.target.sendMessage("L: " + MCGraph.getLowerHeight(location.clone().add(0, HEIGHT, 0).getBlock(), null, 1) + " >= " + (location.getY() + HEIGHT - 0.2));
			}
		}

		velocity = (velocity - GRAVITY_CONSTANT) * (1 - DECELERATION_RATE); // Apply gravity and deceleration

		boolean onground = false;
		location.add(0, velocity * GRAVITY_MODIFIER, 0);
		if (!isValidLocation(location)) { // Blocked by ceiling/floor
			if (velocity <= 0) {
				location.setY(MCGraph.getUpperHeight(location.getBlock(), null, 2));
				onground = true;
			} else location.setY(MCGraph.getLowerHeight(location.getBlock(), null, 2));
			velocity = 0;
		}

		if (!arrived && location.clone().add(direction).getBlock().getType().isSolid()) {
			// Going into a wall

			if (onground && !location.clone().add(direction).add(0, 1, 0).getBlock().getType().isSolid()) {
				// Jump
				double newVelocity = 1;
				Location newLocation = location.clone();
				newLocation.subtract(0, velocity * GRAVITY_MODIFIER, 0);
				newLocation.add(0, newVelocity * GRAVITY_MODIFIER, 0);
				if (isValidLocation(newLocation)) {
					location = newLocation;
					velocity = newVelocity;
				}
			}
		}

		location.setDirection(location.clone().subtract(target).toVector());
		entity.setLocation(location);
	}

	private boolean isValidLocation(Location location) {
		if (MCGraph.getUpperHeight(location.getBlock(), null, 1) > location.getY() + 0.2) // Bypass carpets and stuff
			return false;
		if (MCGraph.getLowerHeight(location.clone().add(0, HEIGHT, 0).getBlock(), null, 1) < location.getY() + HEIGHT - 0.2)
			return false;

		if (Math.floor(location.getY() + HEIGHT) >= location.getBlockY() + 2) {
			int end = (int) Math.floor(location.getY() + HEIGHT) - 1;
			for (int y = location.getBlockY() + 1; y <= end; y++)
				if (location.clone().add(0, y, 0).getBlock().getType().isSolid()) return false;
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
