package me.nullpoint.mod.modules.impl.vanilla;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.eventbus.EventPriority;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.managers.RotateManager;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.entity.TPUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.util.ArrayList;

import static me.nullpoint.api.utils.world.BlockUtil.*;

public class VWebAura extends Module {
    public static VWebAura INSTANCE;
    public VWebAura() {
        super("VWebAura", Category.Combat);
        INSTANCE = this;
    }

    public final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));
    public final SliderSetting placeDelay =
            add(new SliderSetting("PlaceDelay", 50, 0, 500, v -> page.getValue() == Page.General));
    public final SliderSetting blocksPer =
            add(new SliderSetting("BlocksPer", 2, 1, 10, v -> page.getValue() == Page.General));

    public final SliderSetting multiPlace =
            add(new SliderSetting("MultiPlace", 2, 1, 10, v -> page.getValue() == Page.General));
    public final SliderSetting predictTicks =
            add(new SliderSetting("PredictTicks", 2, 0.0, 50, 1, v -> page.getValue() == Page.General));
    private final BooleanSetting detectMining =
            add(new BooleanSetting("DetectMining", true, v -> page.getValue() == Page.General));
    private final BooleanSetting extend =
            add(new BooleanSetting("Extend", true, v -> page.getValue() == Page.General));
    private final BooleanSetting extendFace =
            add(new BooleanSetting("ExtendFace", true, v -> page.getValue() == Page.General));
    private final BooleanSetting leg =
            add(new BooleanSetting("Leg", true, v -> page.getValue() == Page.General));
    private final BooleanSetting down =
            add(new BooleanSetting("Down", true, v -> page.getValue() == Page.General));
    private final BooleanSetting noHole =
            add(new BooleanSetting("NoHole", true, v -> page.getValue() == Page.General));
    private final BooleanSetting inventorySwap =
            add(new BooleanSetting("InventorySwap", true, v -> page.getValue() == Page.General));
    private final BooleanSetting usingPause =
            add(new BooleanSetting("UsingPause", true, v -> page.getValue() == Page.General));
    public final SliderSetting placeRange =
            add(new SliderSetting("PlaceRange", 5.0, 0.0, 300.0, 0.1, v -> page.getValue() == Page.General));
    public final SliderSetting targetRange =
            add(new SliderSetting("TargetRange", 8.0, 0.0, 300.0, 0.1, v -> page.getValue() == Page.General));
    private final BooleanSetting checkMine =
            add(new BooleanSetting("CheckMine", true, v -> page.getValue() == Page.General));
    private final BooleanSetting noMine =
            add(new BooleanSetting("NoMine", true, v -> page.getValue() == Page.General));
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true, v -> page.getValue() == Page.Rotate).setParent());
    private final BooleanSetting newRotate =
            add(new BooleanSetting("NewRotate", false, v -> rotate.isOpen() && page.getValue() == Page.Rotate));
    private final SliderSetting yawStep =
            add(new SliderSetting("YawStep", 0.3f, 0.1f, 1.0f, 0.01f, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
    private final BooleanSetting packet =
            add(new BooleanSetting("Packet", false, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
    private final BooleanSetting checkLook =
            add(new BooleanSetting("CheckLook", true, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
    private final SliderSetting fov =
            add(new SliderSetting("Fov", 5f, 0f, 30f, v -> rotate.isOpen() && newRotate.getValue() && checkLook.getValue() && page.getValue() == Page.Rotate));

    private final Timer timer = new Timer();
    public Vec3d directionVec = null;
    private float lastYaw = 0;
    private float lastPitch = 0;

    @Override
    public String getInfo() {
        if (pos.isEmpty()) return null;
        return "Working..";
    }

    @EventHandler(priority = EventPriority.HIGH - 2)
    public void onRotate(RotateEvent event) {
        if (newRotate.getValue() && directionVec != null) {
            float[] newAngle = injectStep(EntityUtil.getLegitRotations(directionVec), yawStep.getValueFloat());
            lastYaw = newAngle[0];
            lastPitch = newAngle[1];
            event.setYaw(lastYaw);
            event.setPitch(lastPitch);
        } else {
            lastYaw = Nullpoint.ROTATE.lastYaw;
            lastPitch = Nullpoint.ROTATE.lastPitch;
        }
    }

    int progress = 0;

    private final ArrayList<BlockPos> pos = new ArrayList<>();
    @EventHandler
    public void onUpdateWalking(UpdateWalkingEvent event) {
        if (event.isPost() || !timer.passedMs(placeDelay.getValueInt())) {
            return;
        }
        pos.clear();
        progress = 0;
        directionVec = null;
        if (getWebSlot() == -1) {
            return;
        }
        if (usingPause.getValue() && mc.player.isUsingItem()) {
            return;
        }
        for (PlayerEntity player : CombatUtil.getEnemies(targetRange.getValue())) {
            Vec3d playerPos = predictTicks.getValue() > 0 ? CombatUtil.getEntityPosVecWithY(player, predictTicks.getValueInt()) : player.getPos();
            if (leg.getValue()) {
                if (!noHole.getValue() || !BlockUtil.isHole(EntityUtil.getEntityPos(player, true))) {
                    if (placeWeb(new BlockPosX(playerPos.getX(), playerPos.getY(), playerPos.getZ()))) {
                        continue;
                    }
                }
            }
            if (down.getValue()) {
                placeWeb(new BlockPosX(playerPos.getX(), playerPos.getY() - 0.8, playerPos.getZ()));
            }
            boolean skip = false;
            if (extend.getValue() || extendFace.getValue()) {
                for (float x : new float[]{0, 0.3F, -0.3f}) {
                    for (float z : new float[]{0, 0.3F, -0.3f}) {
                        for (float y : new float[]{0, 1, -1}) {
                            BlockPosX pos = new BlockPosX(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z);
                            if (isTargetHere(pos, player) && mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB && !BlockUtil.isMining(pos)) {
                                skip = true;
                            }
                        }
                    }
                }
                if (skip) continue;
                if (extend.getValue()) {
                    start:
                    for (float x : new float[]{0, 0.3F, -0.3f}) {
                        for (float z : new float[]{0, 0.3F, -0.3f}) {
                            BlockPosX pos = new BlockPosX(playerPos.getX() + x, playerPos.getY(), playerPos.getZ() + z);
                            if (pos.equals(new BlockPosX(playerPos.getX(), playerPos.getY(), playerPos.getZ()))) {
                                continue;
                            }
                            if (isTargetHere(pos, player)) {
                                if (placeWeb(pos)) {
                                    skip = true;
                                    break start;
                                }
                            }
                        }
                    }
                }
                if (skip) continue;
                if (extendFace.getValue()) {
                    start:
                    for (float x : new float[]{0, 0.3F, -0.3f}) {
                        for (float z : new float[]{0, 0.3F, -0.3f}) {
                            BlockPosX pos = new BlockPosX(playerPos.getX() + x, playerPos.getY() + 1.1, playerPos.getZ() + z);
                            if (isTargetHere(pos, player)) {
                                if (placeWeb(pos)) {
                                    break start;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private boolean isTargetHere(BlockPos pos, PlayerEntity target) {
        return new Box(pos).intersects(target.getBoundingBox());
    }

    public static boolean place = false;
    private boolean placeWeb(BlockPos pos) {
        if (this.pos.contains(pos)) return false;
        this.pos.add(pos);
        if (progress >= multiPlace.getValueInt()) return false;
        if (getWebSlot() == -1) {
            return false;
        }
        if (checkMine.getValue() && (Nullpoint.BREAK.isMining(pos) || !noMine.getValue() && pos.equals(VSpeedMine.breakPos))) return false;
        if (BlockUtil.getPlaceSide(pos, placeRange.getValue()) != null && mc.world.isAir(pos)) {
            int oldSlot = mc.player.getInventory().selectedSlot;
            int webSlot = getWebSlot();
            if (!placeBlock(pos, rotate.getValue(), webSlot)) return false;
            if (noMine.getValue() && pos.equals(VSpeedMine.breakPos)) {
                VSpeedMine.breakPos = null;
            }
            progress++;
            //doSwap(oldSlot);
            timer.reset();
            place = false;
            return true;
        }
        return false;
    }

    public boolean placeBlock(BlockPos pos, boolean rotate, int slot) {

        if (airPlace()) {
            for (Direction i : Direction.values()) {
                if (mc.world.isAir(pos.offset(i))) {
                    return clickBlock(pos, i, rotate, slot);
                }
            }
        }
        Direction side = getPlaceSide(pos);
        if (side == null) return false;
        Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        BlockUtil.placedPos.add(pos);
        boolean sprint = false;
        if (mc.player != null) {
            sprint = mc.player.isSprinting();
        }
        boolean sneak = false;
        if (mc.world != null) {
            sneak = needSneak(mc.world.getBlockState(result.getBlockPos()).getBlock()) && !mc.player.isSneaking();
        }
        if (sprint)
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        if (sneak)
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        clickBlock(pos.offset(side), side.getOpposite(), rotate, slot);
        if (sneak)
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        if (sprint)
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        EntityUtil.swingHand(Hand.MAIN_HAND, CombatSetting.INSTANCE.swingMode.getValue());
        return true;
    }
    public static boolean needSneak(Block in) {
        return shiftBlocks.contains(in);
    }
    public boolean clickBlock(BlockPos pos, Direction side, boolean rotate, int slot) {
        Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
        if (rotate) {
            if (!faceVector(directionVec)) return false;
        }
        int oldSlot = mc.player.getInventory().selectedSlot;
        doSwap(slot);
        EntityUtil.swingHand(Hand.MAIN_HAND, CombatSetting.INSTANCE.swingMode.getValue());
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        TPUtil.tp(() -> mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, getWorldActionId(mc.world))), pos.toCenterPos());
        //mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, getWorldActionId(mc.world)));
        if (inventorySwap.getValue()) {
            doSwap(slot);
            EntityUtil.syncInventory();
        } else {
            doSwap(oldSlot);
        }
        return true;
    }

    private boolean faceVector(Vec3d directionVec) {
        if (!newRotate.getValue()) {
            RotateManager.lastEvent.cancelRotate();
            EntityUtil.faceVector(directionVec);
            return true;
        } else {
            this.directionVec = directionVec;
            float[] angle = EntityUtil.getLegitRotations(directionVec);
            if (Math.abs(MathHelper.wrapDegrees(angle[0] - lastYaw)) < fov.getValueFloat() && Math.abs(MathHelper.wrapDegrees(angle[1] - lastPitch)) < fov.getValueFloat()) {
                if (packet.getValue()) EntityUtil.sendYawAndPitch(angle[0], angle[1]);
                return true;
            }
        }
        return !checkLook.getValue();
    }

    private float[] injectStep(float[] angle, float steps) {
        if (steps < 0.01f) steps = 0.01f;

        if (steps > 1) steps = 1;

        if (steps < 1 && angle != null) {
            float packetYaw = lastYaw;
            float diff = MathHelper.wrapDegrees(angle[0] - packetYaw);

            if (Math.abs(diff) > 90 * steps) {
                angle[0] = (packetYaw + (diff * ((90 * steps) / Math.abs(diff))));
            }

            float packetPitch = lastPitch;
            diff = angle[1] - packetPitch;
            if (Math.abs(diff) > 90 * steps) {
                angle[1] = (packetPitch + (diff * ((90 * steps) / Math.abs(diff))));
            }
        }

        return new float[]{
                angle[0],
                angle[1]
        };
    }

    private void doSwap(int slot) {
        if (inventorySwap.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getWebSlot() {
        if (inventorySwap.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.COBWEB);
        } else {
            return InventoryUtil.findBlock(Blocks.COBWEB);
        }
    }

    public enum Page {
        General,
        Rotate
    }
}