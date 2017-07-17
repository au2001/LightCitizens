package me.au2001.lightcitizens.managers;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.bukkit.Location;

import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.tinyprotocol.Reflection;
import org.bukkit.util.Vector;

public class PhysicsManager extends Manager {

	Class<?> clazz = null;
	PhysicsImpl impl = null;
	MirrorEntity mirror = null;
	
	private boolean collidable = false;
	private boolean waterBubbles = false;

	public PhysicsManager(FakeEntity entity) {
		super(entity);
		
		try {
			clazz = Class.forName(getClass().getName() + "_" + Reflection.getVersion());
			
			impl = (PhysicsImpl) clazz.getConstructor(getClass()).newInstance(this);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isCollidable() {
		return collidable;
	}
	
	public void setCollidable(boolean collidable) {
		this.collidable = collidable;
	}

	public boolean hasWaterBubbles() {
		return waterBubbles;
	}

	public void setWaterBubbles(boolean waterBubbles) {
		this.waterBubbles = waterBubbles;
	}

    public double getAttribute(Attribute attribute) {
        return mirror.getAttribute(attribute);
    }

    public void setAttribute(Attribute attribute, double value) {
        mirror.setAttribute(attribute, value);
    }

    public MirrorEntity getMirrorEntity() {
	    return mirror;
    }
	
	public void onManagerAdded() {
		if (clazz == null || impl == null) {
			entity.removeManager(getClass());
			String line1 = "PhysicsManager is not compatible with this version of Minecraft servers (" + Reflection.getVersion() + ").";
			String line2 = "If you are a developer, please write your own implementation of PhysicsManager compatible with " + Reflection.getVersion() + ".";
			throw new IllegalStateException(line1 + "\n" + line2);
		}
		
		mirror = impl.createMirrorEntity();
	}
	
	public void onManagerRemoved() {
		if (mirror != null) mirror.destroy();
	}

    public void tick() {
        if (mirror == null) return;

        if (entity.getLocation().distanceSquared(mirror.getLocation()) > 0)
            entity.setLocation(mirror.getLocation());
    }

    public enum Attribute {
        MAX_HEALTH, FOLLOW_RANGE, KNOCKBACK_RESISTANCE, MOVEMENT_SPEED, ATTACK_DAMAGE;
    }
	
	public interface MirrorEntity {

        Location getLocation();
        void setLocation(Location location);

        Vector getVelocity();
        void setVelocity(Vector vector);

        float getYaw();
        float getPitch();
        void setYawPitch(float yaw, float pitch);

        List<Object> getGoals();
        void addGoal(int priority, PathfinderGoal goal);
        void removeGoal(PathfinderGoal goal);
        void clearGoals();

        void setAttribute(Attribute attribute, double value);
        double getAttribute(Attribute attribute);

        boolean isAlive();
        void destroy();
    }

    public static abstract class PathfinderGoal {
        private int a;

        public PathfinderGoal() {}

        public boolean a() {
            return true;
        }

        public boolean b() {
            return this.a();
        }

        public boolean i() {
            return true;
        }

        public void c() {}

        public void d() {}

        public void e() {}

        public void a(int a) {
            this.a = a;
        }

        public int j() {
            return this.a;
        }
    }

    interface PhysicsImpl {

        MirrorEntity createMirrorEntity();

    }

}
