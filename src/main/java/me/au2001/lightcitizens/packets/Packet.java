package me.au2001.lightcitizens.packets;

import io.netty.channel.Channel;
import me.au2001.lightcitizens.LightCitizens;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Packet {

	protected Object packet = null;

	public Packet(Object packet) {
		this.packet = packet;
	}
	
	public Packet(Class<?> clazz) {
		try {
			packet = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public Object get(String name) {
		try {
			Field field = getField(packet.getClass(), name);
			field.setAccessible(true);
			return field.get(packet);
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void set(String name, Object value) {
		try {
			Field field = getField(packet.getClass(), name);
			field.setAccessible(true);
			field.set(packet, value);
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	private Field getField(Class<?> clazz, String name) throws SecurityException {
		if (clazz == null || name == null) return null;
		try {
			return clazz.getDeclaredField(name);
		} catch (NoSuchFieldException e) {
			return getField(clazz.getSuperclass(), name);
		}
	}

	public void send(Player player) {
		LightCitizens.sendPacket(player, packet);
	}

	public Object toPacket() {
		return packet;
	}

	public static abstract class PacketListener {

		private static List<PacketListener> packetListeners = new ArrayList<PacketListener>();

		public void register() {
			if (packetListeners.contains(this)) return;

			packetListeners.add(this);
		}

		public void unregister() {
			if (!packetListeners.contains(this)) return;

			packetListeners.remove(this);
		}

		public void onPacketInAsync(Player sender, Channel channel, Packet packet) {}
		public void onPacketOutAsync(Player receiver, Channel channel, Packet packet) {}

		public static void callPacketInAsync(Player sender, Channel channel, Packet packet) {
			for (PacketListener listener : packetListeners) listener.onPacketInAsync(sender, channel, packet);
		}

		public static void callPacketOutAsync(Player receiver, Channel channel, Packet packet) {
			for (PacketListener listener : packetListeners) listener.onPacketOutAsync(receiver, channel, packet);
		}
	}
	
}
