package me.nullpoint.mod.modules.impl.combat;

import com.mojang.authlib.GameProfile;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.eventbus.EventPriority;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.managers.RotateManager;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import me.nullpoint.mod.modules.impl.render.PlaceRender;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static me.nullpoint.api.utils.world.BlockUtil.*;
import static me.nullpoint.api.utils.world.BlockUtil.getWorldActionId;

public class HoleFiller extends Module {
    public static HoleFiller INSTANCE;

    public final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));

    public final SliderSetting delay =
            add(new SliderSetting("Delay", 50, 0, 500, v -> page.getValue() == Page.General));
    public final SliderSetting blocksPer =
            add(new SliderSetting("BlocksPer", 2, 1, 10, v -> page.getValue() == Page.General));
    private final BooleanSetting detectMining =
            add(new BooleanSetting("DetectMining", true, v -> page.getValue() == Page.General));
    private final BooleanSetting usingPause =
            add(new BooleanSetting("UsingPause", true, v -> page.getValue() == Page.General));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true, v -> page.getValue() == Page.General));
    private final BooleanSetting webs =
            add(new BooleanSetting("Webs", false, v -> page.getValue() == Page.General));
    private final SliderSetting range =
            add(new SliderSetting("Radius", 1.9, 0.0, 6.0, v -> page.getValue() == Page.General));
    private final SliderSetting saferange =
            add(new SliderSetting("SafeRange", 1.4, 0.0, 6.0, v -> page.getValue() == Page.General));
    public final SliderSetting placeRange =
            add(new SliderSetting("PlaceRange", 5.0, 0.0, 6.0, 0.1, v -> page.getValue() == Page.General));

    private final SliderSetting targetRange =
            add(new SliderSetting("TargetRange", 12.0, 0.0, 20.0, v -> page.getValue() == Page.General));
    public final BooleanSetting any = add(new BooleanSetting("AnyHole", true, v -> page.getValue() == Page.General));
    public final BooleanSetting doubleHole = add(new BooleanSetting("DoubleHole", true, v -> page.getValue() == Page.General));
    private final SliderSetting predictTicks =
            add(new SliderSetting("PredictTicks", 4, 0, 10, v -> page.getValue() == Page.General));

    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true, v -> page.getValue() == Page.Rotate).setParent());
    private final BooleanSetting newRotate =
            add(new BooleanSetting("NewRotate", false, v -> rotate.isOpen() && page.getValue() == Page.Rotate));
    private final SliderSetting yawStep =
            add(new SliderSetting("YawStep", 0.3f, 0.1f, 1.0f, 0.01f, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
    private final BooleanSetting checkLook =
            add(new BooleanSetting("CheckLook", true, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
    private final SliderSetting fov =
            add(new SliderSetting("Fov", 5f, 0f, 30f, v -> rotate.isOpen() && newRotate.getValue() && checkLook.getValue() && page.getValue() == Page.Rotate));
    private PlayerEntity closestTarget;
    private final Timer timer = new Timer();

    public Vec3d directionVec = null;
    private float lastYaw = 0;
    private float lastPitch = 0;
    int progress = 0;
    public HoleFiller(){
        super("HoleFiller", "Fills all safe spots in radius", Category.Combat);
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        closestTarget = null;
    }

    @Override
    public String getInfo() {
        return "";
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

    @Override
    public void onUpdate() {
        if (nullCheck()) {
            return;
        }
        if (!timer.passedMs(delay.getValueInt())) {
            return;
        }
        if (usingPause.getValue() && mc.player.isUsingItem()) {
            return;
        }
        progress = 0;
        directionVec = null;
        timer.reset();
        int obbySlot = findBlock(Blocks.OBSIDIAN);
        int eChestSlot = findBlock(Blocks.ENDER_CHEST);
        int webSlot = findBlock(Blocks.COBWEB);
        int block = webs.getValue() ? (webSlot == -1 ? (obbySlot == -1 ? eChestSlot : obbySlot) : webSlot) : (obbySlot == -1 ? eChestSlot : obbySlot);

        if (!webs.getValue() && obbySlot == -1 && eChestSlot == -1) return;

        if (webs.getValue() && webSlot == -1 && obbySlot == -1 && eChestSlot == -1) return;

        ArrayList<PlayerAndPredict> list = new ArrayList<>();
        for (PlayerEntity target : CombatUtil.getEnemies(targetRange.getRange())) {
            list.add(new PlayerAndPredict(target));
        }
        PlayerAndPredict self = new PlayerAndPredict(mc.player);
        if (!list.isEmpty()) {
            for (PlayerAndPredict pap : list) {
                for (BlockPos pos : BlockUtil.getSphere(range.getValueFloat(), pap.player.getPos())) {
                    if (BlockUtil.isHole(pos, true, true, any.getValue()) || doubleHole.getValue() && CombatUtil.isDoubleHole(pos)) {
                        if(mc.player.squaredDistanceTo(pos.toCenterPos()) < saferange.getValue()) continue;
                        if (detectMining.getValue() && (Nullpoint.BREAK.isMining(pos) || pos.equals(SpeedMine.breakPos))) continue;
                        if (progress >= blocksPer.getValueInt()) continue;
                        if(!BlockUtil.canPlace(pos, placeRange.getValue())) continue;
                        if (BlockUtil.getPlaceSide(pos, placeRange.getValue()) != null && mc.world.isAir(pos)) {
                            int oldSlot = mc.player.getInventory().selectedSlot;
                            doSwap(block);
                            placeBlock(pos, rotate.getValue());
                            progress++;
                            if (inventory.getValue()) {
                                doSwap(block);
                                EntityUtil.syncInventory();
                            } else {
                                doSwap(oldSlot);
                            }
                            timer.reset();
                        }
                    }
                }
            }
        }

    }

    public boolean placeBlock(BlockPos pos, boolean rotate) {

        if (airPlace()) {
            for (Direction i : Direction.values()) {
                if (mc.world.isAir(pos.offset(i))) {
                    return clickBlock(pos, i, rotate);
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
        clickBlock(pos.offset(side), side.getOpposite(), rotate);

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
    public boolean clickBlock(BlockPos pos, Direction side, boolean rotate) {
        Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
        if (rotate) {
            if (!faceVector(directionVec)) return false;
        }
        EntityUtil.swingHand(Hand.MAIN_HAND, CombatSetting.INSTANCE.swingMode.getValue());
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, getWorldActionId(mc.world)));
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
                EntityUtil.sendYawAndPitch(angle[0], angle[1]);
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


    public int findBlock(Block blockIn) {
        if (inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(blockIn);
        } else {
            return InventoryUtil.findBlock(blockIn);
        }
    }

    private void doSwap(int slot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    public class PlayerAndPredict {
        PlayerEntity player;
        PlayerEntity predict;
        public PlayerAndPredict(PlayerEntity player) {
            this.player = player;
            if (predictTicks.getValueFloat() > 0) {
                predict = new PlayerEntity(mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {@Override public boolean isSpectator() {return false;} @Override public boolean isCreative() {return false;}};
                predict.setPosition(player.getPos().add(CombatUtil.getMotionVec(player, INSTANCE.predictTicks.getValueInt(), true)));
                predict.setHealth(player.getHealth());
                predict.prevX = player.prevX;
                predict.prevZ = player.prevZ;
                predict.prevY = player.prevY;
                predict.setOnGround(player.isOnGround());
                predict.getInventory().clone(player.getInventory());
                predict.setPose(player.getPose());
                for (StatusEffectInstance se : player.getStatusEffects()) {
                    predict.addStatusEffect(se);
                }
            } else {
                predict = player;
            }
        }
    }

    public enum Page {
        General,
        Rotate
    }


}
