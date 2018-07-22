package me.au2001.lightcitizens.managers;

import com.mojang.authlib.GameProfile;
import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.LightCitizens;
import me.au2001.lightcitizens.packets.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class SkinFixerManager extends Manager {

	private static Class<?> PLAYER_INFO_ACTION;
	private static Constructor<?> PLAYER_INFO_DATA;
	private static Method CHAT_SERIALIZER, ENUM_NAME, GET_GAMEMODE;
	private static Field PROFILE, TABLISTTIME;

	static {
		try {
			Field playerInfoAction = FakeEntity.class.getDeclaredField("PLAYER_INFO_ACTION");
			playerInfoAction.setAccessible(true);
			PLAYER_INFO_ACTION = (Class<?>) playerInfoAction.get(null);

			Field playerInfoData = FakeEntity.class.getDeclaredField("PLAYER_INFO_DATA");
			playerInfoData.setAccessible(true);
			PLAYER_INFO_DATA = (Constructor<?>) playerInfoData.get(null);

			Field chatSerializer = FakeEntity.class.getDeclaredField("CHAT_SERIALIZER");
			chatSerializer.setAccessible(true);
			CHAT_SERIALIZER = (Method) chatSerializer.get(null);

			Field enumName = FakeEntity.class.getDeclaredField("ENUM_NAME");
			enumName.setAccessible(true);
			ENUM_NAME = (Method) enumName.get(null);

			Field getGamemode = FakeEntity.class.getDeclaredField("GET_GAMEMODE");
			getGamemode.setAccessible(true);
			GET_GAMEMODE = (Method) getGamemode.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}

		try {
			PROFILE = FakeEntity.class.getDeclaredField("profile");
			PROFILE.setAccessible(true);

			TABLISTTIME = FakeEntity.class.getDeclaredField("tablisttime");
			TABLISTTIME.setAccessible(true);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	private long delay = 5 * 20;
	private long tick = 0;

	private GameProfile profile = null;
	private HashMap<Player, Long> tablisttime = null;

	public SkinFixerManager(FakeEntity entity) {
		super(entity);

		try {
			profile = (GameProfile) PROFILE.get(entity);

			tablisttime = (HashMap<Player, Long>) TABLISTTIME.get(entity);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void onManagerAdded() {
		for (Player player : Bukkit.getOnlinePlayers()) entity.addObserver(player);
	}

	public void tick() {
		if (++tick >= delay) tick = 0;
		if (entity.getEntityId() % delay == tick) fixSkin();
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public void fixSkin() {
		fixSkin(entity.getVisibleObservers());
	}

	public void fixSkin(Player player) {
		fixSkin(Arrays.asList(player));
	}

	public void fixSkin(List<Player> players) {
		for (Player player : new ArrayList<Player>(players))
			if (tablisttime.containsKey(player)) players.remove(player);

		try {
			PacketPlayOutPlayerInfo info = new PacketPlayOutPlayerInfo();
			for (Object action : PLAYER_INFO_ACTION.getEnumConstants()) {
				if (ENUM_NAME.invoke(action).equals("ADD_PLAYER")) {
					info.set("a", action);
					break;
				}
			}
			List<Object> data = (List<Object>) info.get("b");
			String json = entity.getPlayerListName() != null? "{\"text\":\"" + entity.getPlayerListName().replace("\\", "\\\\").replace("\"", "\\\"") + "\"}" : "{\"text\":\"\"}";
			data.add(PLAYER_INFO_DATA.newInstance(info.toPacket(), profile, entity.getPing(), GET_GAMEMODE.invoke(null, entity.getGameMode().getValue()), CHAT_SERIALIZER.invoke(null, json)));
			// info.set("b", data);
			for (Player player : players) info.send(player);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}

		if (entity.getPlayerListName() == null) {
			for (Player player : players)
				tablisttime.put(player, LightCitizens.getTickTime() + LightCitizens.getPingTicks(player) * 2 + 5);
		}
	}

}
