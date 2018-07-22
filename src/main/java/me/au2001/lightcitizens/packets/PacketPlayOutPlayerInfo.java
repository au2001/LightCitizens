package me.au2001.lightcitizens.packets;

import me.au2001.lightcitizens.tinyprotocol.Reflection;

public class PacketPlayOutPlayerInfo extends Packet {

	private static final Class<?> CLAZZ = Reflection.getMinecraftClass("PacketPlayOutPlayerInfo");
	
	public PacketPlayOutPlayerInfo() {
		super(CLAZZ);
	}

}
