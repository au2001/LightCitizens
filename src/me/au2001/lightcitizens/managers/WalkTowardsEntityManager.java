package me.au2001.lightcitizens.managers;

import me.au2001.lightcitizens.FakeEntity;
import org.bukkit.Location;
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
        if (updateTicks++ >= UPDATE_THRESHOLD) {
            updateTicks = 0;

            walkTowardsEntity(entity);
        }

        super.tick();
    }

    @SuppressWarnings("unchecked")
	public void walkTowardsEntity(Entity entity) {
	    Location location = entity != null? entity.getLocation() : null;
		walkTowardsLocation(location);

		if (entity == null) return;

        if (super.entity.hasManager(WalkTowardsLocationManager.class)) {
            WalkTowardsLocationManager manager = super.entity.getManager(WalkTowardsLocationManager.class);
            if (manager.isWalkingTowardsLocation()) manager.walkTowardsLocation(null);
        }
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
