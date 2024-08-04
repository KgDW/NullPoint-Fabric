package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.Beta;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

import java.awt.*;
import java.text.DecimalFormat;

@Beta
public class TickShift extends Module {
	private final SliderSetting multiplier = add(new SliderSetting("Speed", 2, 1, 10, 0.1));
	private final SliderSetting accumulate = add(new SliderSetting("Charge", 2000f, 1f, 10000f, 50f).setSuffix("ms"));
	private final SliderSetting minAccumulate = add(new SliderSetting("MinCharge", 500f, 1f, 10000f, 50f).setSuffix("ms"));
	private final BooleanSetting smooth = add(new BooleanSetting("Smooth", true).setParent());
	private final EnumSetting<FadeUtils.Quad> quad = add(new EnumSetting<>("Quad", FadeUtils.Quad.In, v -> smooth.isOpen()));
	private final BooleanSetting reset = add(new BooleanSetting("Reset", true));
	private final BooleanSetting indicator = add(new BooleanSetting("Indicator", true).setParent());
	private final ColorSetting work = add(new ColorSetting("Completed", new Color(0, 255, 0), v -> indicator.isOpen()));
	private final ColorSetting charging = add(new ColorSetting("Charging", new Color(255, 0, 0), v -> indicator.isOpen()));
	private final SliderSetting yOffset = add(new SliderSetting("YOffset", 0, -200, 200, 1, v -> indicator.isOpen()));
	public static TickShift INSTANCE;
	public TickShift() {
		super("TickShift", Category.Movement);
		INSTANCE = this;
	}

	private final Timer timer = new Timer();
	private final Timer timer2 = new Timer();
	static DecimalFormat df = new DecimalFormat("0.0");
	private final FadeUtils end = new FadeUtils(500);

	long lastMs = 0;
	boolean moving = false;
	@Override
	public void onRender2D(DrawContext drawContext, float tickDelta) {
		timer.setMs(Math.min(Math.max(0, timer.getPassedTimeMs()), accumulate.getValueInt()));
		if (MovementUtil.isMoving() && !EntityUtil.isInsideBlock()) {

			if (!moving) {
				if (timer.passedMs(minAccumulate.getValue())) {
					timer2.reset();
					lastMs = timer.getPassedTimeMs();
				} else {
					lastMs = 0;
				}
				moving = true;
			}

			timer.reset();

			if (timer2.passed(lastMs)) {
				Nullpoint.TIMER.reset();
			} else {
				if (smooth.getValue()) {
					double timer = Nullpoint.TIMER.getDefault() + (1 - end.getQuad(quad.getValue())) * (multiplier.getValueFloat() - 1) * (lastMs / accumulate.getValue());
					Nullpoint.TIMER.set((float) Math.max(Nullpoint.TIMER.getDefault(), timer));
				} else {
					Nullpoint.TIMER.set(multiplier.getValueFloat());
				}
			}
		} else {
			if (moving) {
				Nullpoint.TIMER.reset();
				if (reset.getValue()) {
					timer.reset();
				} else {
					timer.setMs(Math.max(lastMs - timer2.getPassedTimeMs(), 0));
				}
				moving = false;
			}
			end.setLength(timer.getPassedTimeMs());
			end.reset();
		}

		if (indicator.getValue()) {
			double current = (moving ? (Math.max(lastMs - timer2.getPassedTimeMs(), 0)) : timer.getPassedTimeMs());
			boolean completed = moving && current > 0 || current >= minAccumulate.getValueInt();
			double max = accumulate.getValue();
			String text = df.format(current / max * 100L) + "%";
			drawContext.drawText(mc.textRenderer, text, mc.getWindow().getScaledWidth() / 2 - mc.textRenderer.getWidth(text) / 2, mc.getWindow().getScaledHeight() / 2 + mc.textRenderer.fontHeight - yOffset.getValueInt(), completed ? this.work.getValue().getRGB() : this.charging.getValue().getRGB(), true);
		}
	}

	@Override
	public String getInfo() {
		double current = (moving ? (Math.max(lastMs - timer2.getPassedTimeMs(), 0)) : timer.getPassedTimeMs());
		double max = accumulate.getValue();
		double value = Math.min(current / max * 100, 100);
		return df.format(value) + "%";
	}

	@EventHandler
	public void onReceivePacket(PacketEvent.Receive event) {
		if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
			lastMs = 0;
		}
	}

	public void onDisable() {
		Nullpoint.TIMER.reset();
	}

	public void onEnable() {
		Nullpoint.TIMER.reset();
	}

	private int normalLookPos;
	private int rotationMode;
	private int normalPos;

	@EventHandler
	public final void onPacketSend(final PacketEvent.Send event) {
		if (nullCheck()) return;
		if (event.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround && rotationMode == 1) {
			normalPos++;
			if (normalPos > 20) {
				rotationMode = 2;
			}
		} else if (event.getPacket() instanceof PlayerMoveC2SPacket.Full && rotationMode == 2) {
			normalLookPos++;
			if (normalLookPos > 20) {
				rotationMode = 1;
			}
		}
	}
	public static float nextFloat(final float startInclusive, final float endInclusive) {
		return (startInclusive == endInclusive || endInclusive - startInclusive <= 0.0f) ? startInclusive : ((float) (startInclusive + (endInclusive - startInclusive) * Math.random()));
	}

	@EventHandler
	public final void RotateEvent(RotateEvent event) {
		if (rotationMode == 2) {
			event.setRotation(event.getYaw() + nextFloat(1.0f, 3.0f), event.getPitch() + nextFloat(1.0f, 3.0f));
		}
	}
}