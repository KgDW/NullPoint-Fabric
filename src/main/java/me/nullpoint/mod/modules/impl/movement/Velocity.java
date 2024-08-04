package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.asm.accessors.IEntityVelocityUpdateS2CPacket;
import me.nullpoint.asm.accessors.IExplosionS2CPacket;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

public class Velocity extends Module {
	public static Velocity INSTANCE;
	private final SliderSetting horizontal = add(new SliderSetting("Horizontal", 0f, 0f, 100f, 1f));
	private final SliderSetting vertical = add(new SliderSetting("Vertical", 0f, 0f, 100f, 1f));
	public final BooleanSetting waterPush = add(new BooleanSetting("WaterPush", true));
	public final BooleanSetting entityPush = add(new BooleanSetting("EntityPush", true));
	public final BooleanSetting blockPush = add(new BooleanSetting("BlockPush", true));
	public final BooleanSetting noExplosions = add(new BooleanSetting("NoExplosions", true));
	public Velocity() {
		super("Velocity", Category.Movement);
		this.setDescription("Prevents knockback.");
		INSTANCE = this;
	}

	@Override
	public String getInfo() {
		return "H" + horizontal.getValueInt() +".0" + "%," + "V" + vertical.getValueInt() +".0" + "%";
	}

	@EventHandler
	public void onReceivePacket(PacketEvent.Receive event) {
		if (nullCheck()) return;
		float h = horizontal.getValueFloat() / 100;
		float v = vertical.getValueFloat() / 100;

		if (event.getPacket() instanceof EntityStatusS2CPacket packet && (packet = event.getPacket()).getStatus() == 31 && packet.getEntity(mc.world) instanceof FishingBobberEntity fishHook) {
			if (fishHook.getHookedEntity() == mc.player) {
				event.setCancelled(true);
			}
		}

		if (event.getPacket() instanceof ExplosionS2CPacket) {
			IExplosionS2CPacket packet = event.getPacket();

			packet.setX(packet.getX() * h);
			packet.setY(packet.getY() * v);
			packet.setZ(packet.getZ() * h);

			if (noExplosions.getValue()) event.cancel();
			return;
		}

		if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet) {
			if (packet.getId() == mc.player.getId()) {
				if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
					event.cancel();
				} else {
					((IEntityVelocityUpdateS2CPacket) packet).setX((int) (packet.getVelocityX() * h));
					((IEntityVelocityUpdateS2CPacket) packet).setY((int) (packet.getVelocityY() * v));
					((IEntityVelocityUpdateS2CPacket) packet).setZ((int) (packet.getVelocityZ() * h));
				}
			}
		}
	}
}