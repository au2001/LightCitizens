package me.au2001.lightcitizens.managers;

import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.pathfinder.Node.Node3D;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

public class WalkToEntityManager extends WalkToLocationManager {

	private static final int UPDATE_THRESHOLD = 20;

	private int updateTicks = 0;
	private Entity target = null;

	public WalkToEntityManager(FakeEntity entity) {
		super(entity);
	}

	public void onManagerRemoved() {
		walkToEntity(null);
	}

	public void tick() {
		if (target == null) return;
		if (target.isDead() || !target.isValid() || target.getWorld() == null || !target.getWorld().equals(entity.getLocation().getWorld())) {
			target = null;
			return;
		}

		if (updateTicks >= UPDATE_THRESHOLD) {
	        if (destination == null || !target.getLocation().getBlock().equals(destination.getBlock())) {
		        updateTicks = 0;

		        destination = target.getLocation();
		        plan = null;

		        boolean refreshSource = blockSource == null;
		        refreshSource = refreshSource || !blockSource.isInBound(new Node3D(entity.getLocation().getBlockX(), entity.getLocation().getBlockY(), entity.getLocation().getBlockZ()));
		        refreshSource = refreshSource || !blockSource.isInBound(new Node3D(destination.getBlockX(), destination.getBlockY(), destination.getBlockZ()));
		        if (refreshSource) {
		        	if (blockSource != null) blockSource.close();
		        	blockSource = null;
			        loading = false;
		        }
	        }
	    } else updateTicks++;

		super.tick();
	}

	@SuppressWarnings("unchecked")
	public void walkToEntity(Entity entity) {
		target = entity;
		walkToLocation(entity != null? entity.getLocation() : null);
	}

	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event) {
		if (target != null && event.getEntity().equals(target))
			walkToEntity(null);
	}

}
