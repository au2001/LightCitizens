package me.au2001.lightcitizens.managers;

import io.netty.channel.Channel;
import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.packets.Packet;
import me.au2001.lightcitizens.packets.Packet.PacketListener;
import me.au2001.lightcitizens.packets.PacketPlayOutEntityHeadRotation;
import me.au2001.lightcitizens.packets.PacketPlayOutEntityLook;
import me.au2001.lightcitizens.tinyprotocol.Reflection;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class LookCloseManager extends Manager {

	private PacketListener listener;
	private HashMap<Player, Byte> yawCache = new HashMap<Player, Byte>();
	private HashMap<Player, Byte> pitchCache = new HashMap<Player, Byte>();
	
	public LookCloseManager(FakeEntity entity) {
		super(entity);

		listener = new PacketListener() {

			private final Class<?> entityPacket = Reflection.getMinecraftClass("PacketPlayOutEntity");
			private final Class<?> entityHeadRotationPacket = Reflection.getMinecraftClass("PacketPlayOutEntityHeadRotation");

			public void onPacketOutAsync(Player receiver, Channel channel, Packet packet) {
				if (entityPacket != null && entityPacket.isAssignableFrom(packet.toPacket().getClass())) {
					if ((int) packet.get("a") == LookCloseManager.this.entity.getEntityId()) {
						Location orientation = LookCloseManager.this.entity.getLocation();
						orientation.setDirection(receiver.getLocation().subtract(orientation).toVector());

						byte yaw = (byte) (orientation.getYaw() * 256.0F / 360.0F); // Between 0 and 256 instead of 0° to 360°
						byte pitch = (byte) (orientation.getPitch() * 128.0F / 180.0F); // Between -64 and +64 instead of -90° to +90°

						packet.set("e", yaw);
						packet.set("f", pitch);

						yawCache.put(receiver, yaw);
						pitchCache.put(receiver, pitch);
					}
				} else if (entityHeadRotationPacket != null && entityHeadRotationPacket.isAssignableFrom(packet.toPacket().getClass())) {
					if ((int) packet.get("a") == LookCloseManager.this.entity.getEntityId()) {
						Location orientation = LookCloseManager.this.entity.getLocation();
						orientation.setDirection(receiver.getLocation().subtract(orientation).toVector());

						packet.set("b", (byte) (orientation.getYaw() * 256.0F / 360.0F));
					}
				}
			}

		};
	}

	public void onManagerAdded() {
		listener.register();
	}

	public void onManagerRemoved() {
		listener.unregister();
	}
	
	public void syncTick() {
		for (Player observer : entity.getVisibleObservers()) {
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
				PacketPlayOutEntityLook move = new PacketPlayOutEntityLook();
				move.set("a", entity.getEntityId());
				move.set("e", yaw);
				move.set("f", pitch);
				move.set("g", true);
				move.send(observer);
			}
			
			yawCache.put(observer, yaw);
			pitchCache.put(observer, pitch);
		}
	}
	
	public void onObserverRemoved(Player player) {
		yawCache.remove(player);
		pitchCache.remove(player);
	}

}
