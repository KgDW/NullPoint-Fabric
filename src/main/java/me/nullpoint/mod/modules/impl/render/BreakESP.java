package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.MineManager;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.HashMap;

public class BreakESP extends Module {
	public static BreakESP INSTANCE;
	private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
	public final BooleanSetting outline = add(new BooleanSetting("Outline", false));
	public final BooleanSetting box = add(new BooleanSetting("Box", true));
	public final SliderSetting animationTime = add(new SliderSetting("AnimationTime", 500, 0, 2000));
	private final EnumSetting<FadeUtils.Quad> quad = add(new EnumSetting<>("Quad", FadeUtils.Quad.In));

	public BreakESP() {
		super("BreakESP", Category.Render);
		INSTANCE = this;
	}

	@Override
	public void onRender3D(MatrixStack matrixStack, float partialTicks) {
		for (MineManager.BreakData breakData : new HashMap<>(Nullpoint.BREAK.breakMap).values()) {
			if (breakData == null || breakData.getEntity() == null) continue;
			double size = 0.5 * (1 - breakData.fade.getQuad(quad.getValue()));
			Render3DUtil.draw3DBox(matrixStack, new Box(breakData.pos).shrink(size, size, size).shrink(-size, -size, -size), color.getValue(), outline.getValue(), box.getValue());
			Render3DUtil.drawText3D(breakData.getEntity().getName().getString(), breakData.pos.toCenterPos().add(0, 0.1, 0), -1);
			Render3DUtil.drawText3D(Text.of(mc.world.isAir(breakData.pos) ? "Broken" : "Breaking"), breakData.pos.toCenterPos().add(0, -0.1, 0), 0, 0, 1, new Color(0, 255, 51));
		}
	}
}
