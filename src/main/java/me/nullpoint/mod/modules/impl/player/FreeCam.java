package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.api.events.impl.KeyboardInputEvent;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.eventbus.EventPriority;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;

public class FreeCam extends Module {
    public static FreeCam INSTANCE;
    private final SliderSetting speed = add(new SliderSetting("HSpeed", 1, 0.0, 3));
    private final SliderSetting hspeed = add(new SliderSetting("VSpeed", 0.42, 0.0, 3));
    final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true));
    private float fakeYaw;
    private float fakePitch;
    private float prevFakeYaw;
    private float prevFakePitch;
    private double fakeX;
    private double fakeY;
    private double fakeZ;
    private double prevFakeX;
    private double prevFakeY;
    private double prevFakeZ;

    public FreeCam() {
        super("FreeCam", Category.Player);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            disable();
            return;
        }
        mc.chunkCullingEnabled = false;

        preYaw = mc.player.getYaw();
        prePitch = mc.player.getPitch();

        fakePitch = mc.player.getPitch();
        fakeYaw = mc.player.getYaw();

        prevFakePitch = fakePitch;
        prevFakeYaw = fakeYaw;

        fakeX = mc.player.getX();
        fakeY = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
        fakeZ = mc.player.getZ();

        prevFakeX = fakeX;
        prevFakeY = fakeY;
        prevFakeZ = fakeZ;
    }


    @Override
    public void onDisable() {
        mc.chunkCullingEnabled = true;
    }

    @Override
    public void onUpdate() {
        if (rotate.getValue() && mc.crosshairTarget != null && mc.crosshairTarget.getPos() != null) {
            float[] angle = EntityUtil.getLegitRotations(mc.crosshairTarget.getPos());
            preYaw = angle[0];
            prePitch = angle[1];
        }
    }

    private float preYaw;
    private float prePitch;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRotate(RotateEvent event) {
        event.setYawNoModify(preYaw);
        event.setPitchNoModify(prePitch);
    }

    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        prevFakeYaw = fakeYaw;
        prevFakePitch = fakePitch;

        fakeYaw = mc.player.getYaw();
        fakePitch = mc.player.getPitch();
    }

    @EventHandler
    public void onKeyboardInput(KeyboardInputEvent event) {
        if (mc.player == null) return;

        double[] motion = MovementUtil.directionSpeed(speed.getValue());

        prevFakeX = fakeX;
        prevFakeY = fakeY;
        prevFakeZ = fakeZ;

        fakeX += motion[0];
        fakeZ += motion[1];

        if (mc.options.jumpKey.isPressed())
            fakeY += hspeed.getValue();

        if (mc.options.sneakKey.isPressed())
            fakeY -= hspeed.getValue();

        mc.player.input.movementForward = 0;
        mc.player.input.movementSideways = 0;
        mc.player.input.jumping = false;
        mc.player.input.sneaking = false;
    }

    public float getFakeYaw() {
        return (float) MathUtil.interpolate(prevFakeYaw, fakeYaw, mc.getTickDelta());
    }

    public float getFakePitch() {
        return (float) MathUtil.interpolate(prevFakePitch, fakePitch, mc.getTickDelta());
    }

    public double getFakeX() {
        return MathUtil.interpolate(prevFakeX, fakeX, mc.getTickDelta());
    }

    public double getFakeY() {
        return MathUtil.interpolate(prevFakeY, fakeY, mc.getTickDelta());
    }

    public double getFakeZ() {
        return MathUtil.interpolate(prevFakeZ, fakeZ, mc.getTickDelta());
    }
}
