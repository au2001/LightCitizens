package me.au2001.lightcitizens.managers;

//public class WalkTowardsEntityManager extends Manager {
//
//    private static final int UPDATE_THRESHOLD = 1 * 20;
//
//    private int updateTicks = 0;
//    private WalkTowardsLocationManager walkManager;
//    private Entity entity = null;
//
//	public WalkTowardsEntityManager(FakeEntity entity) {
//		super(entity);
//	}
//
//	public void onManagerAdded() {
//		this.walkManager = super.entity.getManager(WalkTowardsLocationManager.class);
//		this.walkManager = this.walkManager == null? super.entity.addManager(WalkTowardsLocationManager.class, LightCitizens.getInstance()) : null;
//	}
//
//	public void onManagerRemoved() {
//        walkTowardsEntity(null);
//    }
//
//    public void tick() {
//        if (updateTicks >= UPDATE_THRESHOLD || updateTicks++ >= UPDATE_THRESHOLD) {
//        	if (entity != null && !entity.getLocation().getBlock().equals(walkManager.location.getBlock())) {
//		        updateTicks = 0;
//
//		        walkTowardsEntity(entity);
//	        }
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//	public void walkTowardsEntity(Entity entity) {
//	    this.entity = entity;
//
//		walkManager.walkTowardsLocation(entity != null? entity.getLocation() : null);
//	}
//
//    public boolean isWalkingTowardsEntity() {
//        return walkManager.isWalkingTowardsLocation();
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
//        if (entity != null && event.getEntity().equals(entity))
//            walkTowardsEntity(null);
//    }
//
//}

import me.au2001.lightcitizens.FakeEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

public class WalkTowardsEntityManager extends WalkTowardsLocationManager {

	private static final int UPDATE_THRESHOLD = 3 * 20;

	private int updateTicks = 0;
	private Entity entity = null;

	public WalkTowardsEntityManager(FakeEntity entity) {
		super(entity);
	}

	public void onManagerRemoved() {
		walkTowardsEntity(null);
	}

	public void tick() {
		if (updateTicks >= UPDATE_THRESHOLD || updateTicks++ >= UPDATE_THRESHOLD) {
	        if (entity != null && (location == null || !entity.getLocation().getBlock().equals(location.getBlock()))) {
		        updateTicks = 0;

		        walkTowardsEntity(entity);
	        }
	    }

		super.tick();
	}

	@SuppressWarnings("unchecked")
	public void walkTowardsEntity(Entity entity) {
		walkTowardsLocation(entity != null? entity.getLocation() : null);

//		if (entity == null) return;
//
//		if (super.entity.hasManager(WalkTowardsLocationManager.class)) {
//			WalkTowardsLocationManager manager = super.entity.getManager(WalkTowardsLocationManager.class);
//			if (manager.isWalkingTowardsLocation()) manager.walkTowardsLocation(null);
//		}
	}

	public boolean isWalkingTowardsEntity() {
		return isWalkingTowardsLocation();
	}

	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event) {
		if (entity != null && event.getEntity().equals(entity))
			walkTowardsEntity(null);
	}

}
