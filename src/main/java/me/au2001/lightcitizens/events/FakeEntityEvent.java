package me.au2001.lightcitizens.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FakeEntityEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	
	private int entityId;
	
	public FakeEntityEvent(int entityId) {
		this.entityId = entityId;
	}
	
	public int getEntityId() {
		return entityId;
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
