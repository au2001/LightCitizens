package me.au2001.lightcitizens.packets;

import java.lang.reflect.Field;

import org.bukkit.entity.Player;

import me.au2001.lightcitizens.LightCitizens;

public class Packet {

	protected Object packet = null;
	
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
	
}
