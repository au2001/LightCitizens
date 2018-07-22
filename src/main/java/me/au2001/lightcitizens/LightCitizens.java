package me.au2001.lightcitizens;

import io.netty.channel.Channel;
import me.au2001.lightcitizens.events.FakeEntityLeftClickedEvent;
import me.au2001.lightcitizens.events.FakeEntityRightClickedEvent;
import me.au2001.lightcitizens.managers.Manager;
import me.au2001.lightcitizens.packets.Packet;
import me.au2001.lightcitizens.packets.Packet.PacketListener;
import me.au2001.lightcitizens.tinyprotocol.Reflection;
import me.au2001.lightcitizens.tinyprotocol.TinyProtocol;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class LightCitizens extends JavaPlugin {

	private static Class<?> USE_ENTITY;
	private static Field A_FIELD, ACTION_FIELD;

	private static LightCitizens instance;

	private long ticks = 0;
	private List<Entry<Packet, Player>> packetsIn = new ArrayList<Entry<Packet, Player>>();
	private TinyProtocol protocol;
	private PacketListener packetListener;

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
				PacketListener.callPacketInAsync(sender, channel, new Packet(packet));

				return packet;
			}

			public Object onPacketOutAsync(Player receiver, Channel channel, Object packet) {
				PacketListener.callPacketOutAsync(receiver, channel, new Packet(packet));

				return packet;
			}
		};

		packetListener = new PacketListener() {

			public void onPacketInAsync(final Player sender, Channel channel, final Packet packet) {
				if (USE_ENTITY.isInstance(packet.toPacket())) {
					packetsIn.add(new Entry<Packet, Player>() {
						public Packet getKey() {
							return packet;
						}

						public Player getValue() {
							return sender;
						}

						public Player setValue(Player value) {
							throw new UnsupportedOperationException("Can't set value on anonymous Entry.");
						}
					});
				}
			}

		};
		packetListener.register();

		new BukkitRunnable() {
			public void run() {
				ticks++;

				for (FakeEntity entity : FakeEntity.entities) {
					entity.update();
					synchronized (entity) {
						for (Entry<Class<? extends Manager>, Manager> manager : entity.getManagers().entrySet())
							manager.getValue().syncTick();
					}
				}

				for (Entry<Packet, Player> entry : packetsIn) {
					Packet packet = entry.getKey();
					Player sender = entry.getValue();
					try {
						int entityId = A_FIELD.getInt(packet.toPacket());
						Object raw = ACTION_FIELD.get(packet.toPacket());
						String action = (String) raw.getClass().getMethod("name").invoke(raw);

						if (action.equals("INTERACT")) Bukkit.getPluginManager().callEvent(new FakeEntityRightClickedEvent(entityId, sender, packet));
						else if (action.equals("ATTACK")) Bukkit.getPluginManager().callEvent(new FakeEntityLeftClickedEvent(entityId, sender, packet));
					} catch (IllegalArgumentException | IllegalAccessException | SecurityException | InvocationTargetException | NoSuchMethodException e) {
						e.printStackTrace();
					}
				}
				packetsIn.clear();
			}
		}.runTaskTimer(this, 20, 0);

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
		}.runTaskTimerAsynchronously(this, 0, 0);
	}
	
	public void onDisable() {
		if (packetListener != null) packetListener.unregister();

		instance = null;
		protocol = null;
		packetListener = null;
	}

	public static long getTickTime() {
		return instance.ticks;
	}

	public static void sendPacket(Player player, Object packet) {
		instance.protocol.sendPacket(player, packet);
	}

	public static long getPing(Player player) {
		try {
			Class<?> craftPlayer = Reflection.getCraftBukkitClass("entity.CraftPlayer");
			Object handle = craftPlayer.getMethod("getHandle").invoke(player);
			return ((Integer) handle.getClass().getDeclaredField("ping").get(handle)).longValue();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static long getPingTicks(Player player) {
		return (long) Math.max(Math.ceil((double) getPing(player) * 20 / 1000), 1);
	}

	public static LightCitizens getInstance() {
		return instance;
	}
	
}
