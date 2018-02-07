package me.au2001.lightcitizens.managers;

import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.packets.PacketPlayOutAnimation;
import me.au2001.lightcitizens.packets.PacketPlayOutEntityHeadRotation;
import me.au2001.lightcitizens.packets.PacketPlayOutRelEntityMoveLook;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class AttackEntityManager extends Manager {

	private Damageable target;
	private double lookRange = 10;
    private double attackRange = 5;
    private int attackDelay = 20;
    private int randomDelay = 20;
    private double attackDamage = 2;
    private double knockback = 0.5;

    private int lastAttack = 0;
    private int totalKillCount = 0;
    private int playerKillCount = 0;

	public AttackEntityManager(FakeEntity entity) {
		super(entity);
	}

    public void onManagerAdded() {
        if (entity.hasManager(LookCloseManager.class)) lookRange = 0;
    }

    public void onManagerRemoved() {
        setTarget(null);
    }

    public void tick() {
	    if (lastAttack < attackDelay) lastAttack++;
	    if (target == null) return;
	    if (!target.isValid() || target.isDead() || !target.getWorld().equals(entity.getLocation().getWorld())) {
	        target = null;
	        return;
        }
        if (target instanceof Player && ((Player) target).getGameMode().equals(GameMode.SPECTATOR)) {
	        target = null;
	        return;
        }

        Location orientation = entity.getLocation();
        orientation.setDirection(target.getLocation().subtract(entity.getLocation()).toVector());

        if (lookRange > 0 && target.getLocation().distanceSquared(entity.getLocation()) <= lookRange * lookRange) {
            byte yaw = (byte) (orientation.getYaw() * 256.0F / 360.0F); // Between 0 and 256 instead of 0° to 360°
            byte pitch = (byte) (orientation.getPitch() * 128.0F / 180.0F); // Between -64 and +64 instead of -90° to +90°

            boolean updateyaw = entity.getLocation().getYaw() != yaw;
            boolean updatepitch = entity.getLocation().getPitch() != pitch;

            if (updateyaw) {
                PacketPlayOutEntityHeadRotation look = new PacketPlayOutEntityHeadRotation();
                look.set("a", entity.getEntityId());
                look.set("b", yaw);
                for (Player observer : entity.getObservers()) look.send(observer);
            }

            if (updateyaw || updatepitch) {
                PacketPlayOutRelEntityMoveLook move = new PacketPlayOutRelEntityMoveLook();
                move.set("a", entity.getEntityId());
                move.set("b", (byte) 0);
                move.set("c", (byte) 0);
                move.set("d", (byte) 0);
                move.set("e", yaw);
                move.set("f", pitch);
                for (Player observer : entity.getObservers()) move.send(observer);
            }
        }

        if (lastAttack < attackDelay) return;
        if (target instanceof LivingEntity && ((LivingEntity) target).getNoDamageTicks() > 0) return;
        if (target instanceof Player && ((Player) target).getGameMode().equals(GameMode.CREATIVE)) return;

        if (attackRange > 0 && target.getLocation().distanceSquared(entity.getLocation()) <= attackRange * attackRange) {
            PacketPlayOutAnimation move = new PacketPlayOutAnimation();
            move.set("a", entity.getEntityId());
            move.set("b", 0); // 0: Swing main arm, 1: Take damage, 2: Leave bed, 3: Swing offhand, 4: Critical effect, 5: Magic critical effect
            for (Player observer : entity.getObservers()) move.send(observer);

            int nodamage = -1;
            if (target instanceof LivingEntity) nodamage = ((LivingEntity) target).getNoDamageTicks();
            target.damage(attackDamage);

            if (target == null || target.isDead()) {
                totalKillCount++;
                if (target instanceof Player) playerKillCount++;
                target = null;
                return;
            }

            target.setVelocity(orientation.getDirection().setY(0).normalize().multiply(knockback).setY(knockback));
            if (target instanceof LivingEntity) ((LivingEntity) target).setNoDamageTicks(nodamage);

            lastAttack = -entity.nextInt(randomDelay);
        } else lastAttack = attackDelay - entity.nextInt(randomDelay);
    }

    public Damageable getTarget() {
        return target;
    }

    public void setTarget(Damageable target) {
        this.target = target;
    }

    public double getLookRange() {
        return lookRange;
    }

    public void setLookRange(double lookRange) {
        this.lookRange = lookRange;
    }

    public double getAttackRange() {
        return attackRange;
    }

    public void setAttackRange(double attackRange) {
        this.attackRange = attackRange;
    }

    public int getAttackDelay() {
        return attackDelay;
    }

    public void setAttackDelay(int attackDelay) {
	    if (lastAttack >= this.attackDelay) lastAttack = attackDelay;
        this.attackDelay = attackDelay;
    }

    public int getRandomDelay() {
        return randomDelay;
    }

    public void setRandomDelay(int randomDelay) {
        if (lastAttack < -randomDelay) lastAttack = -randomDelay;
        this.randomDelay = randomDelay;
    }

    public double getAttackDamage() {
        return attackDamage;
    }

    public void setAttackDamage(double attackDamage) {
        this.attackDamage = attackDamage;
    }

    public double getKnockback() {
        return knockback;
    }

    public void setKnockback(double knockback) {
        this.knockback = knockback;
    }

    public int getTotalKillCount() {
        return totalKillCount;
    }

    public void setTotalKillCount(int totalKillCount) {
        this.totalKillCount = totalKillCount;
    }

    public int getPlayerKillCount() {
        return playerKillCount;
    }

    public void setPlayerKillCount(int playerKillCount) {
        this.playerKillCount = playerKillCount;
    }

}
