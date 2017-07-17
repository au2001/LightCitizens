package me.au2001.lightcitizens.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.LightCitizens;

public class BroadcastEntityManager extends Manager {

	private int viewDistance = 48;
	
	public BroadcastEntityManager(FakeEntity entity) {
		super(entity);
	}
	
	public int getViewDistance() {
		return viewDistance;
	}
	
	public void setViewDistance(int viewDistance) {
		this.viewDistance = viewDistance;
	}

	public void onManagerAdded() {
		for (Player player : Bukkit.getOnlinePlayers()) updatePlayer(player);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		new BukkitRunnable() {
			public void run() {
				// Delay the NPC loading so that the client has time to render the skin after the entity has spawned.
				// Setting the delay too low would make the client receive the packets during the "login freeze", and
				// wouldn't render the skin before the REMOVE_PLAYER packet is received, preventing the skin to load.
				updatePlayer(event.getPlayer());
			}
		}.runTaskLater(LightCitizens.getInstance(), 5*20);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		updatePlayer(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
		updatePlayer(event.getPlayer());
	}
	
	private void updatePlayer(Player player) {
		if (entity.getLocation().getWorld() != null && !player.getWorld().equals(entity.getLocation().getWorld())) {
			entity.removeObserver(player);
			return;
		}
		if (entity.getLocation().distanceSquared(entity.getLocation()) > viewDistance*viewDistance) {
			entity.removeObserver(player);
			return;
		}
		entity.addObserver(player);
	}
	
}
