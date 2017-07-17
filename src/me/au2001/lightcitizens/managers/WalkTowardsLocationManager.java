package me.au2001.lightcitizens.managers;

import me.au2001.lightcitizens.FakeEntity;
import org.bukkit.Location;

public class WalkTowardsLocationManager extends Manager {

    private static int updateThreshold = 20;

    private boolean walking = false;
    private int updateTicks = 0;

	public WalkTowardsLocationManager(FakeEntity entity) {
		super(entity);
	}

    public void onManagerRemoved() {
        walkTowardsLocation(null, 0, 0);
    }

    public void tick() {
        if (updateTicks++ >= updateThreshold) {
            updateTicks = 0;

            // TODO
        }
    }

	@SuppressWarnings("unchecked")
	public void walkTowardsLocation(Location location, double speed, double range) {
        if (walking) {
            // TODO

            walking = false;
        }

		if (location == null || speed <= 0 || range <= 0) return;

        if (entity.hasManager(WalkTowardsEntityManager.class)) {
            WalkTowardsEntityManager manager = entity.getManager(WalkTowardsEntityManager.class);
            if (manager.isWalkingTowardsEntity()) manager.walkTowardsEntity(null, 0, 0, 0);
        }

        // TODO

        walking = true;
	}

    public boolean isWalkingTowardsLocation() {
        return walking;
    }
	
}
