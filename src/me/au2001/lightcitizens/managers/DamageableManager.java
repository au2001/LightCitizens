package me.au2001.lightcitizens.managers;

import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.events.FakeEntityLeftClickedEvent;
import me.au2001.lightcitizens.nbt.Attributes;
import me.au2001.lightcitizens.nbt.Attributes.Attribute;
import me.au2001.lightcitizens.nbt.Attributes.AttributeType;
import me.au2001.lightcitizens.nbt.Attributes.Operation;
import me.au2001.lightcitizens.nbt.NbtFactory;
import me.au2001.lightcitizens.nbt.NbtFactory.NbtCompound;
import me.au2001.lightcitizens.nbt.NbtFactory.NbtList;
import me.au2001.lightcitizens.packets.PacketPlayOutAnimation;
import me.au2001.lightcitizens.packets.PacketPlayOutNamedSoundEffect;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DamageableManager extends Manager {

    private float health = 20;
    private double protection = 0;
    private int deathCount = 0;

	public DamageableManager(FakeEntity entity) {
		super(entity);
	}

    public void onLeftClicked(FakeEntityLeftClickedEvent event) {
        handleDamage(event.getPlayer());
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
	    if (health < 0) health = 0;
        this.health = health;
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

    public void handleDamage(LivingEntity source) {
	    if (source == null) return;
	    ItemStack item = source.getEquipment() != null? source.getEquipment().getItemInHand() : null;
        handleDamage(source.getEyeLocation(), item);
    }

    public void handleDamage(Location source, ItemStack item) {
        if (source == null) return;
        handleDamage(source, getItemKnockback(item), getItemDamage(item));
    }

    public void handleDamage(Location source, double knockback, float damage) {
        setHealth(health - damage * 100F / ((float) protection + 100F));

        // TODO: Knockback

        if (health > 0) {
            PacketPlayOutAnimation move = new PacketPlayOutAnimation();
            move.set("a", entity.getEntityId());
            move.set("b", 1); // 0: Swing main arm, 1: Take damage, 2: Leave bed, 3: Swing offhand, 4: Critical effect, 5: Magic critical effect
            for (Player observer : entity.getVisibleObservers()) move.send(observer);

            PacketPlayOutNamedSoundEffect sound = new PacketPlayOutNamedSoundEffect();
            sound.set("a", "game.player.hurt");
            sound.set("b", (int) (entity.getLocation().getX() * 8));
            sound.set("c", (int) (entity.getLocation().getY() * 8));
            sound.set("d", (int) (entity.getLocation().getZ() * 8));
            sound.set("e", 1F);
            sound.set("f", (int) (((entity.nextFloat() - entity.nextFloat()) * 0.2F + 1.0F) * 63.0F));
            for (Player observer : entity.getVisibleObservers()) sound.send(observer);
        }
    }

    private static float getItemDamage(ItemStack item) {
        double damage = 2.0;
	    if (item == null) return (float) Math.max(Math.min(damage, 0), 2048);

        NbtCompound nbtCompound = NbtFactory.fromItemTag(NbtFactory.getCraftItemStack(item));
        NbtList attributeList = nbtCompound.getList("Attributes", false);

        if (attributeList != null) {
            for (Object attribute : attributeList) {
                String attributeName = ((NbtCompound) attribute).getString("Name", "");
                if (!attributeName.equals(AttributeType.GENERIC_ATTACK_DAMAGE.getMinecraftId())) continue;
                damage = ((NbtCompound) attribute).getDouble("Base", damage);
                break;
            }
        }

        Attributes attributes = new Attributes(item);

        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
            if (!attribute.getAttributeType().equals(AttributeType.GENERIC_ATTACK_DAMAGE)) continue;
            if (!attribute.getOperation().equals(Operation.ADD_NUMBER)) continue;

            damage += attribute.getAmount();
        }

        double muliplyTotal = 1;
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
            if (!attribute.getAttributeType().equals(AttributeType.GENERIC_ATTACK_DAMAGE)) continue;
            if (!attribute.getOperation().equals(Operation.MULTIPLY_PERCENTAGE)) continue;

            muliplyTotal += attribute.getAmount();
        }
        damage *= muliplyTotal;

        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
            if (!attribute.getAttributeType().equals(AttributeType.GENERIC_ATTACK_DAMAGE)) continue;
            if (!attribute.getOperation().equals(Operation.ADD_PERCENTAGE)) continue;

            damage *= 1 + attribute.getAmount();
        }

        return (float) Math.max(Math.min(damage, 0), 2048);
    }

    private static double getItemKnockback(ItemStack item) {
	    if (item == null) return 0;
        // TODO: Knockback
        return 0;
    }

}
