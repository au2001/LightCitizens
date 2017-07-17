package me.au2001.lightcitizens.packets;

import me.au2001.lightcitizens.tinyprotocol.Reflection;

public class PacketPlayOutEntityStatus extends Packet {

	private static final Class<?> CLAZZ = Reflection.getMinecraftClass("PacketPlayOutEntityStatus");

	public PacketPlayOutEntityStatus() {
		super(CLAZZ);
	}

}
