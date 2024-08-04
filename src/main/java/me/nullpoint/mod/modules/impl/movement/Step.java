/**
 * Step Module
 */
package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.combat.HoleKick;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import me.nullpoint.api.utils.entity.EntityUtil;

public class Step extends Module {

	private final SliderSetting stepHeight = add(new SliderSetting("Height", 1, 0.0, 5, 0.5));
	public final BooleanSetting onlyMoving =
			add(new BooleanSetting("OnlyMoving", true));

	public static Step INSTANCE;
	public Step() {
		super("Step", Category.Movement);
		INSTANCE = this;
	}

	@Override
	public void onDisable() {
		if (nullCheck()) return;
		mc.player.setStepHeight(0.6f);
	}

	@Override
	public void onUpdate() {
		if (nullCheck()) return;
		if (mc.player.isSneaking() || !mc.player.horizontalCollision || mc.player.isInLava() || mc.player.isTouchingWater() || !mc.player.isOnGround() ||(!EntityUtil.isInsideBlock() && HoleKick.isInWeb(mc.player))) {
			mc.player.setStepHeight(0.6f);
			return;
		}
		mc.player.setStepHeight(stepHeight.getValueFloat());
	}
}
