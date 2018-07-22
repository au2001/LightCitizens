package me.au2001.lightcitizens.managers;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.events.FakeEntityDestroyedEvent;
import me.au2001.lightcitizens.events.FakeEntityLeftClickedEvent;
import me.au2001.lightcitizens.events.FakeEntityRightClickedEvent;
import me.au2001.lightcitizens.events.FakeEntitySpawnedEvent;

public abstract class Manager implements Listener {
	
	protected FakeEntity entity;
	
	public Manager(FakeEntity entity) {
		this.entity = entity;
	}
	
	public void tick() {}
	public void syncTick() {}
	public void onManagerAdded() {}
	public void onManagerRemoved() {}
	public void onObserverAdded(Player player) {}
	public void onObserverRemoved(Player player) {}
	
	public void onSpawned(FakeEntitySpawnedEvent event) {}
	public void onDestroyed(FakeEntityDestroyedEvent event) {}
	public void onLeftClicked(FakeEntityLeftClickedEvent event) {}
	public void onRightClicked(FakeEntityRightClickedEvent event) {}
	
}
