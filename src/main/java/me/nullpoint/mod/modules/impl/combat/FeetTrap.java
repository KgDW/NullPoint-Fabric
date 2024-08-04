package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.MoveEvent;
import me.nullpoint.api.events.impl.Render3DEvent;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.managers.RotateManager;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;

import java.awt.*;
import java.util.HashMap;

public class FeetTrap extends Module {
    public static FeetTrap INSTANCE = new FeetTrap();
    //public final BooleanSetting enableInHole = add(new BooleanSetting("EnableInHole", false));
    private final Timer timer = new Timer();
    public final SliderSetting placeDelay =
            add(new SliderSetting("PlaceDelay", 50, 0, 500));
    private final SliderSetting blocksPer =
            add(new SliderSetting("BlocksPer", 1, 1, 8));
    private final BooleanSetting detectMining =
            add(new BooleanSetting("DetectMining", false));
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true));
    private final BooleanSetting packetPlace =
            add(new BooleanSetting("PacketPlace", true));
    private final BooleanSetting breakCrystal =
            add(new BooleanSetting("Break", true).setParent());
    private final BooleanSetting usingPause =
            add(new BooleanSetting("EatingPause", true, v -> breakCrystal.isOpen()));
    private final BooleanSetting center =
            add(new BooleanSetting("Center", true));
    public final BooleanSetting extend =
            add(new BooleanSetting("Extend", true));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true));
    public final BooleanSetting inAir =
            add(new BooleanSetting("InAir", true));
    private final BooleanSetting moveDisable =
            add(new BooleanSetting("AutoDisable", true));
    private final BooleanSetting jumpDisable =
            add(new BooleanSetting("JumpDisable", true));
    private final BooleanSetting enderChest =
            add(new BooleanSetting("EnderChest", true));
    public final BooleanSetting render =
            add(new BooleanSetting("Render", true).setParent());
    final ColorSetting box =
            add(new ColorSetting("Box", new Color(255, 255, 255, 255), v -> render.isOpen()).injectBoolean(true));
    final ColorSetting fill =
            add(new ColorSetting("Fill", new Color(255, 255, 255, 100), v -> render.isOpen()).injectBoolean(true));
    public final SliderSetting fadeTime = add(new SliderSetting("FadeTime", 500, 0, 5000, v -> render.getValue()));
    public final BooleanSetting pre = add(new BooleanSetting("Pre", false, v -> render.getValue()));
    public final BooleanSetting moveReset = add(new BooleanSetting("Reset", true, v -> render.getValue()));

    double startX = 0;
    double startY = 0;
    double startZ = 0;
    int progress = 0;
    public FeetTrap() {
        super("FeetTrap", "Surrounds you with Obsidian", Category.Combat);
        INSTANCE = this;
        Nullpoint.EVENT_BUS.subscribe(new FeetTrapRenderer());
    }


    @Override
    public void onEnable() {
        if (nullCheck()) {
            if (moveDisable.getValue() || jumpDisable.getValue()) disable();
            return;
        }
        startX = mc.player.getX();
        startY = mc.player.getY();
        startZ = mc.player.getZ();
        shouldCenter = true;
    }

    private boolean shouldCenter = true;

    @EventHandler(priority = -1)
    public void onMove(MoveEvent event) {
        if (nullCheck() || !center.getValue() || EntityUtil.isElytraFlying()) {
            return;
        }

        BlockPos blockPos = EntityUtil.getPlayerPos(true);
        if (mc.player.getX() - blockPos.getX() - 0.5 <= 0.2 && mc.player.getX() - blockPos.getX() - 0.5 >= -0.2 && mc.player.getZ() - blockPos.getZ() - 0.5 <= 0.2 && mc.player.getZ() - 0.5 - blockPos.getZ() >= -0.2) {
            if (shouldCenter && (mc.player.isOnGround() || MovementUtil.isMoving())) {
                event.setX(0);
                event.setZ(0);
                shouldCenter = false;
            }
        } else {
            if (shouldCenter) {
                Vec3d centerPos = EntityUtil.getPlayerPos(true).toCenterPos();
                float rotation = getRotationTo(mc.player.getPos(), centerPos).x;
                float yawRad = rotation / 180.0f * 3.1415927f;
                double dist = mc.player.getPos().distanceTo(new Vec3d(centerPos.x, mc.player.getY(), centerPos.z));
                double cappedSpeed = Math.min(0.2873, dist);
                double x = -(float) Math.sin(yawRad) * cappedSpeed;
                double z = (float) Math.cos(yawRad) * cappedSpeed;
                event.setX(x);
                event.setZ(z);
            }
        }
    }

    @EventHandler
    public void onUpdateWalking(UpdateWalkingEvent event) {
        if (event.isPost() || !timer.passedMs((long) placeDelay.getValue())) return;
        progress = 0;
        if (!MovementUtil.isMoving() && !mc.options.jumpKey.isPressed()) {
            startX = mc.player.getX();
            startY = mc.player.getY();
            startZ = mc.player.getZ();
        }
        BlockPos pos = EntityUtil.getPlayerPos(true);

        double distanceToStart = MathHelper.sqrt((float) mc.player.squaredDistanceTo(startX, startY, startZ));

        if (getBlock() == -1) {
            CommandManager.sendChatMessage("§e[?] §c§oObsidian" + (enderChest.getValue() ? "/EnderChest" : "") + "?");
            disable();
            return;
        }
        if ((moveDisable.getValue() && distanceToStart > 1.0 || jumpDisable.getValue() && Math.abs(startY - mc.player.getY()) > 0.5)) {
            disable();
            return;
        }

        if (!inAir.getValue() && !mc.player.isOnGround()) return;
        for (Direction i : Direction.values()) {
            if (i == Direction.UP) continue;
            BlockPos offsetPos = pos.offset(i);
            if (BlockUtil.getPlaceSide(offsetPos) != null) {
                tryPlaceBlock(offsetPos);
            } else if (BlockUtil.canReplace(offsetPos)) {
                tryPlaceBlock(getHelperPos(offsetPos));
            }
            if (selfIntersectPos(offsetPos) && extend.getValue()) {
                for (Direction i2 : Direction.values()) {
                    if (i2 == Direction.UP) continue;
                    BlockPos offsetPos2 = offsetPos.offset(i2);
                    if (selfIntersectPos(offsetPos2)) {
                        for (Direction i3 : Direction.values()) {
                            if (i3 == Direction.UP) continue;
                            tryPlaceBlock(offsetPos2);
                            BlockPos offsetPos3 = offsetPos2.offset(i3);
                            tryPlaceBlock(BlockUtil.getPlaceSide(offsetPos3) != null || !BlockUtil.canReplace(offsetPos3) ? offsetPos3 : getHelperPos(offsetPos3));
                        }
                    }
                    tryPlaceBlock(BlockUtil.getPlaceSide(offsetPos2) != null || !BlockUtil.canReplace(offsetPos2) ? offsetPos2 : getHelperPos(offsetPos2));
                }
            }
        }
    }

    private void tryPlaceBlock(BlockPos pos) {
        if (pos == null) return;
        if (detectMining.getValue() && BlockUtil.isMining(pos)) return;
        if (pre.getValue()) {
            if (BlockUtil.clientCanPlace(pos, true)) FeetTrapRenderer.addBlock(pos);
        }
        if (!(progress < blocksPer.getValue())) return;
        int block = getBlock();
        if (block == -1) return;

        if (!BlockUtil.canPlace(pos, 6, true)) return;
        if (breakCrystal.getValue()) {
            CombatUtil.attackCrystal(pos, rotate.getValue(), usingPause.getValue());
        } else if (BlockUtil.hasEntity(pos, false)) return;
        if (rotate.getValue()) {
            RotateManager.lastEvent.cancelRotate();
        }
        int old = mc.player.getInventory().selectedSlot;
        doSwap(block);
        BlockUtil.placeBlock(pos, rotate.getValue(), packetPlace.getValue());
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.syncInventory();
        } else {
            doSwap(old);
        }
        progress++;
        timer.reset();
        FeetTrapRenderer.addBlock(pos);
    }

    public static boolean selfIntersectPos(BlockPos pos) {
        return mc.player.getBoundingBox().intersects(new Box(pos));
    }
    private void doSwap(int slot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getBlock() {
        if (inventory.getValue()) {
            if (InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN) != -1 || !enderChest.getValue()) {
                return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
            }
            return InventoryUtil.findBlockInventorySlot(Blocks.ENDER_CHEST);
        } else {
            if (InventoryUtil.findBlock(Blocks.OBSIDIAN) != -1 || !enderChest.getValue()) {
                return InventoryUtil.findBlock(Blocks.OBSIDIAN);
            }
            return InventoryUtil.findBlock(Blocks.ENDER_CHEST);
        }
    }

    public BlockPos getHelperPos(BlockPos pos) {
        for (Direction i : Direction.values()) {
            if (detectMining.getValue() && BlockUtil.isMining(pos.offset(i))) continue;
            if (!BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite(), true)) continue;
            if (BlockUtil.canPlace(pos.offset(i))) return pos.offset(i);
        }
        return null;
    }

    public static Vec2f getRotationTo(Vec3d posFrom, Vec3d posTo) {
        Vec3d vec3d = posTo.subtract(posFrom);
        return getRotationFromVec(vec3d);
    }

    private static Vec2f getRotationFromVec(Vec3d vec) {
        double d = vec.x;
        double d2 = vec.z;
        double xz = Math.hypot(d, d2);
        d2 = vec.z;
        double d3 = vec.x;
        double yaw = normalizeAngle(Math.toDegrees(Math.atan2(d2, d3)) - 90.0);
        double pitch = normalizeAngle(Math.toDegrees(-Math.atan2(vec.y, xz)));
        return new Vec2f((float) yaw, (float) pitch);
    }

    private static double normalizeAngle(double angleIn) {
        double angle = angleIn;
        if ((angle %= 360.0) >= 180.0) {
            angle -= 360.0;
        }
        if (angle < -180.0) {
            angle += 360.0;
        }
        return angle;
    }

    public class FeetTrapRenderer {
        public static final HashMap<BlockPos, FeetTrapRenderer.placePosition> renderMap = new HashMap<>();
        public static void addBlock(BlockPos pos) {
            renderMap.put(pos, new FeetTrapRenderer.placePosition(pos));
        }

        private BlockPos lastPos = null;

        @EventHandler
        public void onRender3D(Render3DEvent event) {
            if (!INSTANCE.render.getValue()) return;
            if (INSTANCE.moveReset.getValue() && !EntityUtil.getPlayerPos(true).equals(lastPos)) {
                lastPos = EntityUtil.getPlayerPos(true);
                renderMap.clear();
            }
            if (renderMap.isEmpty()) return;
            boolean shouldClear = true;
            for (FeetTrapRenderer.placePosition placePosition : renderMap.values()) {
                if (!BlockUtil.clientCanPlace(placePosition.pos, true)) {
                    placePosition.isAir = false;
                }
                if (!placePosition.timer.passedMs((long) (placeDelay.getValue() + 100)) && placePosition.isAir) {
                    placePosition.firstFade.reset();
                }
                if (placePosition.firstFade.getQuad(FadeUtils.Quad.In2) == 1) continue;
                shouldClear = false;
                MatrixStack matrixStack = event.getMatrixStack();
                if (INSTANCE.fill.booleanValue) {
                    Render3DUtil.drawFill(matrixStack, new Box(placePosition.pos), ColorUtil.injectAlpha(INSTANCE.fill.getValue(), (int) ((double) fill.getValue().getAlpha() * (1 - placePosition.firstFade.getQuad(FadeUtils.Quad.In2)))));
                }
                if (INSTANCE.box.booleanValue) {
                    Render3DUtil.drawBox(matrixStack, new Box(placePosition.pos), ColorUtil.injectAlpha(INSTANCE.box.getValue(), (int) ((double) box.getValue().getAlpha() * (1 - placePosition.firstFade.getQuad(FadeUtils.Quad.In2)))));
                }
            }
            if (shouldClear) renderMap.clear();
        }


        public static class placePosition {
            public final FadeUtils firstFade;
            public final BlockPos pos;
            public final Timer timer;
            public boolean isAir;
            public placePosition(BlockPos placePos) {
                this.firstFade = new FadeUtils((long) FeetTrap.INSTANCE.fadeTime.getValue());
                this.pos = placePos;
                this.timer = new Timer();
                this.isAir = true;
            }
        }
    }
}
