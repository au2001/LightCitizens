package me.au2001.lightcitizens.events;

import org.bukkit.entity.Player;

public class FakeEntityRightClickedEvent extends FakeEntityClickedEvent {

	public FakeEntityRightClickedEvent(int entityId, Player player, Object packet) {
		super(entityId, player, Action.INTERACT, packet);
	}

}
