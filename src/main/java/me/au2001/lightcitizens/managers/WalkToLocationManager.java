package me.au2001.lightcitizens.managers;

import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.pathfinder.MCGoal;
import org.bukkit.Location;

public class WalkToLocationManager extends Manager {

    protected Location destination = null;
    protected double speed = 1;
    private double distance = 0;
    private double maxJump = 1.2;
    private double maxFall = 3;
//    private BukkitTask task = null;
    protected MCGoal goal = null;
    protected MCGoal.Step step = null;

	public WalkToLocationManager(FakeEntity entity) {
		super(entity);
	}

	private void calculate() {
	    goal = null;
	    step = null;
	    if (destination == null) return;
//        if (task != null) task.cancel();
//        task = new BukkitRunnable() {
//            public void run() {
                goal = new MCGoal(entity.getLocation(), destination, distance, 2.0, (int) (20 / speed / 3), maxJump, maxFall);
//                task = null;
//            }
//        }.runTaskAsynchronously(LightCitizens.getInstance());
    }

    public void onManagerRemoved() {
        walkToLocation(null);
    }

    public void tick() {
	    if (goal == null) return;
        if (step == null || !step.hasNext()) {
            if (!goal.isReady()) return;

            if (!goal.hasNext()) {
                step = null;
                goal = null;
                return;
            } else step = goal.next();
        }

        Location location = step.next();
        location.setDirection(location.clone().subtract(entity.getLocation()).toVector());
        entity.setLocation(location);
    }

	@SuppressWarnings("unchecked")
	public void walkToLocation(Location location) {
        this.destination = location;

		if (location == null) return;

        calculate();
	}

    public boolean isWalking() {
        return destination != null;
    }
    
    public double getSpeed() {
	    return speed;
    }

    public void setSpeed(double speed) {
	    if (speed <= 0) throw new IllegalArgumentException("Speed must be greater than zero (strictly)!");
        if (speed != this.speed) {
            this.speed = speed;
            calculate();
        }
    }

    public double getAcceptableDistance() {
        return distance;
    }

    public void setAcceptableDistance(double distance) {
        if (distance < 0) throw new IllegalArgumentException("Distance must be greater or equal to zero!");
        if (distance != this.distance) {
            this.distance = distance;
            calculate();
        }
    }

    public double getMaximumJump() {
        return maxJump;
    }

    public void setMaximumJump(int maxJump) {
        if (maxJump < 0) throw new IllegalArgumentException("Maximum jump must be greater or equal to zero!");
        if (maxJump != this.maxJump) {
            this.maxJump = maxJump;
            calculate();
        }
    }

    public double getMaximumFall() {
        return maxFall;
    }

    public void setMaximumFall(int maxFall) {
        if (maxFall < 0) throw new IllegalArgumentException("Maximum fall must be greater or equal to zero!");
        if (maxFall != this.maxFall) {
            this.maxFall = maxFall;
            calculate();
        }
    }

}
