package me.au2001.lightcitizens;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import me.au2001.lightcitizens.events.FakeEntityDestroyedEvent;
import me.au2001.lightcitizens.events.FakeEntityLeftClickedEvent;
import me.au2001.lightcitizens.events.FakeEntityRightClickedEvent;
import me.au2001.lightcitizens.events.FakeEntitySpawnedEvent;
import me.au2001.lightcitizens.managers.Manager;
import me.au2001.lightcitizens.packets.*;
import me.au2001.lightcitizens.tinyprotocol.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

public class FakeEntity extends Random implements Listener {

	static List<FakeEntity> entities = new ArrayList<FakeEntity>();

	private static Field ENTITY_ID;
	private static Class<?> PLAYER_INFO_ACTION;
	private static Constructor<?> DATA_WATCHER, PLAYER_INFO_DATA;
	private static Method CHAT_SERIALIZER, ENUM_NAME, GET_GAMEMODE;

	private HashMap<Player, Long> tablisttime = new HashMap<Player, Long>();
	private HashMap<Class<? extends Manager>, Manager> managers = new HashMap<Class<? extends Manager>, Manager>();
	private List<Player> observers = new ArrayList<Player>();
	private Location clientLocation;
	private Location serverLocation;

	private int entityId;
	private String name;
	private UUID uuid;
	private GameProfile profile;
	private FakeEntityEquipment equipment;
	private int ping;
	private String playerListName;
	private GameMode gamemode;
	private Object dataWatcher;
	private double viewDistance;

	boolean changed;

