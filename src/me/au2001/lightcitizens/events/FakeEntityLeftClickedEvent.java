package me.au2001.lightcitizens.events;

import org.bukkit.entity.Player;

public class FakeEntityLeftClickedEvent extends FakeEntityClickedEvent {

	public FakeEntityLeftClickedEvent(int entityId, Player player, Object packet) {
		super(entityId, player, Action.ATTACK, packet);
	}

}
