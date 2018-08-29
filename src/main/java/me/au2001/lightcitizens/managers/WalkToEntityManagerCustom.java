package me.au2001.lightcitizens.managers;

//public class WalkToEntityManagerCustom extends Manager {
//
//    private static final int UPDATE_THRESHOLD = 1 * 20;
//
//    private int updateTicks = 0;
//    private WalkToLocationManagerCustom walkManager;
//    private Entity target = null;
//
//	public WalkToEntityManagerCustom(FakeEntity entity) {
//		super(entity);
//	}
//
//	public void onManagerAdded() {
//		this.walkManager = entity.getManager(WalkToLocationManagerCustom.class);
//		this.walkManager = this.walkManager == null? entity.addManager(WalkToLocationManagerCustom.class, LightCitizens.getInstance()) : null;
//	}
//
//	public void onManagerRemoved() {
//        walkToEntity(null);
//    }
//
//    public void tick() {
//        if (updateTicks >= UPDATE_THRESHOLD || updateTicks++ >= UPDATE_THRESHOLD) {
//        	if (target != null && !target.getLocation().getBlock().equals(walkManager.location.getBlock())) {
//		        updateTicks = 0;
//
//		        walkToEntity(target);
//	        }
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//	public void walkToEntity(Entity entity) {
//	    this.target = entity;
//
//		walkManager.walkToLocation(entity != null? entity.getLocation() : null);
//	}
//
//    public boolean isWalking() {
//        return walkManager.isWalking();
//    }
//
//	public double getSpeed() {
//		return walkManager.getSpeed();
//	}
//
//	public void setSpeed(double speed) {
//		walkManager.setSpeed(speed);
//	}
//
//	public double getAcceptableDistance() {
//		return walkManager.getAcceptableDistance();
//	}
//
//	public void setAcceptableDistance(double distance) {
//		walkManager.setAcceptableDistance(distance);
//	}
//
//	public double getMaximumJump() {
//		return walkManager.getMaximumJump();
//	}
//
//	public void setMaximumJump(int maxJump) {
//		walkManager.setMaximumJump(maxJump);
//	}
//
//	public double getMaximumFall() {
//		return walkManager.getMaximumFall();
//	}
//
//	public void setMaximumFall(int maxFall) {
//		walkManager.setAcceptableDistance(maxFall);
//	}
//
//    @EventHandler
//    public void onEntityDeathEvent(EntityDeathEvent event) {
//        if (target != null && event.getEntity().equals(target))
//            walkToEntity(null);
//    }
//
//}

import me.au2001.lightcitizens.FakeEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

public class WalkToEntityManagerCustom extends WalkToLocationManagerCustom {

	private static final int UPDATE_THRESHOLD = 3 * 20;

	private int updateTicks = 0;
	private Entity target = null;

	public WalkToEntityManagerCustom(FakeEntity entity) {
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

		if (updateTicks >= UPDATE_THRESHOLD || updateTicks++ >= UPDATE_THRESHOLD) {
	        if (destination == null || !target.getLocation().getBlock().equals(destination.getBlock())) {
		        updateTicks = 0;

		        walkToEntity(target);
	        }
	    }

		super.tick();
	}

	@SuppressWarnings("unchecked")
	public void walkToEntity(Entity entity) {
		walkToLocation(entity != null? entity.getLocation() : null);

//		if (target == null) return;
//
//		if (this.entity.hasManager(WalkToLocationManagerCustom.class)) {
//			WalkToLocationManagerCustom manager = this.entity.getManager(WalkToLocationManagerCustom.class);
//			if (manager.isWalking()) manager.walkToLocation(null);
//		}
	}

	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event) {
		if (target != null && event.getEntity().equals(target))
			walkToEntity(null);
	}

}
