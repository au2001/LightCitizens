package me.au2001.lightcitizens.packets;

import me.au2001.lightcitizens.tinyprotocol.Reflection;

public class PacketPlayOutEntityEquipment extends Packet {

	private static final Class<?> CLAZZ = Reflection.getMinecraftClass("PacketPlayOutEntityEquipment");

	public PacketPlayOutEntityEquipment() {
		super(CLAZZ);
	}
	
}
