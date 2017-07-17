package me.au2001.lightcitizens;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import io.netty.channel.Channel;
import me.au2001.lightcitizens.events.FakeEntityLeftClickedEvent;
import me.au2001.lightcitizens.events.FakeEntityRightClickedEvent;
import me.au2001.lightcitizens.managers.Manager;
import me.au2001.lightcitizens.tinyprotocol.Reflection;
import me.au2001.lightcitizens.tinyprotocol.TinyProtocol;

public class LightCitizens extends JavaPlugin {

	private static Class<?> USE_ENTITY;
	private static Field A_FIELD, ACTION_FIELD;

	private static LightCitizens instance;

	private TinyProtocol protocol;

	static {
		try {
			USE_ENTITY = Reflection.getMinecraftClass("PacketPlayInUseEntity");
			A_FIELD = USE_ENTITY.getDeclaredField("a");
			A_FIELD.setAccessible(true);
			ACTION_FIELD = USE_ENTITY.getDeclaredField("action");
			ACTION_FIELD.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onEnable() {
		instance = this;

		protocol = new TinyProtocol(this) {
			public Object onPacketInAsync(Player sender, Channel channel, Object packet) {
				return LightCitizens.onPacketInAsync(sender, channel, packet);
			}
		};
		
		new BukkitRunnable() {
			public void run() {
				for (FakeEntity entity : FakeEntity.entities) {
					entity.update();
					synchronized (entity) {
						for (Entry<Class<? extends Manager>, Manager> manager : entity.getManagers().entrySet())
							manager.getValue().tick();
					}
				}
			}
		}.runTaskTimer(this, 0, 0);
	}
	
	public void onDisable() {
		instance = null;
		protocol = null;
	}

	private static Object onPacketInAsync(Player sender, Channel channel, Object packet) {
		if (!USE_ENTITY.isInstance(packet)) return packet;
		new BukkitRunnable() {
			public void run() {
				try {
					int entityId = A_FIELD.getInt(packet);
					Object raw = ACTION_FIELD.get(packet);
					String action = (String) raw.getClass().getMethod("name").invoke(raw);

					if (action.equals("INTERACT")) Bukkit.getPluginManager().callEvent(new FakeEntityRightClickedEvent(entityId, sender, packet));
					else if (action.equals("ATTACK")) Bukkit.getPluginManager().callEvent(new FakeEntityLeftClickedEvent(entityId, sender, packet));

				} catch (IllegalArgumentException | IllegalAccessException | SecurityException | InvocationTargetException | NoSuchMethodException e) {
					e.printStackTrace();
				}
			}
		}.runTask(instance);
		return packet;
	}

	public static void sendPacket(Player player, Object packet) {
		instance.protocol.sendPacket(player, packet);
	}

	public static LightCitizens getInstance() {
		return instance;
	}
	
}
