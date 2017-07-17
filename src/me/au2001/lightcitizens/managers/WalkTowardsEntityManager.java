package me.au2001.lightcitizens.managers;

import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.managers.PhysicsManager.Attribute;
import me.au2001.lightcitizens.managers.PhysicsManager.MirrorEntity;
import me.au2001.lightcitizens.managers.PhysicsManager.PathfinderGoal;
import me.au2001.lightcitizens.tinyprotocol.Reflection;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class WalkTowardsEntityManager extends Manager {

    private static Method GET_NAVIGATION, NAVIGATION_SET, NAVIGATION_CREATE;

    private PhysicsManager physics;
    private boolean walking = false;
    private int updateTicks = 0;
    private int updateThreshold = 20;

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

	public WalkTowardsEntityManager(FakeEntity entity) {
		super(entity);
	}

	public void onManagerAdded() {
        if (!entity.hasManager(PhysicsManager.class))
            throw new IllegalStateException("WalkTowardsEntityManager requires an instance of PhysicsManager.");

        physics = entity.getManager(PhysicsManager.class);
	}

    public void onManagerRemoved() {
        walkTowardsEntity(null, 0, 0, 0);
    }

    public void tick() {
        if (updateTicks++ >= updateThreshold) {
            updateTicks = 0;

            for (Object goal : new ArrayList<Object> (physics.getMirrorEntity().getGoals()))
                if (goal instanceof PathfinderGoalWalkTowardsEntity)
                    ((PathfinderGoalWalkTowardsEntity) goal).c();
        }
    }

    @SuppressWarnings("unchecked")
	public void walkTowardsEntity(Entity entity, double speed, double range, double maxrange) {
        if (walking) {
            for (Object goal : new ArrayList<Object> (physics.getMirrorEntity().getGoals())) {
                if (goal instanceof PathfinderGoalWalkTowardsEntity)
                    physics.getMirrorEntity().removeGoal((PathfinderGoalWalkTowardsEntity) goal);
            }
            walking = false;
		}

		if (entity == null || speed <= 0 || maxrange <= 0) return;

        if (this.entity.hasManager(WalkTowardsLocationManager.class)) {
            WalkTowardsLocationManager manager = this.entity.getManager(WalkTowardsLocationManager.class);
            if (manager.isWalkingTowardsLocation()) manager.walkTowardsLocation(null, 0, 0);
        }

        physics.setAttribute(Attribute.FOLLOW_RANGE, maxrange);
        physics.setAttribute(Attribute.MOVEMENT_SPEED, speed*0.15);
		physics.getMirrorEntity().addGoal(1, new PathfinderGoalWalkTowardsEntity(physics.mirror, entity, range));
        walking = true;
	}

    public boolean isWalkingTowardsEntity() {
        return walking;
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        for (Object goal : new ArrayList<Object> (physics.getMirrorEntity().getGoals())) {
            if (goal instanceof PathfinderGoalWalkTowardsEntity) {
                if (event.getEntity().equals(((PathfinderGoalWalkTowardsEntity) goal).target)) {
                    physics.getMirrorEntity().removeGoal((PathfinderGoalWalkTowardsEntity) goal);
                    walking = false;
                }
            }
        }
    }

	private static class PathfinderGoalWalkTowardsEntity extends PathfinderGoal {

        private double range;
		private Entity target;
		private MirrorEntity entity;
        private Object navigation;

        private Location destination;

		public PathfinderGoalWalkTowardsEntity(MirrorEntity entity, Entity target, double range) {
            this.range = range;
			this.entity = entity;
			this.target = target;

			this.destination = entity.getLocation().clone();

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
			if (target == null || !target.isValid() || target.isDead()) return false;
			double maxrange = entity.getAttribute(Attribute.FOLLOW_RANGE);
			if (entity.getLocation().distanceSquared(target.getLocation()) > maxrange*maxrange) return false;
			return true;
		}

        public void c() {
            try {
                NAVIGATION_SET.invoke(navigation, NAVIGATION_CREATE.invoke(navigation, destination.getX(), destination.getY(), destination.getZ()), 1.0);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

		public void d() {
            try {
                NAVIGATION_SET.invoke(navigation, null, 0);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

		public void e() {
		    if (destination.getBlock().equals(target.getLocation().getBlock())) return;

		    if (destination.distanceSquared(target.getLocation())+1 > range*range) {
		        destination = target.getLocation().clone();
                c();
            }
		}
	}
	
}
