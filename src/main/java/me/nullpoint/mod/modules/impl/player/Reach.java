package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class Reach extends Module {
	public static Reach INSTANCE;
	public final SliderSetting distance = add(new SliderSetting("Distance", 5, 1, 15, 0.1));

	public Reach() {
		super("Reach", Category.Player);
		INSTANCE = this;
	}
}