package me.au2001.lightcitizens.managers;

import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.managers.PhysicsManager.Attribute;
import me.au2001.lightcitizens.managers.PhysicsManager.MirrorEntity;
import me.au2001.lightcitizens.managers.PhysicsManager.PathfinderGoal;
import me.au2001.lightcitizens.tinyprotocol.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class WalkTowardsLocationManager extends Manager {

    private static Method GET_NAVIGATION, NAVIGATION_SET, NAVIGATION_CREATE;
    private static int updateThreshold = 20;

    private PhysicsManager physics;
    private boolean walking = false;
    private int updateTicks = 0;

    static {
        try {
            Class<?> nav = Reflection.getMinecraftClass("NavigationAbstract");

            GET_NAVIGATION = Reflection.getMinecraftClass("EntityInsentient").getMethod("getNavigation");

            NAVIGATION_SET = nav.getMethod("a", Reflection.getMinecraftClass("PathEntity"), double.class);

            NAVIGATION_CREATE = nav.getMethod("a", double.class, double.class, double.class);
        } catch (SecurityException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

	public WalkTowardsLocationManager(FakeEntity entity) {
		super(entity);
	}

	public void onManagerAdded() {
        if (!entity.hasManager(PhysicsManager.class))
            throw new IllegalStateException("WalkTowardsLocationManager requires an instance of PhysicsManager.");

        physics = entity.getManager(PhysicsManager.class);
	}

    public void onManagerRemoved() {
        walkTowardsLocation(null, 0, 0);
    }

    public void tick() {
        if (updateTicks++ >= updateThreshold) {
            updateTicks = 0;

            for (Object goal : new ArrayList<Object> (physics.getMirrorEntity().getGoals()))
                if (goal instanceof PathfinderGoalWalkTowardsLocation)
                    ((PathfinderGoalWalkTowardsLocation) goal).c();
        }
    }

	@SuppressWarnings("unchecked")
	public void walkTowardsLocation(Location location, double speed, double range) {
        if (walking) {
            for (Object goal : new ArrayList<Object> (physics.getMirrorEntity().getGoals())) {
                if (goal instanceof PathfinderGoalWalkTowardsLocation) {
                    ((PathfinderGoalWalkTowardsLocation) goal).d();
                    physics.getMirrorEntity().removeGoal((PathfinderGoalWalkTowardsLocation) goal);
                }
            }
            walking = false;
        }

		if (location == null || speed <= 0 || range <= 0) return;

        if (entity.hasManager(WalkTowardsEntityManager.class)) {
            WalkTowardsEntityManager manager = entity.getManager(WalkTowardsEntityManager.class);
            if (manager.isWalkingTowardsEntity()) manager.walkTowardsEntity(null, 0, 0, 0);
        }

        physics.setAttribute(Attribute.FOLLOW_RANGE, range);
        physics.setAttribute(Attribute.MOVEMENT_SPEED, speed*0.15);
		physics.getMirrorEntity().addGoal(1, new PathfinderGoalWalkTowardsLocation(physics.mirror, location));
        walking = true;
	}

    public boolean isWalkingTowardsLocation() {
        return walking;
    }

	private static class PathfinderGoalWalkTowardsLocation extends PathfinderGoal {

		private Location destination;
		private MirrorEntity entity;
        private Object navigation;

		private boolean close = false;

		public PathfinderGoalWalkTowardsLocation(MirrorEntity entity, Location destination) {
			this.entity = entity;
			this.destination = destination.clone();

			try {
                this.navigation = GET_NAVIGATION.invoke(entity);
            } catch (IllegalAccessException | InvocationTargetException e) {
			    e.printStackTrace();
            }
		}

		public boolean a() {
			return true;
		}

        public boolean b() {
            return true;
        }

		public void d() {
            try {
                NAVIGATION_SET.invoke(navigation, null, 0);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

		public void c() {
            try {
                Bukkit.getLogger().info("Rerouting navigation to (" + destination.getX() + " ; " + destination.getY() + " ; " + destination.getZ() + ")...");
                NAVIGATION_SET.invoke(navigation, NAVIGATION_CREATE.invoke(navigation, destination.getX(), destination.getY(), destination.getZ()), 1.0);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

		public void e() {
            Bukkit.getLogger().info("Walking... (" + entity.getLocation().getX() + " ; " + entity.getLocation().getY() + " ; " + entity.getLocation().getZ() + ") -> (" + destination.getX() + " ; " + destination.getY() + " ; " + destination.getZ() + ")");
			if (entity.getLocation().getBlockX() == destination.getBlockX() && entity.getLocation().getBlockZ() == destination.getBlockZ()) {
				if (!close) {
					this.d();
					close = true;
				}

				Vector direction = new Vector(destination.getX() - entity.getLocation().getX(), 0, destination.getZ() - entity.getLocation().getZ());
				if (direction.lengthSquared() > 1.0/(32.0 * 32.0)) {
				    entity.setVelocity(new Vector(direction.getX(), 0, direction.getZ()));
				    entity.setYawPitch(destination.clone().setDirection(direction.clone()).getYaw(), entity.getPitch());
				} else {
					entity.removeGoal(this);
					entity.setLocation(destination);
				}
			} else if (close) {
				this.c();
				close = false;
			}
		}
	}
	
}
