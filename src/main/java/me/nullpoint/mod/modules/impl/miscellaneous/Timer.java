package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.Nullpoint;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class Timer extends Module {
	public final SliderSetting multiplier = add(new SliderSetting("Speed", 1, 0.1, 5, 0.01));
	public static Timer INSTANCE;
	public Timer() {
		super("Timer", Category.Misc);
		this.setDescription("Increases the speed of Minecraft.");
		INSTANCE = this;
	}

	@Override
	public void onDisable() {
		Nullpoint.TIMER.reset();
	}

	@Override
	public void onUpdate() {
		Nullpoint.TIMER.tryReset();
	}

	@Override
	public void onEnable() {
		Nullpoint.TIMER.reset();
	}
}