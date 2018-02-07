package me.au2001.lightcitizens.packets;

import me.au2001.lightcitizens.tinyprotocol.Reflection;

public class PacketPlayOutScoreboardScore extends Packet {

	private static Class<?> SCOREBOARD_ACTION;

	private static final Class<?> CLAZZ = Reflection.getMinecraftClass("PacketPlayOutScoreboardScore");

	static {
		SCOREBOARD_ACTION = Reflection.getMinecraftClass("PacketPlayOutScoreboardScore$EnumScoreboardAction");
	}

	public PacketPlayOutScoreboardScore() {
		super(CLAZZ);
	}

	public static class EnumScoreboardAction {

		public static Object CHANGE, REMOVE;

		static {
			try {
				CHANGE = SCOREBOARD_ACTION.getDeclaredField("CHANGE").get(null);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}

			try {
				REMOVE = SCOREBOARD_ACTION.getDeclaredField("REMOVE").get(null);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}

	}
	
}
