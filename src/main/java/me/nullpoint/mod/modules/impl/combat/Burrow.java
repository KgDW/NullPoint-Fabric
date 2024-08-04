package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.Render3DEvent;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.managers.BreakManager;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.managers.MineManager;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.asm.accessors.IEntity;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.nullpoint.api.utils.world.BlockUtil.canReplace;

public class Burrow extends Module {
    public static Burrow INSTANCE;
    private final BooleanSetting enderChest = add(new BooleanSetting("EnderChest", true));
    private final BooleanSetting antiLag = add(new BooleanSetting("AntiLag", true));
    public final BooleanSetting helper =
            add(new BooleanSetting("Helper", true));
    private final BooleanSetting headFill = add(new BooleanSetting("HeadFill", true));
    private final BooleanSetting noSelfPos = add(new BooleanSetting("NoSelfPos", false));
    private final BooleanSetting packetPlace =
            add(new BooleanSetting("PacketPlace", true));
    private final BooleanSetting sound =
            add(new BooleanSetting("Sound", true));
    private final SliderSetting blocksPer = add(new SliderSetting("BlocksPer", 4, 1, 4, 1));
    private final EnumSetting<RotateMode> rotate = add(new EnumSetting<>("RotateMode", RotateMode.Bypass));
    private final BooleanSetting breakCrystal = add(new BooleanSetting("Break", true));
    private final BooleanSetting wait = add(new BooleanSetting("Wait", true));
    private final BooleanSetting fakeMove = add(new BooleanSetting("FakeMove", true).setParent());
    private final BooleanSetting center = add(new BooleanSetting("AllowCenter", false, v -> fakeMove.isOpen()));
    private final BooleanSetting inventory = add(new BooleanSetting("InventorySwap", true));
    private final EnumSetting<LagBackMode> lagMode = add(new EnumSetting<>("LagMode", LagBackMode.TrollHack));
    private final EnumSetting<LagBackMode> aboveLagMode = add(new EnumSetting<>("MoveLagMode", LagBackMode.Smart));
    private final SliderSetting smartX = add(new SliderSetting("SmartXZ", 3, 0, 10, 0.1, v -> lagMode.getValue() == LagBackMode.Smart || aboveLagMode.getValue() == LagBackMode.Smart));
    private final SliderSetting smartUp = add(new SliderSetting("SmartUp", 3, 0, 10, 0.1, v -> lagMode.getValue() == LagBackMode.Smart || aboveLagMode.getValue() == LagBackMode.Smart));
    private final SliderSetting smartDown = add(new SliderSetting("SmartDown", 3, 0, 10, 0.1, v -> lagMode.getValue() == LagBackMode.Smart || aboveLagMode.getValue() == LagBackMode.Smart));
    private final SliderSetting smartDistance = add(new SliderSetting("SmartDistance", 2, 0, 10, 0.1, v -> lagMode.getValue() == LagBackMode.Smart || aboveLagMode.getValue() == LagBackMode.Smart));
    private int progress = 0;
    private final List<BlockPos> placePos = new ArrayList<>();

    public Burrow() {
        super("Burrow", Category.Combat);
        INSTANCE = this;
    }

