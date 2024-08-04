package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.TotemParticleEvent;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

import java.awt.*;
import java.util.Random;

public class TotemParticle extends Module {
	public static TotemParticle INSTANCE;
	private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 255)));
	private final ColorSetting color2 = add(new ColorSetting("Color2", new Color(0, 0, 0, 255)));
	public final SliderSetting velocityXZ =
			add(new SliderSetting("VelocityXZ", 100, 0, 500, 1).setSuffix("%"));
	public final SliderSetting velocityY =
			add(new SliderSetting("VelocityY", 100, 0, 500, 1).setSuffix("%"));
	public TotemParticle() {
		super("TotemParticle", Category.Render);
		INSTANCE = this;
	}
	Random random = new Random();
	@EventHandler
	public void idk(TotemParticleEvent event) {
		event.cancel();
		event.velocityZ *= velocityXZ.getValue() / 100;
		event.velocityX *= velocityXZ.getValue() / 100;

		event.velocityY *= velocityY.getValue() / 100;

		event.color = ColorUtil.fadeColor(color.getValue(), color2.getValue(), random.nextDouble());
	}
}
