package me.nullpoint.mod.modules.impl.player;

import com.mojang.blaze3d.systems.RenderSystem;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.eventbus.EventPriority;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.movement.HoleSnap;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Scaffold extends Module {
    private final BooleanSetting tower =
            add(new BooleanSetting("Tower", true));
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", false));
    public final SliderSetting rotateTime = add(new SliderSetting("KeepRotate", 1000, 0, 3000, 10));
    private final BooleanSetting render = add(new BooleanSetting("Render", true).setParent());
    public final ColorSetting color = add(new ColorSetting("Color",new Color(255, 255, 255, 100), v -> render.isOpen()));
    private final BooleanSetting esp = add(new BooleanSetting("ESP", true, v -> render.isOpen()));
    private final BooleanSetting box = add(new BooleanSetting("Box", true, v -> render.isOpen()));
    private final BooleanSetting outline = add(new BooleanSetting("Outline", true, v -> render.isOpen()));
    public final SliderSetting sliderSpeed = add(new SliderSetting("SliderSpeed", 0.2, 0.01, 1, 0.01, v -> render.isOpen()));

    public Scaffold() {
        super("Scaffold", Category.Player);
    }



    private final Timer timer = new Timer();

    private float[] angle = null;

    @EventHandler(priority =  EventPriority.HIGH)
    public void onRotation(RotateEvent event) {
        if (rotate.getValue() && !timer.passedMs(rotateTime.getValueInt()) && angle != null) {
            event.setYaw(angle[0]);
            event.setPitch(angle[1]);
        }
    }

    @Override
    public void onEnable() {
        lastVec3d = null;
        pos = null;
    }

    private BlockPos pos;
    private static Vec3d lastVec3d;
    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        if (render.getValue()) {
            if (esp.getValue()) {
                GL11.glEnable(GL11.GL_BLEND);
                double temp = 0.01;
                for (double i = 0; i < 0.8; i += temp) {
                    HoleSnap.doCircle(matrixStack, ColorUtil.injectAlpha(color.getValue(), (int) Math.min(color.getValue().getAlpha() * 2 / (0.8 / temp), 255)), i, new Vec3d(MathUtil.interpolate(mc.player.lastRenderX, mc.player.getX(), partialTicks), MathUtil.interpolate(mc.player.lastRenderY, mc.player.getY(), partialTicks), MathUtil.interpolate(mc.player.lastRenderZ, mc.player.getZ(), partialTicks)), 5);
                }
                RenderSystem.setShaderColor(1, 1, 1, 1);
                GL11.glDisable(GL11.GL_BLEND);
            }
            if (pos != null) {
                Vec3d cur = pos.toCenterPos();
                if (lastVec3d == null) {
                    lastVec3d = cur;
                } else {
                    lastVec3d = new Vec3d(AnimateUtil.animate(lastVec3d.getX(), cur.x, sliderSpeed.getValue()),
                            AnimateUtil.animate(lastVec3d.getY(), cur.y, sliderSpeed.getValue()),
                            AnimateUtil.animate(lastVec3d.getZ(), cur.z, sliderSpeed.getValue()));
                }
                Render3DUtil.draw3DBox(matrixStack, new Box(lastVec3d.add(0.5, 0.5, 0.5), lastVec3d.add(-0.5, -0.5, -0.5)), ColorUtil.injectAlpha(color.getValue(), color.getValue().getAlpha()), outline.getValue(), box.getValue());
            }
        }
    }
    
    private final Timer towerTimer = new Timer();
    @Override
    public void onUpdate() {
        int block = InventoryUtil.findBlock();
        if (block == -1) return;
        BlockPos placePos = EntityUtil.getPlayerPos().down();
        if (BlockUtil.clientCanPlace(placePos, false)) {
            int old = mc.player.getInventory().selectedSlot;
            if (BlockUtil.getPlaceSide(placePos) == null) {
                double distance = 1000;
                BlockPos bestPos = null;
                for (Direction i : Direction.values()) {
                    if (i == Direction.UP) continue;
                    if (BlockUtil.canPlace(placePos.offset(i))) {
                        if (bestPos == null || mc.player.squaredDistanceTo(placePos.offset(i).toCenterPos()) < distance) {
                            bestPos = placePos.offset(i);
                            distance = mc.player.squaredDistanceTo(placePos.offset(i).toCenterPos());
                        }
                    }
                }
                if (bestPos != null) {
                    placePos = bestPos;
                } else {
                    return;
                }
            }
            if (rotate.getValue()) {
                Direction side = BlockUtil.getPlaceSide(placePos);
                angle = EntityUtil.getLegitRotations(placePos.offset(side).toCenterPos().add(side.getOpposite().getVector().getX() * 0.5, side.getOpposite().getVector().getY() * 0.5, side.getOpposite().getVector().getZ() * 0.5));
                timer.reset();
            }
            InventoryUtil.switchToSlot(block);
            BlockUtil.placeBlock(placePos, rotate.getValue(), false);
            InventoryUtil.switchToSlot(old);
            pos = placePos;
            if (tower.getValue() && mc.options.jumpKey.isPressed() && !MovementUtil.isMoving()) {
                MovementUtil.setMotionY(0.42);
                MovementUtil.setMotionX(0);
                MovementUtil.setMotionZ(0);
                if (this.towerTimer.passedMs(1500L)) {
                    MovementUtil.setMotionY(-0.28);
                    this.towerTimer.reset();
                }
            } else {
                this.towerTimer.reset();
            }
        }
    }
}
