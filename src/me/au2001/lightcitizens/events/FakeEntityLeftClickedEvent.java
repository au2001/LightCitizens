package me.au2001.lightcitizens.events;

import me.au2001.lightcitizens.packets.Packet;
import org.bukkit.entity.Player;

public class FakeEntityLeftClickedEvent extends FakeEntityClickedEvent {

	public FakeEntityLeftClickedEvent(int entityId, Player player, Packet packet) {
		super(entityId, player, Action.ATTACK, packet);
	}

}
