package me.au2001.lightcitizens.managers;

import me.au2001.lightcitizens.FakeEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

public class WalkTowardsEntityManager extends Manager {

    private static int updateThreshold = 20;

    private boolean walking = false;
    private int updateTicks = 0;

	public WalkTowardsEntityManager(FakeEntity entity) {
		super(entity);
	}

    public void onManagerRemoved() {
        walkTowardsEntity(null, 0, 0, 0);
    }

    public void tick() {
        if (updateTicks++ >= updateThreshold) {
            updateTicks = 0;

            // TODO
        }
    }

    @SuppressWarnings("unchecked")
	public void walkTowardsEntity(Entity entity, double speed, double range, double maxrange) {
        if (walking) {
            // TODO

            walking = false;
		}

		if (entity == null || speed <= 0 || maxrange <= 0) return;

        if (this.entity.hasManager(WalkTowardsLocationManager.class)) {
            WalkTowardsLocationManager manager = this.entity.getManager(WalkTowardsLocationManager.class);
            if (manager.isWalkingTowardsLocation()) manager.walkTowardsLocation(null, 0, 0);
        }

        // TODO

        walking = true;
	}

    public boolean isWalkingTowardsEntity() {
        return walking;
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        // TODO
    }
	
}
