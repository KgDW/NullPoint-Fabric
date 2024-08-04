/**
 * Sprint Module
 */
package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;

public class Sprint extends Module {

	public static Sprint INSTANCE;
	public final EnumSetting<Mode> mode =
			add(new EnumSetting<>("Mode", Mode.Normal));
	public Sprint() {
		super("Sprint", Category.Movement);
		this.setDescription("Permanently keeps player in sprinting mode.");
		INSTANCE = this;
	}

	public static boolean shouldSprint;

	@Override
	public String getInfo() {
		return mode.getValue().name();
	}

	@Override
	public void onUpdate() {
		if (nullCheck()) return;
		switch (mode.getValue()) {
			case Legit -> {
				mc.options.sprintKey.setPressed(true);
				shouldSprint = false;
			}
			case Normal -> {
				mc.options.sprintKey.setPressed(true);
				shouldSprint = false;
				if (mc.player.getHungerManager().getFoodLevel() <= 6 && !mc.player.isCreative()) return;
				mc.player.setSprinting(MovementUtil.isMoving() && !mc.player.isSneaking());
			}
			case Rage -> {
				shouldSprint = (mc.player.getHungerManager().getFoodLevel() > 6 || mc.player.isCreative()) && !mc.player.isSneaking();
				mc.player.setSprinting(shouldSprint);
			}
		}
	}

	public enum Mode {
		Legit,
		Normal,
		Rage
	}
}
