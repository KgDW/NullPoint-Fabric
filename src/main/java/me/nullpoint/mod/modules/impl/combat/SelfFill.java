package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.managers.MineManager;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static me.nullpoint.api.utils.world.BlockUtil.canReplace;

public class SelfFill extends Module {

    public SelfFill() {
        super("SelfFill", Category.Exploit);
    }

    public final EnumSetting<Page> page = add
            (new EnumSetting<>("Page", Page.Misc));

    //Misc
    private final BooleanSetting OnGround =
            add(new BooleanSetting("OnGround", true, v -> page.getValue() == Page.Misc));

    private final BooleanSetting antiLag =
            add(new BooleanSetting("AntiLag", true, v -> page.getValue() == Page.Misc));

    private final BooleanSetting noSelfPos =
            add(new BooleanSetting("NoSelfPos", false, v -> page.getValue() == Page.Misc));

    private final BooleanSetting breakCrystal =
            add(new BooleanSetting("BreakCrystal", true, v -> page.getValue() == Page.Misc));

    private final BooleanSetting wait =
            add(new BooleanSetting("Wait", true, v -> page.getValue() == Page.Misc));

    private final BooleanSetting headFill =
            add(new BooleanSetting("HeadFill", true, v -> page.getValue() == Page.Misc));

    //RotateBypass
    private final EnumSetting<RotateMode> rotate =
            add(new EnumSetting<>("RotateMode", RotateMode.Bypass , v -> page.getValue() == Page.RotateBypass));

    //Move
    private final BooleanSetting fakeMove =
            add(new BooleanSetting("FakeMove", true , v -> page.getValue() == Page.Move).setParent());
    private final BooleanSetting center =
            add(new BooleanSetting("AllowCenter", false, v -> page.getValue() == Page.Move && fakeMove.isOpen()));

    //Packet
    public final EnumSetting<Mode> Packet = add
            (new EnumSetting<>("LagMode", Mode.Au, v -> page.getValue() == Page.Packet));

    private final SliderSetting smartX =
            add(new SliderSetting("SmartXZ", 3, 0, 10, 0.1, v -> Packet.getValue() == Mode.Smart));

    private final SliderSetting smartUp =
            add(new SliderSetting("SmartUp", 3, 0, 10, 0.1, v ->   Packet.getValue() == Mode.Smart));

    private final SliderSetting smartDown =
            add(new SliderSetting("SmartDown", 3, 0, 10, 0.1, v -> Packet.getValue() == Mode.Smart));

    private final SliderSetting smartDistance =
            add(new SliderSetting("SmartDistance", 2, 0, 10, 0.1, v -> Packet.getValue() == Mode.Smart));



    private final SliderSetting off =
            add(new SliderSetting("INLagOff", 2, 0, 10, 0.1, v -> Packet.getValue() == Mode.Invalid));

    private final BooleanSetting Bypass =
            add(new BooleanSetting("INBypass", false, v -> Packet.getValue() == Mode.Invalid));

    private final SliderSetting offAu =
            add(new SliderSetting("AULagOff", 2, 0, 10, 0.1, v -> Packet.getValue() == Mode.Au));

    private final BooleanSetting BypassAu =
            add(new BooleanSetting("AUBypass", false, v -> Packet.getValue() == Mode.Au));

    private final SliderSetting offTo =
            add(new SliderSetting("VDLagOff", 108, 0, 109, 0.1, v -> Packet.getValue() == Mode.ToVoid));

    private final BooleanSetting BypassTo =
            add(new BooleanSetting("VDBypass", false, v -> Packet.getValue() == Mode.ToVoid));
    private final BooleanSetting self =
            add(new BooleanSetting("SelfLag", false, v -> Packet.getValue() == Mode.Nu));


    private final SliderSetting offAuto =
            add(new SliderSetting("LagOff", 1, -1, 2, 0.1, v -> Packet.getValue() == Mode.Auto));


    private final BooleanSetting ScannerY =
            add(new BooleanSetting("Scanner", false, v -> Packet.getValue() == Mode.Scanner));

    private final BooleanSetting tick =
            add(new BooleanSetting("TickLag", false, v -> Packet.getValue() == Mode.Scanner));

    private final SliderSetting AACOff =
            add(new SliderSetting("AACLagOff", 2, -10, 10, 0.1, v -> Packet.getValue() == Mode.Scanner));

    //FakeJump
    public final EnumSetting<FakeJumpMode> Jump = add
            (new EnumSetting<>("LagMode", FakeJumpMode.Normal, v -> page.getValue() == Page.FakeJump));

