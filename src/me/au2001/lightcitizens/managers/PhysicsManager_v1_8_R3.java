package me.au2001.lightcitizens.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import me.au2001.lightcitizens.managers.PhysicsManager.Attribute;
import me.au2001.lightcitizens.managers.PhysicsManager.MirrorEntity;
import me.au2001.lightcitizens.managers.PhysicsManager.PhysicsImpl;
import me.au2001.lightcitizens.tinyprotocol.Reflection;
import me.au2001.lightcitizens.tinyprotocol.Reflection.FieldAccessor;
import org.bukkit.util.Vector;

class PhysicsManager_v1_8_R3 implements PhysicsImpl {

	@SuppressWarnings("rawtypes")
	private static FieldAccessor<List> GOAL_B, GOAL_C;
	private static FieldAccessor<Boolean> INVULNERABLE;

	private PhysicsManager parent;
	private MirrorEntity_v1_8_R3 mirror;

	static {
		GOAL_B = Reflection.getField(PathfinderGoalSelector.class, "b", List.class);
		GOAL_C = Reflection.getField(PathfinderGoalSelector.class, "b", List.class);
		INVULNERABLE = Reflection.getField(Entity.class, "invulnerable", boolean.class);
	}

	public PhysicsManager_v1_8_R3(PhysicsManager parent) {
		this.parent = parent;
	}

	public MirrorEntity createMirrorEntity() {
		Location location = parent.entity.getLocation();
		World world = ((CraftWorld) location.getWorld()).getHandle();
		mirror = new MirrorEntity_v1_8_R3(world);
		mirror.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		mirror.setInvisible(true);
		world.addEntity(mirror);
		return mirror;
	}

    private class MirrorEntity_v1_8_R3 extends EntityZombie implements MirrorEntity {

        private HashMap<PhysicsManager.PathfinderGoal, PathfinderGoal> realgoals = new HashMap<PhysicsManager.PathfinderGoal, PathfinderGoal>();

		public MirrorEntity_v1_8_R3(World world) {
			super(world);

            GOAL_B.get(goalSelector).clear();
            GOAL_C.get(goalSelector).clear();
            GOAL_B.get(targetSelector).clear();
            GOAL_C.get(targetSelector).clear();

            getAttributeInstance(GenericAttributes.maxHealth).setValue(Double.MAX_VALUE);
			INVULNERABLE.set(this, false);
		}

        protected Item getLoot() {
            // Nothing to do. This disables all drops.
            return null;
        }

        protected void dropDeathLoot(boolean flag, int i) {
            // Nothing to do. This disables all drops.
        }

        public int getExpReward() {
            // Nothing to do. This disables exp drops.
            return 0;
        }

        protected boolean alwaysGivesExp() {
            // Nothing to do. This disables exp drops.
            return false;
        }

        protected int getExpValue(EntityHuman entityhuman) {
            // Nothing to do. This disables exp drops.
            return 0;
        }

        public void makeSound(String s, float f, float f1) {
            // Nothing to do. This disables all ambient sounds.
        }

        public void die() {
            // Nothing to do. This prevents it from dying.
        }

        public void die(DamageSource damagesource) {
            // Nothing to do. This prevents it from dying.
        }

        protected void X() {
            if (parent.hasWaterBubbles()) super.X();
        }

        public void collide(Entity entity) {
            if (parent.isCollidable()) super.collide(entity);
        }

        protected void s(Entity entity) {
            if (parent.isCollidable()) super.s(entity);
        }

        public void setOnFire(int i) {
            // Nothing to do. This prevents it from catching fire.
        }

        public boolean r(Entity entity) {
            // Nothing to do. This prevents it from catching fire.
            return false;
        }

//        public void m() {
//            // Reset ticksLived to prevent the entity from getting removed.
//            ticksLived = 0;
//        }

        public void inactiveTick() {
            // Reset ticksFarFromPlayer to prevent the entity from getting removed.
            ticksFarFromPlayer = 0;
        }

        public void a(NBTTagCompound nbttagcompound) {
            // Nothing to do. This prevents NBT data from being saved.
        }

        public void b(NBTTagCompound nbttagcompound) {
            // Nothing to do. This prevents NBT data from being saved.
        }

        public boolean c(NBTTagCompound nbttagcompound) {
            // Nothing to do. This prevents NBT data from being saved.
            return false;
        }

        public boolean d(NBTTagCompound nbttagcompound) {
            // Nothing to do. This prevents NBT data from being saved.
            return false;
        }

        public void e(NBTTagCompound nbttagcompound) {
            // Nothing to do. This prevents NBT data from being saved.
        }