    @EventHandler
    public void onUpdateWalking(UpdateWalkingEvent event) {
        if (!mc.player.isOnGround() || HoleKick.isInWeb(mc.player) || event.isPost()) {
            return;
        }
        if (HoleKick.isInWeb(mc.player)){
            return;
        }
        if (antiLag.getValue()) {
            if (!BlockUtil.getState(EntityUtil.getPlayerPos(true).down()).blocksMovement()) return;
        }
        int oldSlot = mc.player.getInventory().selectedSlot;
        int block;
        if ((block = getBlock()) == -1) {
            CommandManager.sendChatMessage("\u00a7e[?] \u00a7c\u00a7oObsidian" + (enderChest.getValue() ? "/EnderChest" : "") + "?");
            disable();
            return;
        }
        progress = 0;
        placePos.clear();
        double offset = CombatSetting.getOffset();
        BlockPos pos1 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 0.5, mc.player.getZ() + offset);
        BlockPos pos2 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() + 0.5, mc.player.getZ() + offset);
        BlockPos pos3 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 0.5, mc.player.getZ() - offset);
        BlockPos pos4 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() + 0.5, mc.player.getZ() - offset);
        BlockPos pos5 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 1.5, mc.player.getZ() + offset);
        BlockPos pos6 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() + 1.5, mc.player.getZ() + offset);
        BlockPos pos7 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 1.5, mc.player.getZ() - offset);
        BlockPos pos8 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() + 1.5, mc.player.getZ() - offset);
        BlockPos playerPos = EntityUtil.getPlayerPos(true);
        boolean headFill = false;
        if (!canPlace(pos1) && !canPlace(pos2) && !canPlace(pos3) && !canPlace(pos4)) {
            headFill = true;
            if (!this.headFill.getValue() || !canPlace(pos5) && !canPlace(pos6) && !canPlace(pos7) && !canPlace(pos8)) {
                if (!wait.getValue()) disable();
                return;
            }
        }
        boolean above = false;
        BlockPos headPos = EntityUtil.getPlayerPos(true).up(2);
        boolean rotate = this.rotate.getValue() == RotateMode.Normal;
        CombatUtil.attackCrystal(pos1, rotate, false);
        CombatUtil.attackCrystal(pos2, rotate, false);
        CombatUtil.attackCrystal(pos3, rotate, false);
        CombatUtil.attackCrystal(pos4, rotate, false);
        if (!mc.player.isOnGround()) {
            return;
        }
        if (headFill || mc.player.isInSneakingPose() || Trapped(headPos) || Trapped(headPos.add(1, 0, 0)) || Trapped(headPos.add(-1, 0, 0)) || Trapped(headPos.add(0, 0, 1)) || Trapped(headPos.add(0, 0, -1)) || Trapped(headPos.add(1, 0, -1)) || Trapped(headPos.add(-1, 0, -1)) || Trapped(headPos.add(1, 0, 1)) || Trapped(headPos.add(-1, 0, 1))) {
            above = true;
            if (!fakeMove.getValue()) {
                if (!wait.getValue()) disable();
                return;
            }
            boolean moved = false;
            BlockPos offPos = playerPos;
            if (checkSelf(offPos) && !canReplace(offPos) && (!this.headFill.getValue() || !canReplace(offPos.up()))) {
                gotoPos(offPos);
            } else {
                for (final Direction facing : Direction.values()) {
                    if (facing == Direction.UP || facing == Direction.DOWN) continue;
                    offPos = playerPos.offset(facing);
                    if (checkSelf(offPos) && !canReplace(offPos) && (!this.headFill.getValue() || !canReplace(offPos.up()))) {
                        gotoPos(offPos);
                        moved = true;
                        break;
                    }
                }
                if (!moved) {
                    for (final Direction facing : Direction.values()) {
                        if (facing == Direction.UP || facing == Direction.DOWN) continue;
                        offPos = playerPos.offset(facing);
                        if (checkSelf(offPos)) {
                            gotoPos(offPos);
                            moved = true;
                            break;
                        }
                    }
                    if (!moved) {
                        if (!center.getValue()) {
                            return;
                        }
                        for (final Direction facing : Direction.values()) {
                            if (facing == Direction.UP || facing == Direction.DOWN) continue;
                            offPos = playerPos.offset(facing);
                            if (canGoto(offPos)) {
                                gotoPos(offPos);
                                moved = true;
                                break;
                            }
                        }
                        if (!moved) {
                            if (!wait.getValue()) disable();
                            return;
                        }
                    }
                }
            }
        } else {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.4199999868869781, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.7531999805212017, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.9999957640154541, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.1661092609382138, mc.player.getZ(), false));
        }
        doSwap(block);
        if (this.rotate.getValue() == RotateMode.Bypass) {
            event.cancelRotate();
            EntityUtil.sendYawAndPitch(Nullpoint.ROTATE.rotateYaw, 90);
        }
        if (!mc.player.isOnGround()) {
            return;
        }
        if(helper.getValue()) placeBlock(playerPos.down(), rotate);
        placeBlock(playerPos, rotate);
        if(helper.getValue()) placeBlock(pos1.down(), rotate);
        placeBlock(pos1, rotate);
        if(helper.getValue()) placeBlock(pos2.down(), rotate);
        placeBlock(pos2, rotate);
        if(helper.getValue()) placeBlock(pos3.down(), rotate);
        placeBlock(pos3, rotate);
        if(helper.getValue()) placeBlock(pos4.down(), rotate);
        placeBlock(pos4, rotate);
        if (this.headFill.getValue() && above) {
            placeBlock(pos5, rotate);
            placeBlock(pos6, rotate);
            placeBlock(pos7, rotate);
            placeBlock(pos8, rotate);
        }
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.syncInventory();
        } else {
            doSwap(oldSlot);
        }
        switch (above ? aboveLagMode.getValue() : lagMode.getValue()) {
            case Smart -> {
                ArrayList<BlockPos> list = new ArrayList<>();
                for (double x = mc.player.getPos().getX() - smartX.getValue(); x < mc.player.getPos().getX() + smartX.getValue(); ++x) {
                    for (double z = mc.player.getPos().getZ() - smartX.getValue(); z < mc.player.getPos().getZ() + smartX.getValue(); ++z) {
                        for (double y = mc.player.getPos().getY() - smartDown.getValue(); y < mc.player.getPos().getY() + smartUp.getValue(); ++y) {
                            list.add(new BlockPosX(x, y, z));
                        }
                    }
                }

                double distance = 0;
                BlockPos bestPos = null;
                for (BlockPos pos : list) {
                    if (!canGoto(pos)) continue;
                    if (MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.toCenterPos().add(0, -0.5, 0))) < smartDistance.getValue()) continue;
                    if (bestPos == null || mc.player.squaredDistanceTo(pos.toCenterPos()) < distance) {
                        bestPos = pos;
                        distance = mc.player.squaredDistanceTo(pos.toCenterPos());
                    }
                }
                if (bestPos != null) {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(bestPos.getX() + 0.5, bestPos.getY(), bestPos.getZ() + 0.5, false));
                }
            }
            case Invalid -> {
                //for (int i = 0; i < 20; i++)
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1337, mc.player.getZ(), false));
            }
            case Fly -> {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.16610926093821, mc.player.getZ(), false));
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.170005801788139, mc.player.getZ(), false));
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.2426308013947485, mc.player.getZ(), false));
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 2.3400880035762786, mc.player.getZ(), false));
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 2.6400880035762786, mc.player.getZ(), false));
            }
            case TrollHack -> mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 2.3400880035762786, mc.player.getZ(), false));
            case Normal -> mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.9, mc.player.getZ(), false));
            case ToVoid -> mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), -70, mc.player.getZ(), false));
            case Rotation -> {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(-180, -90, false));
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(180, 90, false));
            }
        }
        disable();
    }


    private void placeBlock(BlockPos pos, boolean rotate) {
        if (canPlace(pos) && !placePos.contains(pos) && progress < blocksPer.getValueInt()) {
            placePos.add(pos);
            progress++;
            Direction side;
            if ((side = BlockUtil.getPlaceSide(pos)) == null) return;
            BlockUtil.placedPos.add(pos);
            if (sound.getValue()) mc.world.playSound(mc.player, pos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
            BlockUtil.clickBlock(pos.offset(side), side.getOpposite(), rotate, packetPlace.getValue());
        }
    }

    private void doSwap(int slot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private void gotoPos(BlockPos offPos) {
        if (rotate.getValue() == RotateMode.None) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(offPos.getX() + 0.5, mc.player.getY() + 0.2, offPos.getZ() + 0.5, false));
        } else {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(offPos.getX() + 0.5, mc.player.getY() + 0.2, offPos.getZ() + 0.5, Nullpoint.ROTATE.rotateYaw, 90, false));
        }
    }

    private boolean canGoto(BlockPos pos) {
        return mc.world.isAir(pos) && mc.world.isAir(pos.up());
    }

    public boolean canPlace(BlockPos pos) {
        if (noSelfPos.getValue() && EntityUtil.getPlayerPos().equals(pos)) {
            return false;
        }
        if (BlockUtil.getPlaceSide(pos) == null) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        if(BurrowAssist.INSTANCE.isOn() && BurrowAssist.INSTANCE.mcheck.getValue()) {
            for (MineManager.BreakData breakData : new HashMap<>(Nullpoint.BREAK.breakMap).values()) {
                if (breakData == null || breakData.getEntity() == null) continue;
                if(pos.equals(breakData.pos) && breakData.getEntity() != mc.player){
                    return false;
                }
            }
        }
        return !hasEntity(pos);
    }


    private boolean hasEntity(BlockPos pos) {
        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
            if (entity == mc.player) continue;
            if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof ExperienceBottleEntity || entity instanceof ArrowEntity || entity instanceof EndCrystalEntity && breakCrystal.getValue() || entity instanceof ArmorStandEntity && CombatSetting.INSTANCE.obsMode.getValue())
                continue;
            return true;
        }
        return false;
    }

    private boolean checkSelf(BlockPos pos) {
        return mc.player.getBoundingBox().intersects(new Box(pos));
    }

    private boolean Trapped(BlockPos pos) {
        return mc.world.canCollide(mc.player, new Box(pos)) && checkSelf(pos.down(2));
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

    private enum RotateMode {Bypass, Normal, None}

    private enum LagBackMode {Smart, Invalid, TrollHack, ToVoid, Normal, Rotation, Fly}
}