	static {
		try {
			Class<?> entity = Reflection.getMinecraftClass("Entity");
			Class<?> gamemode = Reflection.getMinecraftClass("WorldSettings$EnumGamemode");

			ENTITY_ID = entity.getDeclaredField("entityCount");
			ENTITY_ID.setAccessible(true);

			DATA_WATCHER = Reflection.getMinecraftClass("DataWatcher").getConstructor(entity);

			CHAT_SERIALIZER = Reflection.getMinecraftClass("IChatBaseComponent$ChatSerializer").getMethod("a", String.class);

			PLAYER_INFO_ACTION = Reflection.getMinecraftClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");

			Class<?> info = Reflection.getMinecraftClass("PacketPlayOutPlayerInfo");
			Class<?> data = Reflection.getMinecraftClass("PacketPlayOutPlayerInfo$PlayerInfoData");
			Class<?> component = Reflection.getMinecraftClass("IChatBaseComponent");
			PLAYER_INFO_DATA = data.getConstructor(info, GameProfile.class, int.class, gamemode, component);

            ENUM_NAME = Enum.class.getMethod("name");

			GET_GAMEMODE = gamemode.getMethod("getById", int.class);
		} catch (NoSuchFieldException | SecurityException | NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

    public FakeEntity(Plugin plugin, Location location, String name) {
		this(plugin, location, name, null, null);
	}

	public FakeEntity(Plugin plugin, Location location, String name, UUID uuid) {
		this(plugin, location, name, uuid, null);
	}

	public FakeEntity(Plugin plugin, Location location, String name, List<Class<Manager>> managers) {
		this(plugin, location, name, null, managers);
	}

	public FakeEntity(Plugin plugin, Location location, String name, UUID uuid, List<Class<Manager>> managers) {
		if (plugin == null) throw new IllegalArgumentException("plugin can't be null");
		if (location == null) throw new IllegalArgumentException("location can't be null");
		if (name == null) throw new IllegalArgumentException("name can't be null");

		if (uuid == null) uuid = UUID.randomUUID();
		if (managers == null) managers = new ArrayList<Class<Manager>>();

		this.clientLocation = location.clone();
		this.serverLocation = location.clone();
		try {
			this.entityId = (Integer) ENTITY_ID.get(null);
			ENTITY_ID.set(null, entityId + 1);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		this.name = name;
		this.uuid = uuid;
		this.profile = new GameProfile(uuid, name);
		this.equipment = new FakeEntityEquipment(this, null, null, null, null, null);
		this.ping = 0;
		this.playerListName = name;
		this.gamemode = GameMode.CREATIVE;
		try {
			this.dataWatcher = DATA_WATCHER.newInstance(new Object[] { null });
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		this.viewDistance = 48;

		for (Class<Manager> manager : managers) addManager(manager, plugin);
		Bukkit.getPluginManager().registerEvents(this, plugin);

		Bukkit.getPluginManager().callEvent(new FakeEntitySpawnedEvent(this));
		
		entities.add(this);
	}

	public void update() {
		if (changed) {
			PropertyMap properties = profile.getProperties();
			profile = new GameProfile(uuid, name);
			for (Entry<String, Property> property : properties.entries())
				profile.getProperties().put(property.getKey(), property.getValue());
			showEntity();
			clientLocation = serverLocation.clone();
			changed = false;
			return;
		}

		if (!serverLocation.equals(clientLocation)) {
			PacketPlayOutEntityTeleport move = new PacketPlayOutEntityTeleport();
			move.set("a", entityId);
			move.set("b", (int) (serverLocation.getX() * 32.0D));
			move.set("c", (int) (serverLocation.getY() * 32.0D));
			move.set("d", (int) (serverLocation.getZ() * 32.0D));
			move.set("e", (byte) (serverLocation.getYaw() * 256.0F / 360.0F));
			move.set("f", (byte) (serverLocation.getPitch() * 256.0F / 360.0F));
			for (Player player : getVisibleObservers()) move.send(player);

			PacketPlayOutEntityHeadRotation look = new PacketPlayOutEntityHeadRotation();
			look.set("a", entityId);
			look.set("b", (byte) (serverLocation.getYaw() * 256.0F / 360.0F));
			for (Player player : getVisibleObservers()) look.send(player);

			clientLocation = serverLocation.clone();
		}

		List<Player> players = new ArrayList<Player>();
		for (Entry<Player, Long> entry : tablisttime.entrySet())
			if (entry.getValue() >= LightCitizens.getTickTime()) players.add(entry.getKey());
		if (!players.isEmpty()) {
			try {
				PacketPlayOutPlayerInfo info = new PacketPlayOutPlayerInfo();
				for (Object action : PLAYER_INFO_ACTION.getEnumConstants()) {
					if (ENUM_NAME.invoke(action).equals("REMOVE_PLAYER")) {
						info.set("a", action);
						break;
					}
				}
				List<Object> data = (List<Object>) info.get("b");
				data.add(PLAYER_INFO_DATA.newInstance(info.toPacket(), profile, ping, GET_GAMEMODE.invoke(null, gamemode.getValue()), CHAT_SERIALIZER.invoke(null, "{\"text\":\"\"}")));
				// info.set("b", data);
				for (Player player : players) {
					info.send(player);
					tablisttime.remove(player);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
				e.printStackTrace();
			}
		}
	}

	private void showEntity() {
		showEntity(getVisibleObservers());
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	private void showEntity(List<Player> players) {
		for (Player player : players)
			if (tablisttime.containsKey(player)) tablisttime.remove(player);

		try {
			PacketPlayOutPlayerInfo info = new PacketPlayOutPlayerInfo();
			for (Object action : PLAYER_INFO_ACTION.getEnumConstants()) {
				if (ENUM_NAME.invoke(action).equals("ADD_PLAYER")) {
					info.set("a", action);
					break;
				}
			}
			List<Object> data = (List<Object>) info.get("b");
			String json = "{\"text\":\"" + (playerListName != null? playerListName.replace("\\", "\\\\").replace("\"", "\\\"") : "") + "\"}";
			data.add(PLAYER_INFO_DATA.newInstance(info.toPacket(), profile, ping, GET_GAMEMODE.invoke(null, gamemode.getValue()), CHAT_SERIALIZER.invoke(null, json)));
			// info.set("b", data);
			for (Player player : players) info.send(player);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}

		PacketPlayOutNamedEntitySpawn spawned = new PacketPlayOutNamedEntitySpawn();
		spawned.set("a", entityId);
		spawned.set("b", uuid);
		spawned.set("c", (int) (serverLocation.getX() * 32.0D));
		spawned.set("d", (int) (serverLocation.getY() * 32.0D));
		spawned.set("e", (int) (serverLocation.getZ() * 32.0D));
		spawned.set("f", (byte) ((int) (serverLocation.getYaw() * 256.0F / 360.0F)));
		spawned.set("g", (byte) ((int) (serverLocation.getPitch() * 256.0F / 360.0F)));
		// spawned.set("h", equipment.hasItemInHand()? equipment.getItemInHand().getTypeId() : 0);
		spawned.set("i", dataWatcher);
		for (Player player : players) spawned.send(player);

		for (Player player : players) equipment.send(player);
		
		if (playerListName == null) {
			for (Player player : players)
				tablisttime.put(player, LightCitizens.getTickTime() + LightCitizens.getPingTicks(player) * 2 + 5);
		}
	}

	private void hideEntity() {
		hideEntity(observers);
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	private void hideEntity(List<Player> players) {
		for (Player player : players)
			if (tablisttime.containsKey(player)) tablisttime.remove(player);

		try {
			PacketPlayOutPlayerInfo info = new PacketPlayOutPlayerInfo();
			for (Object action : PLAYER_INFO_ACTION.getEnumConstants()) {
				if (ENUM_NAME.invoke(action).equals("REMOVE_PLAYER")) {
					info.set("a", action);
					break;
				}
			}
			List<Object> data = (List<Object>) info.get("b");
			data.add(PLAYER_INFO_DATA.newInstance(info.toPacket(), profile, ping, GET_GAMEMODE.invoke(null, gamemode.getValue()), CHAT_SERIALIZER.invoke(null, "{\"text\":\"\"}")));
			// info.set("b", data);
			for (Player player : players) info.send(player);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy();
		destroy.set("a", new int[] { entityId });
		for (Player player : players) destroy.send(player);
	}

	public void destroy() {
		destroy(false);
	}

    public void destroy(boolean animation) {
	    if (animation) {
	    	FakeEntityEquipment equipment = new FakeEntityEquipment(this, null, null, null, null, null);
	    	for (Player observer : getVisibleObservers()) equipment.send(observer);

            PacketPlayOutNamedSoundEffect sound = new PacketPlayOutNamedSoundEffect();
            sound.set("a", "game.player.die");
            sound.set("b", (int) (serverLocation.getX() * 8));
            sound.set("c", (int) (serverLocation.getY() * 8));
            sound.set("d", (int) (serverLocation.getZ() * 8));
            sound.set("e", 1F);
            sound.set("f", (int) (((nextFloat() - nextFloat()) * 0.2F + 1.0F) * 63.0F));
            for (Player observer : getVisibleObservers()) sound.send(observer);

            PacketPlayOutEntityStatus status = new PacketPlayOutEntityStatus();
            status.set("a", entityId);
            status.set("b", (byte) 3);
            for (Player observer : getVisibleObservers()) status.send(observer);

	        new BukkitRunnable() {
                public void run() {
                    for (Player player : getObservers()) removeObserver(player);
                }
            }.runTaskLater(LightCitizens.getInstance(), 20);
        } else for (Player player : getObservers()) removeObserver(player);

        Bukkit.getPluginManager().callEvent(new FakeEntityDestroyedEvent(this));
        for (Class<? extends Manager> manager : new ArrayList<Class<? extends Manager>>(managers.keySet())) removeManager(manager);

        HandlerList.unregisterAll(this);
        entities.remove(this);
    }

    @SuppressWarnings("unchecked")
	public Map<Class<? extends Manager>, Manager> getManagers() {
		return Collections.unmodifiableMap((HashMap<Class<? extends Manager>, Manager>) managers.clone());
	}

	public boolean hasManager(Class<? extends Manager> clazz) {
		return managers.containsKey(clazz);
	}

	@SuppressWarnings("unchecked")
	public <T extends Manager> T getManager(Class<T> clazz) {
		return (T) managers.get(clazz);
	}

	@SuppressWarnings("unchecked")
	public <T extends Manager> T addManager(Class<T> clazz, Plugin plugin) {
		if (managers.containsKey(clazz)) return (T) managers.get(clazz);

		try {
			synchronized (this) {
				Constructor<T> constructor = clazz.getConstructor(getClass());
				T manager = constructor.newInstance(this);
				managers.put(clazz, manager);
				manager.onManagerAdded();
				Bukkit.getPluginManager().registerEvents(manager, plugin);
				return manager;
			}
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Manager> T removeManager(Class<T> clazz) {
		if (!managers.containsKey(clazz)) return null;

        synchronized (this) {
            Manager manager = managers.remove(clazz);
            if (manager == null) return null;
            manager.onManagerRemoved();
            HandlerList.unregisterAll(manager);
            return (T) manager;
        }
	}

	public List<Player> getObservers() {
		return Collections.unmodifiableList(new ArrayList<Player>(observers));
	}

	public boolean isObserver(Player player) {
		return observers.contains(player);
	}

	public void addObserver(Player player) {
		if (observers.contains(player)) return;

		if (serverLocation != null && serverLocation.getWorld() != null && serverLocation.getChunk() != null && serverLocation.getChunk().isLoaded())
			if (serverLocation.getWorld().equals(player.getWorld()) && (viewDistance <= 0 || serverLocation.distanceSquared(player.getLocation()) <= viewDistance * viewDistance))
				showEntity(Arrays.asList(player));

        synchronized (this) {
            observers.add(player);
            for (Manager manager : new ArrayList<Manager>(managers.values())) manager.onObserverAdded(player);
        }
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	public void removeObserver(Player player) {
		if (!observers.contains(player)) return;
		hideEntity(Arrays.asList(player));

        synchronized (this) {
            observers.remove(player);
            for (Manager manager : new ArrayList<Manager>(managers.values())) manager.onObserverRemoved(player);
        }
	}

	public List<Player> getVisibleObservers() {
		if (serverLocation == null || serverLocation.getWorld() == null || serverLocation.getChunk() == null) // || !serverLocation.getChunk().isLoaded()
			return new ArrayList<Player>();

		List<Player> observers = new ArrayList<Player>();
		for (Player player : this.observers) {
			if (!serverLocation.getWorld().equals(player.getWorld())) continue;
			if (viewDistance > 0 && serverLocation.distanceSquared(player.getLocation()) > viewDistance * viewDistance) continue;
			observers.add(player);
		}
		return observers;
	}

	public int getEntityId() {
		return entityId;
	}

	public Location getLocation() {
		return serverLocation.clone();
	}

	public void setLocation(Location location) {
		this.serverLocation = location.clone();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null) throw new IllegalArgumentException("name can't be null");
		if (this.name.equals(this.playerListName)) this.playerListName = this.name;
		this.name = name;
		this.changed = true;
	}

	public UUID getUUID() {
		return uuid;
	}

	public void setUUID(UUID uuid) {
		if (uuid == null) throw new IllegalArgumentException("name can't be null");
		this.uuid = uuid;
		this.changed = true;
	}

	public String getPlayerListName() {
		return playerListName;
	}

	public void setPlayerListName(String name) {
		this.playerListName = name;
		this.changed = true;
	}

	public FakeEntityEquipment getEquipment() {
		return equipment;
	}

	public ItemStack getItemInHand() {
		return equipment.getItemInHand();
	}

	public void setItemInHand(ItemStack item) {
		equipment.setItemInHand(item);
	}

	public ItemStack getHelmet() {
		return  equipment.getHelmet();
	}

	public void setHelmet(ItemStack item) {
		equipment.setHelmet(item);
	}

	public ItemStack getChestplate() {
		return  equipment.getChestplate();
	}

	public void setChestplate(ItemStack item) {
		equipment.setChestplate(item);
	}

	public ItemStack getLeggings() {
		return  equipment.getLeggings();
	}

	public void setLeggings(ItemStack item) {
		equipment.setLeggings(item);
	}

	public ItemStack getBoots() {
		return  equipment.getBoots();
	}

	public void setBoots(ItemStack item) {
		equipment.setBoots(item);
	}

	public int getPing() {
		return ping;
	}

	public void setPing(int ping) {
		if (ping < 0) throw new IllegalArgumentException("ping can't be negative");
		this.ping = ping;
		this.changed = true;
	}

	public GameMode getGameMode() {
		return gamemode;
	}

	public void setGameMode(GameMode gamemode) {
		if (gamemode == null) throw new IllegalArgumentException("gamemode can't be null");
		this.gamemode = gamemode;
		this.changed = true;
	}

	public double getViewDistance() {
		return viewDistance;
	}

	public void setViewDistance(double viewDistance) {
		if ((this.viewDistance <= 0 || viewDistance < this.viewDistance) && viewDistance > 0) {
			List<Player> players = new ArrayList<Player>();
			for (Player player : getVisibleObservers()) {
				double distance = serverLocation.distanceSquared(player.getLocation());
				if (distance > viewDistance * viewDistance) players.add(player);
			}
			hideEntity(players);
			this.viewDistance = viewDistance;
		} else if ((viewDistance <= 0 || viewDistance > this.viewDistance) && this.viewDistance > 0) {
			this.viewDistance = viewDistance;
			List<Player> players = new ArrayList<Player>();
			for (Player player : getVisibleObservers()) {
				double distance = serverLocation.distanceSquared(player.getLocation());
				if (distance > this.viewDistance * this.viewDistance) players.add(player);
			}
			showEntity(players);
		} else {
			this.viewDistance = viewDistance;
		}
	}

	public void setSkin(String data, String signature) {
		profile.getProperties().removeAll("textures");
		if (data != null) {
			Property property = new Property("textures", data, signature);
			profile.getProperties().put(property.getName(), property);
		}
		changed = true;
	}

	@EventHandler
	public void onFakeEntitySpawned(FakeEntitySpawnedEvent event) {
		if (event.getEntityId() != entityId) return;

		for (Manager manager : new ArrayList<Manager>(managers.values())) manager.onSpawned(event);
	}

	@EventHandler
	public void onFakeEntityDestroyed(FakeEntityDestroyedEvent event) {
		if (event.getEntityId() != entityId) return;

		for (Manager manager : new ArrayList<Manager>(managers.values())) manager.onDestroyed(event);
	}

	@EventHandler
	public void onFakeEntityLeftClicked(FakeEntityLeftClickedEvent event) {
		if (event.getEntityId() != entityId) return;

		for (Manager manager : new ArrayList<Manager>(managers.values())) manager.onLeftClicked(event);
	}

	@EventHandler
	public void onFakeEntityRightClicked(FakeEntityRightClickedEvent event) {
		if (event.getEntityId() != entityId) return;

		for (Manager manager : new ArrayList<Manager>(managers.values())) manager.onRightClicked(event);
	}

    @SuppressWarnings({ "unchecked", "deprecation" })
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (!observers.contains(player)) return;
	    hideEntity(Arrays.asList(player));
	}

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        if (!observers.contains(event.getPlayer())) return;

        new BukkitRunnable() {
            public void run() {
                // Delay the NPC loading so that the client has time to render the skin after the entity has spawned.
                // Setting the delay too low would make the client receive the packets during the "login freeze", and
                // wouldn't render the skin before the REMOVE_PLAYER packet is received, preventing the skin to load.
	            if (serverLocation == null || serverLocation.getWorld() == null || serverLocation.getChunk() == null || !serverLocation.getChunk().isLoaded()) return;
	            if (!serverLocation.getWorld().equals(event.getPlayer().getWorld())) return;
	            if (viewDistance > 0 && serverLocation.distanceSquared(event.getPlayer().getLocation()) > viewDistance * viewDistance) return;
	            showEntity(Arrays.asList(event.getPlayer()));
            }
        }.runTaskLater(LightCitizens.getInstance(), LightCitizens.getPingTicks(event.getPlayer()) * 5 + 10);
    }

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		removeObserver(event.getPlayer());
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		removeObserver(event.getPlayer());
	}

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (serverLocation.getChunk().equals(event.getChunk())) hideEntity();
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        if (serverLocation.getWorld().equals(event.getWorld())) hideEntity();
    }

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		if (serverLocation.getChunk().equals(event.getChunk())) showEntity();
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		if (serverLocation.getWorld().equals(event.getWorld())) showEntity();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (viewDistance <= 0) return;
		if (serverLocation == null || serverLocation.getWorld() == null || serverLocation.getChunk() == null || !serverLocation.getChunk().isLoaded()) return;
		if (!observers.contains(event.getPlayer()));

		if (!serverLocation.getWorld().equals(event.getPlayer().getWorld())) return;

		boolean from = serverLocation.distanceSquared(event.getFrom()) <= viewDistance * viewDistance;
		boolean to = serverLocation.distanceSquared(event.getTo()) <= viewDistance * viewDistance;

		if (from && !to) hideEntity(Arrays.asList(event.getPlayer()));
		else if (!from && to) showEntity(Arrays.asList(event.getPlayer()));
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (serverLocation == null || serverLocation.getWorld() == null || serverLocation.getChunk() == null || !serverLocation.getChunk().isLoaded()) return;
		if (!observers.contains(event.getPlayer()));

		boolean from = serverLocation.getWorld().equals(event.getFrom().getWorld()) && (viewDistance <= 0 || serverLocation.distanceSquared(event.getFrom()) <= viewDistance * viewDistance);
		boolean to = serverLocation.getWorld().equals(event.getTo().getWorld()) && (viewDistance <= 0 || serverLocation.distanceSquared(event.getTo()) <= viewDistance * viewDistance);

		if (from && !to) hideEntity(Arrays.asList(event.getPlayer()));
		else if (!from && to) showEntity(Arrays.asList(event.getPlayer()));
	}

	@EventHandler
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		if (serverLocation == null || serverLocation.getWorld() == null || serverLocation.getChunk() == null || !serverLocation.getChunk().isLoaded()) return;
		if (!observers.contains(event.getPlayer()));

		boolean from = serverLocation.getWorld().equals(event.getFrom());
		boolean to = serverLocation.getWorld().equals(event.getPlayer().getWorld()) && (viewDistance <= 0 || serverLocation.distanceSquared(event.getPlayer().getLocation()) <= viewDistance * viewDistance);

		if (from && !to) hideEntity(Arrays.asList(event.getPlayer()));
		else if (!from && to) showEntity(Arrays.asList(event.getPlayer()));
	}

	public boolean hasLineOfSight(Entity entity) {
		if (entity instanceof LivingEntity) return hasLineOfSight((LivingEntity) entity);
		else return hasLineOfSight(entity.getLocation());
	}

	public boolean hasLineOfSight(LivingEntity entity) {
		return hasLineOfSight(entity.getEyeLocation());
	}

	public boolean hasLineOfSight(Location location) {
		if (location == null || !this.serverLocation.getWorld().equals(location.getWorld())) return false;
		location = location.clone();

		Location current = this.serverLocation.clone().add(0, 1.62, 0); // Eye height (1.54 while sneaking)
		Vector direction = location.toVector().subtract(current.toVector());
		Block target = location.getBlock(), block = current.getBlock();

		double distance = current.distance(location);
		if (distance <= 0) return true;

		double precision;
		if (distance > 1000) precision = 5.0;
		else if (distance > 500) precision = 2.0;
		else if (distance > 100) precision = 1.0;
		else if (distance > 50) precision = 0.2;
		else if (distance > 10) precision = 0.1;
		else if (distance > 1) precision = 0.02;
		else precision = 0.002;

		direction = direction.multiply(precision / distance);
		for (int i = 0; i < distance / precision; i++) {
			if (block.getType().isOccluding()) return false;
			location.add(direction);
			block = location.getBlock();
		}

		return true;
	}

}