    //Place
    private final SliderSetting blocksPer =
            add(new SliderSetting("BlocksPer", 4, 1, 9, 1, v -> page.getValue() == Page.Place));

    private final BooleanSetting enderChest =
            add(new BooleanSetting("EnderChest", true, v -> page.getValue() == Page.Place));

    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true, v -> page.getValue() == Page.Place));
    public final BooleanSetting helper =
            add(new BooleanSetting("Helper", true, v -> page.getValue() == Page.Place));

    private final BooleanSetting packetPlace =
            add(new BooleanSetting("PacketPlace", true, v -> page.getValue() == Page.Place));

    private final BooleanSetting sound =
            add(new BooleanSetting("Sound", true, v -> page.getValue() == Page.Place));

    private int progress = 0;

    private final List<BlockPos> placePos = new ArrayList<>();

    public enum Page {
        Packet,
        Place,
        Misc,
        RotateBypass,
        Move,
        FakeJump,
    }
    public enum Mode {
        Xin,  //完成
        Auto,  //完成
        Scanner, //完成
        Nu,  //完成
        Au,  //完成
        Invalid, //完成
        Smart,  //完成
        ToVoid, //完成
    }

    private enum RotateMode {
        Bypass,
        Normal,
        None
    }

    private enum FakeJumpMode {
        NullPoint,
        M7,
        Normal
    }
    @EventHandler
    public void onUpdateWalking(UpdateWalkingEvent event) {
     Misc(event);
     FakeJump(event);
     LagMode();
     //添加DisableMode或者自己选择
     disable();
    }


    private void Misc(UpdateWalkingEvent event) {
        //Misc
        //先检测玩家isOnGround
        if (OnGround.getValue()) {
            if (mc.player != null && (!mc.player.isOnGround()
                    || HoleKick.isInWeb(mc.player)
                    || event.isPost())) {
                return;
            }
        }
        //AntiLag
        if (antiLag.getValue()) {
            if (!BlockUtil.getState(EntityUtil.getPlayerPos(true).down()).blocksMovement()) return;
        }

        if (antiLag.getValue()) {
            if (mc.player != null) {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(-180, -89, false));
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(180, 89, false));
            }
        }
    }

    private void FakeJump(UpdateWalkingEvent event) {
        //NullPoint FakeJump
        switch (Jump.getValue()) {
            case NullPoint -> {
                int oldSlot = 0;
                if (mc.player != null) {
                    oldSlot = mc.player.getInventory().selectedSlot;
                }
                int block;
                if ((block = getBlock()) == -1) {
                    CommandManager.sendChatMessage("§e[?] §c§oObsidian" + (enderChest.getValue() ? "/EnderChest" : "") + "?");
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

            }
            case Normal -> {
                    int oldSlot = 0;
                    if (mc.player != null) {
                        oldSlot = mc.player.getInventory().selectedSlot;
                    }
                    int block;
                    if ((block = getBlock()) == -1) {
                        CommandManager.sendChatMessage("§e[?] §c§oObsidian" + (enderChest.getValue() ? "/EnderChest" : "") + "?");
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
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.41999998688698, mc.player.getZ(), false));
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.75319998052119, mc.player.getZ(), false));
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.00133597911214, mc.player.getZ(), false));
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.15610926093821, mc.player.getZ(), false));
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
            }
            case M7 -> {
                int oldSlot = 0;
                if (mc.player != null) {
                    oldSlot = mc.player.getInventory().selectedSlot;
                }
                int block;
                if ((block = getBlock()) == -1) {
                    CommandManager.sendChatMessage("§e[?] §c§oObsidian" + (enderChest.getValue() ? "/EnderChest" : "") + "?");
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
                    BlockPos offPoss = playerPos;
                    List<Vec3d> offsets = new LinkedList<>();
                    Vec3d offVec3 = getVec3dDirection(offPoss);
                    offsets.add(new Vec3d(mc.player.getX() + (offVec3.x * 0.42132d), mc.player.getY() + 0.12160004615784d, mc.player.getZ() + (offVec3.z * 0.42132d)));
                    offsets.add(new Vec3d(mc.player.getX() + (offVec3.x * 0.95d), mc.player.getY() + 0.200000047683716d, mc.player.getZ() + (offVec3.z * 0.95d)));
                    offsets.add(new Vec3d(mc.player.getX() + (offVec3.x * 1.03d), mc.player.getY() + 0.200000047683716d, mc.player.getZ() + (offVec3.z * 1.03d)));
                    offsets.add(new Vec3d(mc.player.getX() + (offVec3.x * 1.0933d), mc.player.getY() + 0.12160004615784d, mc.player.getZ() + (offVec3.z * 1.0933d)));
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
            }
        }
    }

    public Vec3d getVec3dDirection(BlockPos burBlockPos) {
        BlockPos playerPos = null;
        if (mc.player != null) {
            playerPos = mc.player.getBlockPos();
        }
        Vec3d centerPos = burBlockPos.toCenterPos();
        Vec3d subtracted = mc.player.getPos().subtract(centerPos);
        Vec3d off = Vec3d.ZERO;
        if (Math.abs(subtracted.x) >= Math.abs(subtracted.z) && Math.abs(subtracted.x) > 0.2) {
            if (subtracted.x > 0.0) {
                off = new Vec3d(0.8 - subtracted.x, 0.0, 0.0);
            } else {
                off = new Vec3d(-0.8 - subtracted.x, 0.0, 0.0);
            }
        } else if (Math.abs(subtracted.z) >= Math.abs(subtracted.x) && Math.abs(subtracted.z) > 0.2) {
            if (subtracted.z > 0.0) {
                off = new Vec3d(0.0, 0.0, 0.8 - subtracted.z);
            } else {
                off = new Vec3d(0.0, 0.0, -0.8 - subtracted.z);
            }
        } else if (burBlockPos.equals(playerPos)) {
            List<Direction> facList = new ArrayList<>();
            for (Direction dir : Direction.values()) {
                if (dir == Direction.UP || dir == Direction.DOWN) continue;

                if (!solid(playerPos.offset(dir)) && !solid(playerPos.offset(dir).offset(Direction.UP))) {
                    facList.add(dir);
                }
            }
            Vec3d vec3d = Vec3d.ZERO;
            Vec3d[] offVec1 = new Vec3d[1];
            Vec3d[] offVec2 = new Vec3d[1];
            facList.sort((dir1, dir2) -> {
                offVec1[0] = vec3d.add(new Vec3d(dir1.getOffsetX(), dir1.getOffsetY(), dir1.getOffsetZ()).multiply(0.5));
                offVec2[0] = vec3d.add(new Vec3d(dir2.getOffsetX(), dir2.getOffsetY(), dir2.getOffsetZ()).multiply(0.5));
                return (int) (Math.sqrt(mc.player.squaredDistanceTo(offVec1[0].x, mc.player.getY(), offVec1[0].z)) - Math.sqrt(mc.player.squaredDistanceTo(offVec2[0].x, mc.player.getY(), offVec2[0].z)));
            });
            if (facList.size() > 0) {
                off = new Vec3d(facList.get(0).getOffsetX(), facList.get(0).getOffsetY(), facList.get(0).getOffsetZ());
            }
        }
        return off;
    }

    //Lag
    public void LagMode(){

        //全部没写完 别急
        switch (Packet.getValue()) {
            case Smart -> {
                ArrayList<BlockPos> list = new ArrayList<>();
                assert mc.player != null;
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
                if (mc.player != null) {
                    boolean DefOffset = true;
                    if (mc.player.getY() >= 3.0) {
                        for (int i = -10; i < 10; ++i) {
                            if (i == -1) {
                                i = 3;
                            }
                            if (mc.world != null
                                    && mc.world.getBlockState(getFlooredPosition( mc.player).add(0, i, 0)).getBlock().equals(Blocks.AIR)
                                    && mc.world.getBlockState(getFlooredPosition(mc.player).add(0, i + 1, 0)).getBlock().equals(Blocks.AIR)) {
                                final BlockPos poss = getFlooredPosition(mc.player).add(0, i, 0);
                                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(poss.getX() + 0.3, (double) poss.getY(), poss.getZ() + 0.3, true));
                                DefOffset = false;
                                break;
                            }
                        }
                    }
                    if (DefOffset) {
                        sendPlayerPacket(0,6,0,false);
                    }
                    if (Bypass.getValue()) {
                        sendPlayerPacket(0, off.getValue(), 0, false);
                    }
                }
            }
            case Au -> {
                Vec3i clip = getClip();
                if (mc.player != null) {
                    sendPlayerPacket(mc.player.getX()+clip.getX(),  mc.player.getY()+clip.getY(), mc.player.getZ()+clip.getZ(),true);
                }

             if (BypassAu.getValue()){
                 sendPlayerPacket(0,0 + offAu.getValue(),0 ,false);
            }

        }
            case ToVoid -> {
                if (mc.player != null) {
              if(BypassTo.getValue()) {
                  sendPlayerPacket(0, -109, 0, true);
              }
                  sendPlayerPacket(0, 0+offTo.getValue(), 0, false);
                }
            }

            case Nu -> {
                sendPlayerPacket(0, 1.16610926093821, 0, false);
                sendPlayerPacket(0, 1.170005801788139, 0, false);
                sendPlayerPacket(0, 1.2426308013947485, 0, false);
                sendPlayerPacket(0, 2.3400880035762786, 0, false);
                sendPlayerPacket(0, 2.640088003576279, 0, true);
                if (self.getValue()) {
                    sendPlayerPacket(0, sendPackets(), 0, false);
                }
            }

            case Auto -> {
                if (mc.player != null) {
                    final BlockPos pos = getFlooredPosition(mc.player).add(0, 3, 0);
                    sendPlayerPacket(pos.getX() + 0.2, pos.getY() + offAuto.getValue(), pos.getZ() + 0.2, false);
                    sendPlayerPacket(0, auto(), 0, false);
                }
            }

            case Xin -> {
                ArrayList<BlockPos> list = new ArrayList<>();
                assert mc.player != null;
                for (double x = mc.player.getPos().getX() - 3; x < mc.player.getPos().getX() + 3; ++x) {
                    for (double z = mc.player.getPos().getZ() - 3; z < mc.player.getPos().getZ() + 3; ++z) {
                        for (double y = mc.player.getPos().getY() - 3; y < mc.player.getPos().getY() + 3; ++y) {
                            list.add(new BlockPosX(x, y, z));
                        }
                    }
                }

                double distance = 0;
                BlockPos bestPos = null;
                for (BlockPos pos : list) {
                    if (!canGoto(pos)) continue;
                    if (MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.toCenterPos().add(0, -0.5, 0))) < 2) continue;
                    if (bestPos == null || mc.player.squaredDistanceTo(pos.toCenterPos()) < distance) {
                        bestPos = pos;
                        distance = mc.player.squaredDistanceTo(pos.toCenterPos());
                    }
                }
                if (bestPos != null) {
                    sendPlayerPacket(bestPos.getX() + 0.5, bestPos.getY(), bestPos.getZ() + 0.5, false);
                }
            }

            case Scanner -> {
                Vec3i clip = getClip();
                if (mc.player != null) {
                    final BlockPos pos = getFlooredPosition(mc.player).add(0, (int) (clip.getY() + AACOff.getValue()), 0);
                    sendPlayerPacket(pos.getX(), pos.getY(), pos.getZ(), true);
                    if(ScannerY.getValue()) {
                        sendPlayerPacket(pos.getX() + 0.3, pos.getY(), pos.getZ() + 0.3, true);
                    }
                    if(tick.getValue()) {
                        final BlockPos poss = getFlooredPosition(mc.player).add(0, (int) (clip.getY() + AACOff.getValue()), 0);
                        sendPlayerPacket(poss.getX() + 0.3, poss.getY(), poss.getZ() + 0.3, true);
                        sendPlayerPacket(0,  6, 0, false);
                    }
                }
            }
        }
    }
    //Packet
    public static boolean solid(BlockPos blockPos) {
        Block block = null;
        if (mc.world != null) {
            block = mc.world.getBlockState(blockPos).getBlock();
        }
        return !(block instanceof AbstractFireBlock || block instanceof FluidBlock || block instanceof AirBlock);
    }

    private BlockPos playerPos(PlayerEntity targetEntity) {
        return new BlockPos((int) Math.floor(targetEntity.getX()), (int) Math.round(targetEntity.getY()), (int) Math.floor(targetEntity.getZ()));
    }

    public double auto() {
        if (mc.player != null && solid(playerPos(mc.player).multiply(3))) {
            return 1.2d;
        }
        for (int i = 4; i < 6; i++) {
            if (solid(playerPos(mc.player).multiply(i))) {
                return (2.2d + i) - 4.0d;
            }
        }
        return 8.0d;
    }

    public double sendPackets() {
        if (mc.player != null && solid(playerPos(mc.player).multiply(2))) {
            return 0.0;
        }
        if (mc.player != null && solid(playerPos(mc.player).multiply(3))) {
            return 1.2d;
        }
        for (int i = 4; i < 5; i++) {
            if (solid(playerPos(mc.player).multiply(i))) {
                return (2.2d + i) - 4.0d;
            }
        }
        for (int i = 6; i < 7; i++) {
            if (solid(playerPos(mc.player).multiply(i))) {
                return 3.1 + i - 6.0;
            }
        }

        return 8.0d;
    }
    private void sendPlayerPacket(double x, double y,double z,boolean onGround)
    {
        if (mc.player != null) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround));
        }
    }
    public static BlockPos getPlayerPosFloored() {
        if (mc.player != null) {
            return new BlockPos((int) Math.floor(mc.player.getX()), (int) Math.floor(mc.player.getY()), (int) Math.floor(mc.player.getZ()));
        }
        return null;
    }
    public static BlockPos getPlayerPos(PlayerEntity player) {
        return new BlockPos((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()));
    }

    public static BlockPos getPlayerPosFloored(final Entity p_Player)
    {
        return new BlockPos((int) Math.floor(p_Player.getX()), (int) Math.floor(p_Player.getY()), (int) Math.floor(p_Player.getZ()));
    }
    public static BlockPos getPlayerPosFloored(final Vec3d pos, double h)
    {
        return new BlockPos((int) Math.floor(pos.x), (int) Math.floor(pos.y+h), (int) Math.floor(pos.z));
    }

    public static BlockPos getPlayerPosFloored(final Entity p_Player , double h)
    {
        return new BlockPos((int) Math.floor(p_Player.getX()), (int) Math.floor(p_Player.getY() + h), (int) Math.floor(p_Player.getZ()));
    }

    public static BlockPos getFlooredPosition(final Entity entity) {
        return new BlockPos((int) Math.floor(entity.getX()), (int) Math.round(entity.getY()), (int) Math.floor(entity.getZ()));
    }

    Vec3i getClip(){
        BlockPos c = null;
        if (mc.player != null) {
            c = getPlayerPosFloored(mc.player);
        }

        if (c != null && isSelfBurrowClipPos(c.add(0, 2, 0))) return new Vec3i(0, 2, 0);
        if (c != null && isSelfBurrowClipPos(c.add(1, 1, 0))) return new Vec3i(1, 1, 0);
        if (c != null && isSelfBurrowClipPos(c.add(-1, 1, 0))) return new Vec3i(-1, 1, 0);
        if (c != null && isSelfBurrowClipPos(c.add(0, 1, 1))) return new Vec3i(0, 1, 1);
        if (c != null && isSelfBurrowClipPos(c.add(0, 1, -1))) return new Vec3i(0, 1, -1);

        return new Vec3i(0,2,0);
    }
    boolean isSelfBurrowClipPos (BlockPos p){
        if (mc.world != null) {
            return (!mc.world.getBlockState(p).isSolid() && !mc.world.getBlockState(p.add(0,1,0)).isSolid());
        }
        return false;
    }


    //Misc
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
        if (mc.world != null) {
            for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
                if (entity == mc.player) continue;
                if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof ExperienceBottleEntity || entity instanceof ArrowEntity || entity instanceof EndCrystalEntity && breakCrystal.getValue() || entity instanceof ArmorStandEntity && CombatSetting.INSTANCE.obsMode.getValue())
                    continue;
                return true;
            }
        }
        return false;
    }

    private boolean Trapped(BlockPos pos) {
        if (mc.world != null) {
            return mc.world.canCollide(mc.player, new Box(pos)) && checkSelf(pos.down(2));
        }
        return false;
    }

    private boolean checkSelf(BlockPos pos) {
        if (mc.player != null) {
            return mc.player.getBoundingBox().intersects(new Box(pos));
        }
        return false;
    }

    private void gotoPos(BlockPos offPos) {
        if (rotate.getValue() == RotateMode.None) {
            if (mc.player != null) {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(offPos.getX() + 0.5, mc.player.getY() + 0.2, offPos.getZ() + 0.5, false));
            }
        } else {
            if (mc.player != null) {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(offPos.getX() + 0.5, mc.player.getY() + 0.2, offPos.getZ() + 0.5, Nullpoint.ROTATE.rotateYaw, 90, false));
            }
        }
    }

    private boolean canGoto(BlockPos pos) {
        if (mc.world != null) {
            return mc.world.isAir(pos) && mc.world.isAir(pos.up());
        }
        return false;
    }

    //Place
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

    private void doSwap(int slot) {
        if (inventory.getValue()) {
            if (mc.player != null) {
                InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
            }
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private void placeBlock(BlockPos pos, boolean rotate) {
        if (canPlace(pos) && !placePos.contains(pos) && progress < blocksPer.getValueInt()) {
            placePos.add(pos);
            progress++;
            Direction side;
            if ((side = BlockUtil.getPlaceSide(pos)) == null) return;
            BlockUtil.placedPos.add(pos);
            if (sound.getValue()) if (mc.world != null) {
                mc.world.playSound(mc.player, pos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
            }
            BlockUtil.clickBlock(pos.offset(side), side.getOpposite(), rotate, packetPlace.getValue());
        }
    }
}
