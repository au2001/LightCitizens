package me.au2001.lightcitizens.events;

import me.au2001.lightcitizens.packets.Packet;
import org.bukkit.entity.Player;

public class FakeEntityRightClickedEvent extends FakeEntityClickedEvent {

	public FakeEntityRightClickedEvent(int entityId, Player player, Packet packet) {
		super(entityId, player, Action.INTERACT, packet);
	}

}
