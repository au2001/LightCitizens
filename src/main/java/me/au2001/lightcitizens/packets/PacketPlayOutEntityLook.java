package me.au2001.lightcitizens.packets;

import me.au2001.lightcitizens.tinyprotocol.Reflection;

public class PacketPlayOutEntityLook extends Packet {

	private static final Class<?> CLAZZ = Reflection.getMinecraftClass("PacketPlayOutEntity$PacketPlayOutEntityLook");
	
	public PacketPlayOutEntityLook() {
		super(CLAZZ);
	}
	
}
