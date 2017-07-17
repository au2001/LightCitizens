package me.au2001.lightcitizens.packets;

import me.au2001.lightcitizens.tinyprotocol.Reflection;

public class PacketPlayOutEntityDestroy extends Packet {

	private static final Class<?> CLAZZ = Reflection.getMinecraftClass("PacketPlayOutEntityDestroy");
	
	public PacketPlayOutEntityDestroy() {
		super(CLAZZ);
	}

}
