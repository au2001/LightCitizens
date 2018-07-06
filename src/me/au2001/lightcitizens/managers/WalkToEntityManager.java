package me.au2001.lightcitizens.managers;

//public class WalkToEntityManager extends Manager {
//
//    private static final int UPDATE_THRESHOLD = 1 * 20;
//
//    private int updateTicks = 0;
//    private WalkToLocationManager walkManager;
//    private Entity target = null;
//
//	public WalkToEntityManager(FakeEntity entity) {
//		super(entity);
//	}
//
//	public void onManagerAdded() {
//		this.walkManager = entity.getManager(WalkToLocationManager.class);
//		this.walkManager = this.walkManager == null? entity.addManager(WalkToLocationManager.class, LightCitizens.getInstance()) : null;
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

public class WalkToEntityManager extends WalkToLocationManager {

	private static final int UPDATE_THRESHOLD = 3 * 20;

	private int updateTicks = 0;
	private Entity target = null;

	public WalkToEntityManager(FakeEntity entity) {
		super(entity);
	}

	public void onManagerRemoved() {
		walkToEntity(null);
	}

	public void tick() {
		if (updateTicks >= UPDATE_THRESHOLD || updateTicks++ >= UPDATE_THRESHOLD) {
	        if (target != null && (destination == null || !target.getLocation().getBlock().equals(destination.getBlock()))) {
		        updateTicks = 0;

		        walkToEntity(target);
	        }
	    }
	}

	@SuppressWarnings("unchecked")
	public void walkToEntity(Entity entity) {
		walkToLocation(entity != null? entity.getLocation() : null);

//		if (target == null) return;
//
//		if (this.entity.hasManager(WalkToLocationManager.class)) {
//			WalkToLocationManager manager = this.entity.getManager(WalkToLocationManager.class);
//			if (manager.isWalking()) manager.walkToLocation(null);
//		}
	}

	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event) {
		if (target != null && event.getEntity().equals(target))
			walkToEntity(null);
	}

}
