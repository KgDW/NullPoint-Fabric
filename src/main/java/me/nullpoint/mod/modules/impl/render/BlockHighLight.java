package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class BlockHighLight extends Module {
    public BlockHighLight() {
        super("BlockHighLight", Category.Render);
    }

    final BooleanSetting center =
            add(new BooleanSetting("Center", true));
    final BooleanSetting shrink =
            add(new BooleanSetting("Shrink", true));
    final ColorSetting box =
            add(new ColorSetting("Box", new Color(255, 255, 255, 255)).injectBoolean(true));
    final ColorSetting fill =
            add(new ColorSetting("Fill", new Color(255, 255, 255, 100)).injectBoolean(true));
    final SliderSetting sliderSpeed = add(new SliderSetting("SliderSpeed", 0.2, 0.01, 1, 0.01));
    final SliderSetting startFadeTime =
            add(new SliderSetting("StartFade", 0.3d, 0d, 2d, 0.01).setSuffix("s"));
    final SliderSetting fadeSpeed =
            add(new SliderSetting("FadeSpeed", 0.2d, 0.01d, 1d, 0.01));
    final Timer noPosTimer = new Timer();
    static Vec3d placeVec3d;
    static Vec3d curVec3d;
    double fade = 0;
    
    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        if(mc.crosshairTarget==null ||!(mc.crosshairTarget instanceof BlockHitResult hitResult)) return;
        if (mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            noPosTimer.reset();
            placeVec3d = center.getValue() ? hitResult.getBlockPos().toCenterPos() : mc.crosshairTarget.getPos();
        }
        if (placeVec3d == null) {
            return;
        }
        if (fadeSpeed.getValue() >= 1) {
            fade = noPosTimer.passedMs((long) (startFadeTime.getValue() * 1000)) ? 0 : 0.5;
        } else {
            fade = AnimateUtil.animate(fade, noPosTimer.passedMs((long) (startFadeTime.getValue() * 1000)) ? 0 : 0.5, fadeSpeed.getValue() / 10);
        }
        if (fade == 0) {
            curVec3d = null;
            return;
        }
        if (curVec3d == null || sliderSpeed.getValue() >= 1) {
            curVec3d = placeVec3d;
        } else {
            curVec3d = new Vec3d(AnimateUtil.animate(curVec3d.x, placeVec3d.x, sliderSpeed.getValue() / 10),
                    AnimateUtil.animate(curVec3d.y, placeVec3d.y, sliderSpeed.getValue() / 10),
                    AnimateUtil.animate(curVec3d.z, placeVec3d.z, sliderSpeed.getValue() / 10));
        }

            Box box = new Box(curVec3d, curVec3d);
            if (shrink.getValue()) {
                box = box.expand(fade);
            } else {
                box = box.expand(0.5);
            }
            if (fill.booleanValue) {
                Render3DUtil.drawFill(matrixStack, box, ColorUtil.injectAlpha(fill.getValue(), (int) (fill.getValue().getAlpha() * fade * 2D)));
            }
            if (this.box.booleanValue) {
                Render3DUtil.drawBox(matrixStack, box, ColorUtil.injectAlpha(this.box.getValue(), (int) (this.box.getValue().getAlpha() * fade * 2D)));
            }
        }
    }
