package me.au2001.lightcitizens.managers;

import java.util.HashMap;

import me.au2001.lightcitizens.packets.PacketPlayOutEntityHeadRotation;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.packets.PacketPlayOutRelEntityMoveLook;

public class LookCloseManager extends Manager {

	private HashMap<Player, Byte> yawCache = new HashMap<Player, Byte>();
	private HashMap<Player, Byte> pitchCache = new HashMap<Player, Byte>();
	
	public LookCloseManager(FakeEntity entity) {
		super(entity);
	}
	
	public void tick() {
		for (Player observer : entity.getObservers()) {
			Location orientation = entity.getLocation();
			orientation.setDirection(observer.getLocation().subtract(entity.getLocation()).toVector());
			
			byte yaw = (byte) (orientation.getYaw() * 256.0F / 360.0F); // Between 0 and 256 instead of 0° to 360°
			byte pitch = (byte) (orientation.getPitch() * 128.0F / 180.0F); // Between -64 and +64 instead of -90° to +90°
			
			boolean updateyaw = !yawCache.containsKey(observer) || yawCache.get(observer) != yaw;
			boolean updatepitch = !pitchCache.containsKey(observer) || pitchCache.get(observer) != pitch;
			
			if (updateyaw) {
				PacketPlayOutEntityHeadRotation look = new PacketPlayOutEntityHeadRotation();
				look.set("a", entity.getEntityId());
				look.set("b", yaw);
				look.send(observer);
			}
			
			if (updateyaw || updatepitch) {
				PacketPlayOutRelEntityMoveLook move = new PacketPlayOutRelEntityMoveLook();
				move.set("a", entity.getEntityId());
				move.set("b", (byte) 0);
				move.set("c", (byte) 0);
				move.set("d", (byte) 0);
				move.set("e", yaw);
				move.set("f", pitch);
				move.send(observer);
			}
			
			yawCache.put(observer, yaw);
			pitchCache.put(observer, yaw);
		}
	}
	
	public void onObserverRemoved(Player player) {
		yawCache.remove(player);
		pitchCache.remove(player);
	}

}
