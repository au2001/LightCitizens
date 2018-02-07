package me.au2001.lightcitizens.events;

import me.au2001.lightcitizens.packets.Packet;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class FakeEntityClickedEvent extends FakeEntityEvent implements Cancellable {
	
	private boolean cancelled = false;
	private Action action;
	private Player player;
	private Object packet;
	
	public FakeEntityClickedEvent(int entityId, Player player, Action action, Packet packet) {
		super(entityId);
		this.player = player;
		this.action = action;
		this.packet = packet;
	}
	
	public Player getPlayer() {
		return player;
	}

	public boolean isAttack() {
		return action.equals(Action.ATTACK);
	}

	public boolean isInteract() {
		return action.equals(Action.INTERACT);
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public static enum Action {
		ATTACK,
		INTERACT;
	}

}
