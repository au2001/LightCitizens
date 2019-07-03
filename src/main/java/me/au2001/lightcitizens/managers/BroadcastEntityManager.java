package me.au2001.lightcitizens.managers;

import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.LightCitizens;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class BroadcastEntityManager extends Manager {
	
	public BroadcastEntityManager(FakeEntity entity) {
		super(entity);
	}

	public void onManagerAdded() {
		for (Player player : Bukkit.getOnlinePlayers()) entity.addObserver(player);
	}

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		new BukkitRunnable() {
			public void run() {
				if (!Bukkit.getOnlinePlayers().contains(event.getPlayer())) return;
				// Delay the NPC loading so that the client has time to render the skin after the entity has spawned.
				// Setting the delay too low would make the client receive the packets during the "login freeze", and
				// wouldn't render the skin before the REMOVE_PLAYER packet is received, preventing the skin to load.
				entity.addObserver(event.getPlayer());
			}
		}.runTaskLater(LightCitizens.getInstance(), LightCitizens.getPingTicks(event.getPlayer()) * 25 + 50);
	}
	
}
