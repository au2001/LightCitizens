package me.au2001.lightcitizens.packets;

import me.au2001.lightcitizens.tinyprotocol.Reflection;

public class PacketPlayOutEntityHeadRotation extends Packet {

	private static final Class<?> CLAZZ = Reflection.getMinecraftClass("PacketPlayOutEntityHeadRotation");

    public PacketPlayOutEntityHeadRotation() {
		super(CLAZZ);
	}

}
