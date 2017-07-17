package me.au2001.lightcitizens.packets;

import me.au2001.lightcitizens.tinyprotocol.Reflection;

public class PacketPlayOutNamedSoundEffect extends Packet {

	private static final Class<?> CLAZZ = Reflection.getMinecraftClass("PacketPlayOutNamedSoundEffect");

	public PacketPlayOutNamedSoundEffect() {
		super(CLAZZ);
	}

}
