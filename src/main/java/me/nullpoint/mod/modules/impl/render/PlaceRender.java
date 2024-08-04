package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.HashMap;

public class PlaceRender extends Module {
	public static PlaceRender INSTANCE;
	private final ColorSetting box = add(new ColorSetting("Box", new Color(255, 255, 255, 255)).injectBoolean(true));
	private final ColorSetting fill = add(new ColorSetting("Fill", new Color(255, 255, 255, 100)).injectBoolean(true));
	public final SliderSetting fadeTime = add(new SliderSetting("FadeTime", 500, 0, 3000));

	private final ColorSetting tryPlaceBox = add(new ColorSetting("TryPlaceBox", new Color(178, 178, 178, 255)).injectBoolean(true));
	private final ColorSetting tryPlaceFill = add(new ColorSetting("TryPlaceFill", new Color(255, 119, 119, 157)).injectBoolean(true));
	public final SliderSetting timeout = add(new SliderSetting("TimeOut", 500, 0, 3000));
	private final EnumSetting<FadeUtils.Quad> quad = add(new EnumSetting<>("Quad", FadeUtils.Quad.In));
	private final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.All));
	private enum Mode {
		Fade,
		Shrink,
		All,
	}
	public PlaceRender() {
		super("PlaceRender", Category.Render);
		enable();
		INSTANCE = this;
	}

	@Override
	public void onRender3D(MatrixStack matrixStack, float partialTicks) {
		BlockUtil.placedPos.forEach(pos -> renderMap.put(pos, new PlacePos(pos)));
		BlockUtil.placedPos.clear();
		if (renderMap.isEmpty()) return;
		boolean shouldClear = true;
		for (PlacePos placePosition : renderMap.values()) {
			if (placePosition.isAir) {
				if (!mc.world.isAir(placePosition.pos)) {
					placePosition.isAir = false;
				} else {
					if (!placePosition.timer.passed(timeout.getValue())) {
						placePosition.fade.reset();
						Box aBox = new Box(placePosition.pos);
						if (tryPlaceFill.booleanValue) {
							Render3DUtil.drawFill(matrixStack, aBox, tryPlaceFill.getValue());
						}
						if (tryPlaceBox.booleanValue) {
							Render3DUtil.drawBox(matrixStack, aBox, tryPlaceBox.getValue());
						}
						shouldClear = false;
					}
					continue;
				}
			}
			double quads = placePosition.fade.getQuad(quad.getValue());
			if (quads == 1) continue;
			shouldClear = false;
			double alpha = (mode.getValue() == Mode.Fade || mode.getValue() == Mode.All) ? 1 - quads : 1;
			double size = (mode.getValue() == Mode.Shrink || mode.getValue() == Mode.All) ? quads : 0;
			Box aBox = new Box(placePosition.pos).expand(-size * 0.5, -size * 0.5, -size * 0.5);
			if (fill.booleanValue) {
				Render3DUtil.drawFill(matrixStack, aBox, ColorUtil.injectAlpha(fill.getValue(), (int) ((double) fill.getValue().getAlpha() * alpha)));
			}
			if (box.booleanValue) {
				Render3DUtil.drawBox(matrixStack, aBox, ColorUtil.injectAlpha(box.getValue(), (int) ((double) box.getValue().getAlpha() * alpha)));
			}
		}
		if (shouldClear) renderMap.clear();
	}

	public static final HashMap<BlockPos, PlacePos> renderMap = new HashMap<>();

	public class PlacePos {
		public final FadeUtils fade;
		public final BlockPos pos;
		public final Timer timer;
		public boolean isAir;
		public PlacePos(BlockPos placePos) {
			this.fade = new FadeUtils((long) fadeTime.getValue());
			this.pos = placePos;
			this.timer = new Timer();
			this.isAir = true;
		}
	}
}

