package me.au2001.lightcitizens.packets;

import me.au2001.lightcitizens.tinyprotocol.Reflection;

public class PacketPlayOutRelEntityMove extends Packet {

	private static final Class<?> CLAZZ = Reflection.getMinecraftClass("PacketPlayOutEntity$PacketPlayOutRelEntityMove");
	
	public PacketPlayOutRelEntityMove() {
		super(CLAZZ);
	}
	
}
