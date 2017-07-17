package me.au2001.lightcitizens.managers;

import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.LightCitizens;
import me.au2001.lightcitizens.events.FakeEntityClickedEvent;
import me.au2001.lightcitizens.events.FakeEntityLeftClickedEvent;
import me.au2001.lightcitizens.packets.PacketPlayOutAnimation;
import me.au2001.lightcitizens.packets.PacketPlayOutNamedSoundEffect;
import me.au2001.lightcitizens.tinyprotocol.Reflection;
import me.au2001.lightcitizens.tinyprotocol.TinyProtocol;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class DamageableManager extends Manager {

    private static Field GET_PACKET, A_FIELD, ENTITY_ID;
    private static TinyProtocol protocol;

    private PhysicsManager physics;
    private float health = 20;
    private double protection = 0;
    private int deathCount = 0;

    static {
        try {
            GET_PACKET = FakeEntityClickedEvent.class.getDeclaredField("packet");
            GET_PACKET.setAccessible(true);

            A_FIELD = Reflection.getMinecraftClass("PacketPlayInUseEntity").getDeclaredField("a");
            A_FIELD.setAccessible(true);

            ENTITY_ID = Reflection.getMinecraftClass("Entity").getDeclaredField("id");
            ENTITY_ID.setAccessible(true);

            Field field = LightCitizens.class.getDeclaredField("protocol");
            field.setAccessible(true);
            protocol = (TinyProtocol) field.get(LightCitizens.getInstance());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

	public DamageableManager(FakeEntity entity) {
		super(entity);
	}

    public void onManagerAdded() {
        if (!entity.hasManager(PhysicsManager.class))
            throw new IllegalStateException("DamageableManager requires an instance of PhysicsManager.");

        physics = entity.getManager(PhysicsManager.class);
    }

    public void onLeftClicked(FakeEntityLeftClickedEvent event) {
        try {
            Object packet = GET_PACKET.get(event);
            A_FIELD.set(packet, ENTITY_ID.get(physics.getMirrorEntity()));

            protocol.receivePacket(event.getPlayer(), packet);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = Math.max(health, 0);
        if (health <= 0) {
            deathCount++;
            entity.destroy(true);
        }
    }

    public double getProtection() {
        return protection;
    }

    public void setProtection(double protection) {
        this.protection = Math.max(protection, 0);
    }

    public int getDeathCount() {
        return deathCount;
    }

    public void setDeathCount(int deathCount) {
        this.deathCount = deathCount;
    }

    void handleDamage(Object source, float damage) {
        setHealth(health - damage * 100F / ((float) protection + 100F));

        if (health > 0) {
            PacketPlayOutAnimation move = new PacketPlayOutAnimation();
            move.set("a", entity.getEntityId());
            move.set("b", 1); // 0: Swing main arm, 1: Take damage, 2: Leave bed, 3: Swing offhand, 4: Critical effect, 5: Magic critical effect
            for (Player observer : entity.getObservers()) move.send(observer);

            PacketPlayOutNamedSoundEffect sound = new PacketPlayOutNamedSoundEffect();
            sound.set("a", "game.player.hurt");
            sound.set("b", (int) (entity.getLocation().getX() * 8));
            sound.set("c", (int) (entity.getLocation().getY() * 8));
            sound.set("d", (int) (entity.getLocation().getZ() * 8));
            sound.set("e", 1F);
            sound.set("f", (int) (((entity.nextFloat() - entity.nextFloat()) * 0.2F + 1.0F) * 63.0F));
            for (Player observer : entity.getObservers()) sound.send(observer);
        }
    }
}