        public void f(NBTTagCompound nbttagcompound) {
            // Nothing to do. This prevents NBT data from being saved.
        }

        public boolean damageEntity(DamageSource damagesource, float f) {
		    if (parent.entity.hasManager(DamageableManager.class)) {
                DamageableManager manager = parent.entity.getManager(DamageableManager.class);
                manager.handleDamage(damagesource, f);

                setHealth(getMaxHealth());
                return super.damageEntity(damagesource, 1);
            }
            return false;
        }

        public Location getLocation() {
            return new Location(world.getWorld(), locX, locY, locZ, yaw, pitch);
        }

        public void setLocation(Location location) {
            setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        }

        public float getYaw() {
            return yaw;
        }

        public float getPitch() {
            return pitch;
        }

        public void setYawPitch(float yaw, float pitch) {
            super.setYawPitch(yaw, pitch);
        }

        public Vector getVelocity() {
            return new Vector(motX, motY, motZ);
        }

        public void setVelocity(Vector velocity) {
            motX = velocity.getX();
            motY = velocity.getY();
            motZ = velocity.getZ();
            velocityChanged = true;
        }

        public List<Object> getGoals() {
            List<Object> goals = new ArrayList<Object>();
            for (Object goal : GOAL_B.get(mirror.goalSelector)) if (!goals.contains(goal)) goals.add(goal);
            for (Object goal : GOAL_C.get(mirror.goalSelector)) if (!goals.contains(goal)) goals.add(goal);
            for (Object goal : GOAL_B.get(mirror.targetSelector)) if (!goals.contains(goal)) goals.add(goal);
            for (Object goal : GOAL_C.get(mirror.targetSelector)) if (!goals.contains(goal)) goals.add(goal);
            return Collections.unmodifiableList(goals);
        }

        public void addGoal(int priority, PhysicsManager.PathfinderGoal goal) {
            PathfinderGoal realgoal = new PathfinderGoal() {
                public boolean a() {
                    return goal.a();
                }

                public boolean b() {
                    return goal.a();
                }

                public boolean i() {
                    return goal.i();
                }

                public void c() {
                    goal.c();
                }

                public void d() {
                    goal.d();
                }

                public void e() {
                    goal.e();
                }

                public void a(int a) {
                    goal.a(a);
                }

                public int j() {
                    return goal.j();
                }
            };
            realgoals.put(goal, realgoal);
            goalSelector.a(priority, realgoal);
        }

        @SuppressWarnings("unchecked")
        public void removeGoal(PhysicsManager.PathfinderGoal goal) {
            if (!realgoals.containsKey(goal)) return;
            PathfinderGoal realgoal = realgoals.remove(goal);
            realgoal.d();
            mirror.goalSelector.a(realgoal);
            mirror.targetSelector.a(realgoal);
        }

        public void clearGoals() {
            GOAL_B.get(goalSelector).clear();
            GOAL_C.get(goalSelector).clear();
            GOAL_B.get(targetSelector).clear();
            GOAL_C.get(targetSelector).clear();
        }

        public double getAttribute(Attribute attribute) {
            IAttribute iattribute = null;
            switch (attribute) {
                case MAX_HEALTH:
                    iattribute = GenericAttributes.maxHealth;
                    break;
                case ATTACK_DAMAGE:
                    iattribute = GenericAttributes.ATTACK_DAMAGE;
                    break;
                case FOLLOW_RANGE:
                    iattribute = GenericAttributes.FOLLOW_RANGE;
                    break;
                case KNOCKBACK_RESISTANCE:
                    iattribute = GenericAttributes.c;
                    break;
                case MOVEMENT_SPEED:
                    iattribute = GenericAttributes.MOVEMENT_SPEED;
                    break;
            }
            return mirror.getAttributeInstance(iattribute).getValue();
        }

        public void setAttribute(Attribute attribute, double value) {
            IAttribute iattribute = null;
            switch (attribute) {
                case MAX_HEALTH:
                    iattribute = GenericAttributes.maxHealth;
                    break;
                case ATTACK_DAMAGE:
                    iattribute = GenericAttributes.ATTACK_DAMAGE;
                    break;
                case FOLLOW_RANGE:
                    iattribute = GenericAttributes.FOLLOW_RANGE;
                    break;
                case KNOCKBACK_RESISTANCE:
                    iattribute = GenericAttributes.c;
                    break;
                case MOVEMENT_SPEED:
                    iattribute = GenericAttributes.MOVEMENT_SPEED;
                    break;
            }
            mirror.getAttributeInstance(iattribute).setValue(value);
        }

        public boolean isAlive() {
            return valid && !dead;
        }

		public void destroy() {
			world.kill(this);
            dead = true;
		}

    }

}
