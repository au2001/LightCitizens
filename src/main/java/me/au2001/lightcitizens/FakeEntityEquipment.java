package me.au2001.lightcitizens;

import me.au2001.lightcitizens.packets.PacketPlayOutEntityEquipment;
import me.au2001.lightcitizens.tinyprotocol.Reflection;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FakeEntityEquipment {

    private static Method ITEM_TO_NMS;

	private FakeEntity entity;
	private ItemStack hand, helmet, chestplate, leggings, boots;

    static {
        try {
            ITEM_TO_NMS = Reflection.getCraftBukkitClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class);
        } catch (SecurityException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

	FakeEntityEquipment(FakeEntity entity, ItemStack hand, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
		this.entity = entity;
		this.hand = hand;
		this.helmet = helmet;
		this.chestplate = chestplate;
		this.leggings = leggings;
		this.boots = boots;
	}

	public boolean hasItemInHand() {
		return hand != null && !hand.getType().equals(Material.AIR);
	}

	public ItemStack getItemInHand() {
		return hand != null? hand : new ItemStack(Material.AIR);
	}

	public void setItemInHand(ItemStack hand) {
		if (this.hand == null && hand == null) return;
		if (this.hand != null && hand != null && this.hand.equals(hand)) return;

		this.hand = hand;
		entity.changed = true;
	}

    public boolean hasHelmet() {
        return helmet != null && !helmet.getType().equals(Material.AIR);
    }

    public ItemStack getHelmet() {
        return helmet != null? helmet : new ItemStack(Material.AIR);
    }

    public void setHelmet(ItemStack helmet) {
        if (this.helmet == null && helmet == null) return;
        if (this.helmet != null && helmet != null && this.helmet.equals(helmet)) return;

        this.helmet = helmet;
        entity.changed = true;
    }

    public boolean hasChestplate() {
        return chestplate != null && !chestplate.getType().equals(Material.AIR);
    }

    public ItemStack getChestplate() {
        return chestplate != null? chestplate : new ItemStack(Material.AIR);
    }

    public void setChestplate(ItemStack chestplate) {
        if (this.chestplate == null && chestplate == null) return;
        if (this.chestplate != null && chestplate != null && this.chestplate.equals(chestplate)) return;

        this.chestplate = chestplate;
        entity.changed = true;
    }

    public boolean hasLeggings() {
        return leggings != null && !leggings.getType().equals(Material.AIR);
    }

    public ItemStack getLeggings() {
        return leggings != null? leggings : new ItemStack(Material.AIR);
    }

    public void setLeggings(ItemStack leggings) {
        if (this.leggings == null && leggings == null) return;
        if (this.leggings != null && leggings != null && this.leggings.equals(leggings)) return;

        this.leggings = leggings;
        entity.changed = true;
    }

    public boolean hasBoots() {
        return boots != null && !boots.getType().equals(Material.AIR);
    }

    public ItemStack getBoots() {
        return boots != null? boots : new ItemStack(Material.AIR);
    }

    public void setBoots(ItemStack boots) {
        if (this.boots == null && boots == null) return;
        if (this.boots != null && boots != null && this.boots.equals(boots)) return;

        this.boots = boots;
        entity.changed = true;
    }

    void send(Player player) {
        try {
            PacketPlayOutEntityEquipment equip = new PacketPlayOutEntityEquipment();
            equip.set("a", entity.getEntityId());
            equip.set("b", 0);
            equip.set("c", hasItemInHand()? ITEM_TO_NMS.invoke(null, hand) : ITEM_TO_NMS.invoke(null, new ItemStack(Material.AIR)));
            equip.send(player);

	        equip = new PacketPlayOutEntityEquipment();
	        equip.set("a", entity.getEntityId());
	        equip.set("b", 1);
	        equip.set("c", hasBoots()? ITEM_TO_NMS.invoke(null, boots) : ITEM_TO_NMS.invoke(null, new ItemStack(Material.AIR)));
	        equip.send(player);

	        equip = new PacketPlayOutEntityEquipment();
	        equip.set("a", entity.getEntityId());
	        equip.set("b", 2);
	        equip.set("c", hasLeggings()? ITEM_TO_NMS.invoke(null, leggings) : ITEM_TO_NMS.invoke(null, new ItemStack(Material.AIR)));
	        equip.send(player);

	        equip = new PacketPlayOutEntityEquipment();
	        equip.set("a", entity.getEntityId());
	        equip.set("b", 3);
	        equip.set("c", hasChestplate()? ITEM_TO_NMS.invoke(null, chestplate) : ITEM_TO_NMS.invoke(null, new ItemStack(Material.AIR)));
	        equip.send(player);

	        equip = new PacketPlayOutEntityEquipment();
	        equip.set("a", entity.getEntityId());
            equip.set("b", 4);
            equip.set("c", hasHelmet()? ITEM_TO_NMS.invoke(null, helmet) : ITEM_TO_NMS.invoke(null, new ItemStack(Material.AIR)));
            equip.send(player);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
