/**
 * NoSlowdown Module
 */
package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class NoSlow extends Module {
	public static NoSlow INSTANCE;
	private final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Vanilla));
	public NoSlow() {
		super("NoSlow", Category.Movement);
		INSTANCE = this;
	}

	@Override
	public String getInfo() {
		return mode.getValue().name();
	}

	@Override
	public void onUpdate() {
		if (mode.getValue() == Mode.NCP && mc.player.isUsingItem() && !mc.player.isRiding() && !mc.player.isFallFlying()) {
			mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
		}
	}

	public enum Mode {
		Vanilla,
		NCP
	}
}
