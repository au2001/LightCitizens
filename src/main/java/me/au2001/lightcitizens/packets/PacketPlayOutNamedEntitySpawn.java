package me.au2001.lightcitizens.packets;

import me.au2001.lightcitizens.tinyprotocol.Reflection;

public class PacketPlayOutNamedEntitySpawn extends Packet {

	private static final Class<?> CLAZZ = Reflection.getMinecraftClass("PacketPlayOutNamedEntitySpawn");
	
	public PacketPlayOutNamedEntitySpawn() {
		super(CLAZZ);
	}

}
