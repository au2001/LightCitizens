package me.au2001.lightcitizens.packets;

import me.au2001.lightcitizens.tinyprotocol.Reflection;

public class PacketPlayOutAnimation extends Packet {

    private static final Class<?> CLAZZ = Reflection.getMinecraftClass("PacketPlayOutAnimation");

    public PacketPlayOutAnimation() {
        super(CLAZZ);
    }

}