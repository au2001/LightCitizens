package me.au2001.lightcitizens.packets;

import me.au2001.lightcitizens.tinyprotocol.Reflection;

public class PacketPlayOutRelEntityMoveLook extends Packet {

	private static final Class<?> CLAZZ = Reflection.getMinecraftClass("PacketPlayOutEntity$PacketPlayOutRelEntityMoveLook");
	
	public PacketPlayOutRelEntityMoveLook() {
		super(CLAZZ);
	}
	
}
