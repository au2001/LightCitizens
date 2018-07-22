package me.au2001.lightcitizens.packets;

import me.au2001.lightcitizens.tinyprotocol.Reflection;

public class PacketPlayOutEntityTeleport extends Packet {

	private static final Class<?> CLAZZ = Reflection.getMinecraftClass("PacketPlayOutEntityTeleport");
	
	public PacketPlayOutEntityTeleport() {
		super(CLAZZ);
	}
	
}
