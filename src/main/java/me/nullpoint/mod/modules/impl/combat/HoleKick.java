package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class HoleKick extends Module {
    private final BooleanSetting rotate = add(new BooleanSetting("Rotate", true));
    public final BooleanSetting yawDeceive = add(new BooleanSetting("YawDeceive", true));

    public static HoleKick INSTANCE;
    private final BooleanSetting pistonPacket = add(new BooleanSetting("PistonPacket", false));
    private final BooleanSetting powerPacket = add(new BooleanSetting("PowerPacket", true));
    private final BooleanSetting noEating = add(new BooleanSetting("EatingPause", true));
    private final BooleanSetting attackCrystal = add(new BooleanSetting("BreakCrystal", true));
    private final BooleanSetting mine = add(new BooleanSetting("Mine", true));
    private final BooleanSetting allowWeb = add(new BooleanSetting("AllowWeb", true));
    private final SliderSetting updateDelay = add(new SliderSetting("Delay", 100, 0, 1000));
    private final BooleanSetting selfGround = add(new BooleanSetting("SelfGround", true));
    private final BooleanSetting onlyGround = add(new BooleanSetting("OnlyGround", false));
    private final BooleanSetting autoDisable = add(new BooleanSetting("AutoDisable", true));
    private final SliderSetting range = add(new SliderSetting("Range", 5.0, 0.0, 6.0));
    private final SliderSetting placeRange = add(new SliderSetting("PlaceRange", 5.0, 0.0, 6.0));
    private final SliderSetting surroundCheck = add(new SliderSetting("SurroundCheck", 2, 0, 4));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true));
    private final Timer timer = new Timer();

    public HoleKick() {
        super("HoleKick", Category.Combat);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        AutoCrystal.INSTANCE.lastBreakTimer.reset();
        //CrystalAura.INSTANCE.faceTimer.reset();
    }

    public static void pistonFacing(Direction i) {
        if (i == Direction.EAST) {
            EntityUtil.sendYawAndPitch(-90.0f, 5.0f);
        } else if (i == Direction.WEST) {
            EntityUtil.sendYawAndPitch(90.0f, 5.0f);
        } else if (i == Direction.NORTH) {
            EntityUtil.sendYawAndPitch(180.0f, 5.0f);
        } else if (i == Direction.SOUTH) {
            EntityUtil.sendYawAndPitch(0.0f, 5.0f);
        }
    }

    static boolean isTargetHere(BlockPos pos, Entity target) {
        return new Box(pos).intersects(target.getBoundingBox());
    }

    public static boolean isInWeb(PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        for (float x : new float[]{0, 0.3F, -0.3f}) {
            for (float z : new float[]{0, 0.3F, -0.3f}) {
                for (int y : new int[]{0, 1, 2}) {
                    BlockPos pos = new BlockPosX(playerPos.getX() + x, playerPos.getY(), playerPos.getZ() + z).up(y);
                    if (isTargetHere(pos, player) && mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onUpdate() {
        if (!timer.passedMs(updateDelay.getValue())) return;
        if (selfGround.getValue() && !mc.player.isOnGround()) {
            if (autoDisable.getValue()) disable();
            return;
        }
        if (findBlock(Blocks.REDSTONE_BLOCK) == -1 || findClass(PistonBlock.class) == -1) {
            if (autoDisable.getValue()) disable();
            return;
        }
        if (noEating.getValue() && EntityUtil.isUsing())
            return;
        for (PlayerEntity player : CombatUtil.getEnemies(range.getValue())) {
            if (!canPush(player)) continue;
/*            for (Direction i : Direction.values()) {
                if (i == Direction.UP || i == Direction.DOWN) continue;
                BlockPos pos = EntityUtil.getEntityPos(player).offset(i);
                if (isTargetHere(pos, player)) {
                    if (mc.world.canCollide(player, new Box(pos))) {
                        if (tryPush(EntityUtil.getEntityPos(player).offset(i.getOpposite()), i)) {
                            timer.reset();
                            return;
                        }
                        if (tryPush(EntityUtil.getEntityPos(player).offset(i.getOpposite()).up(), i)) {
                            timer.reset();
                            return;
                        }
                    }
                }
            }*/

            float[] offset = new float[]{-0.25f, 0f, 0.25f};
            for (float x : offset) {
                for (float z : offset) {
                    BlockPosX playerPos = new BlockPosX(player.getX() + x, player.getY() + 0.5, player.getZ() + z);
                    for (Direction i : Direction.values()) {
                        if (i == Direction.UP || i == Direction.DOWN) continue;
                        BlockPos pos = playerPos.offset(i);
                        if (isTargetHere(pos, player)) {
                            if (mc.world.canCollide(player, new Box(pos))) {
                                if (tryPush(playerPos.offset(i.getOpposite()), i)) {
                                    timer.reset();
                                    return;
                                }
                                if (tryPush(playerPos.offset(i.getOpposite()).up(), i)) {
                                    timer.reset();
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            if (!mc.world.canCollide(player, new Box(new BlockPosX(player.getX(), player.getY() + 2.5, player.getZ())))) {
                for (Direction i : Direction.values()) {
                    if (i == Direction.UP || i == Direction.DOWN) continue;
                    BlockPos pos = EntityUtil.getEntityPos(player).offset(i);
                    Box box = player.getBoundingBox().offset(new Vec3d(i.getOffsetX(), i.getOffsetY(), i.getOffsetZ()));
                    if (getBlock(pos.up()) != Blocks.PISTON_HEAD && !mc.world.canCollide(player, box.offset(0, 1, 0)) && !isTargetHere(pos, player)) {
                        if (tryPush(EntityUtil.getEntityPos(player).offset(i.getOpposite()).up(), i)) {
                            timer.reset();
                            return;
                        }
                        if (tryPush(EntityUtil.getEntityPos(player).offset(i.getOpposite()), i)) {
                            timer.reset();
                            return;
                        }
                    }
                }
            }

            for (float x : offset) {
                for (float z : offset) {
                    BlockPosX playerPos = new BlockPosX(player.getX() + x, player.getY() + 0.5, player.getZ() + z);
                    for (Direction i : Direction.values()) {
                        if (i == Direction.UP || i == Direction.DOWN) continue;
                        BlockPos pos = playerPos.offset(i);
                        if (isTargetHere(pos, player)) {
                            if (tryPush(playerPos.offset(i.getOpposite()).up(), i)) {
                                timer.reset();
                                return;
                            }
                            if (tryPush(playerPos.offset(i.getOpposite()), i)) {
                                timer.reset();
                                return;
                            }
                        }
                    }
                }
            }
        }
        if (autoDisable.getValue()) disable();

    }

    private boolean tryPush(BlockPos piston, Direction direction) {
        if (!BlockUtil.isAir(piston.offset(direction))) return false;
        if (isTrueFacing(piston, direction)) {
            if (BlockUtil.clientCanPlace(piston)) {
                boolean canPower = false;
                if (BlockUtil.getPlaceSide(piston, placeRange.getValue()) != null) {
                    CombatUtil.modifyPos = piston;
                    CombatUtil.modifyBlockState = Blocks.PISTON.getDefaultState();
                    for (Direction i : Direction.values()) {
                        if (getBlock(piston.offset(i)) == Blocks.REDSTONE_BLOCK) {
                            canPower = true;
                            break;
                        }
                    }
                    for (Direction i : Direction.values()) {
                        if (canPower) break;
                        if (BlockUtil.canPlace(piston.offset(i), placeRange.getValue())) {
                            canPower = true;
                        }
                    }
                    CombatUtil.modifyPos = null;

                    if (canPower) {
                        int pistonSlot = findClass(PistonBlock.class);
                        Direction side = BlockUtil.getPlaceSide(piston);
                        if (side != null) {
                            if (rotate.getValue()) EntityUtil.facePosSide(piston.offset(side), side.getOpposite());
                            if (yawDeceive.getValue()) pistonFacing(direction.getOpposite());
                            int old = mc.player.getInventory().selectedSlot;
                            doSwap(pistonSlot);
                            BlockUtil.placeBlock(piston, false, pistonPacket.getValue());
                            if (inventory.getValue()) {
                                doSwap(pistonSlot);
                                EntityUtil.syncInventory();
                            } else {
                                doSwap(old);
                            }
                            if (rotate.getValue() && yawDeceive.getValue()) EntityUtil.facePosSide(piston.offset(side), side.getOpposite());

                            for (Direction i : Direction.values()) {
                                if (getBlock(piston.offset(i)) == Blocks.REDSTONE_BLOCK) {
                                    if (autoDisable.getValue()) {
                                        disable();
                                    }
                                    return true;
                                }
                            }
                            for (Direction i : Direction.values()) {
                                if (BlockUtil.canPlace(piston.offset(i), placeRange.getValue())) {
                                    int oldSlot = mc.player.getInventory().selectedSlot;
                                    int powerSlot = findBlock(Blocks.REDSTONE_BLOCK);
                                    doSwap(powerSlot);
                                    BlockUtil.placeBlock(piston.offset(i), rotate.getValue(), powerPacket.getValue());
                                    if (inventory.getValue()) {
                                        doSwap(powerSlot);
                                        EntityUtil.syncInventory();
                                    } else {
                                        doSwap(oldSlot);
                                    }
                                    return true;
                                }
                            }

                            return true;
                        }
                    }
                } else {
                    Direction powerFacing = null;
                    for (Direction i : Direction.values()) {
                        if (powerFacing != null) break;
                        CombatUtil.modifyPos = piston.offset(i);
                        CombatUtil.modifyBlockState = Blocks.REDSTONE_BLOCK.getDefaultState();
                        if (BlockUtil.getPlaceSide(piston) != null) {
                            powerFacing = i;
                        }
                        CombatUtil.modifyPos = null;
                        if (powerFacing != null && !BlockUtil.canPlace(piston.offset(powerFacing))) {
                            powerFacing = null;
                        }
                    }
                    if (powerFacing != null) {
                        int oldSlot = mc.player.getInventory().selectedSlot;
                        int powerSlot = findBlock(Blocks.REDSTONE_BLOCK);
                        doSwap(powerSlot);
                        BlockUtil.placeBlock(piston.offset(powerFacing), rotate.getValue(), powerPacket.getValue());
                        if (inventory.getValue()) {
                            doSwap(powerSlot);
                            EntityUtil.syncInventory();
                        } else {
                            doSwap(oldSlot);
                        }

                        CombatUtil.modifyPos = piston.offset(powerFacing);
                        CombatUtil.modifyBlockState = Blocks.REDSTONE_BLOCK.getDefaultState();
                        int pistonSlot = findClass(PistonBlock.class);
                        Direction side = BlockUtil.getPlaceSide(piston);
                        if (side != null) {
                            if (rotate.getValue()) EntityUtil.facePosSide(piston.offset(side), side.getOpposite());
                            if (yawDeceive.getValue()) pistonFacing(direction.getOpposite());
                            int old = mc.player.getInventory().selectedSlot;
                            doSwap(pistonSlot);
                            BlockUtil.placeBlock(piston, false, pistonPacket.getValue());
                            if (inventory.getValue()) {
                                doSwap(pistonSlot);
                                EntityUtil.syncInventory();
                            } else {
                                doSwap(old);
                            }
                            if (rotate.getValue() && yawDeceive.getValue()) EntityUtil.facePosSide(piston.offset(side), side.getOpposite());
                        }
                        CombatUtil.modifyPos = null;
                        return true;
                    }
                }
            }
        }
        if (getBlock(piston) instanceof PistonBlock && getBlockState(piston).get(FacingBlock.FACING) == direction) {
            for (Direction i : Direction.values()) {
                if (getBlock(piston.offset(i)) == Blocks.REDSTONE_BLOCK) {
                    if (autoDisable.getValue()) {
                        disable();
                        return true;
                    }
                    return false;
                }
            }
            for (Direction i : Direction.values()) {
                if (BlockUtil.canPlace(piston.offset(i), placeRange.getValue())) {
                    int oldSlot = mc.player.getInventory().selectedSlot;
                    int powerSlot = findBlock(Blocks.REDSTONE_BLOCK);
                    doSwap(powerSlot);
                    BlockUtil.placeBlock(piston.offset(i), rotate.getValue(), powerPacket.getValue());
                    if (inventory.getValue()) {
                        doSwap(powerSlot);
                        EntityUtil.syncInventory();
                    } else {
                        doSwap(oldSlot);
                    }
                    return true;
                }
            }
        }
        return false;
    }
    private boolean isTrueFacing(BlockPos pos, Direction facing) {
        if (yawDeceive.getValue()) return true;
        Direction side = BlockUtil.getPlaceSide(pos);
        if (side == null) return false;
        Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
        float[] rotation = EntityUtil.getLegitRotations(directionVec);
        return MathUtil.getFacingOrder(rotation[0], rotation[1]).getOpposite() == facing;
    }

    private void doSwap(int slot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }
    public int findBlock(Block blockIn) {
        if (inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(blockIn);
        } else {
            return InventoryUtil.findBlock(blockIn);
        }
    }
    public int findClass(Class clazz) {
        if (inventory.getValue()) {
            return InventoryUtil.findClassInventorySlot(clazz);
        } else {
            return InventoryUtil.findClass(clazz);
        }
    }
    private Boolean canPush(PlayerEntity player) {
        if (onlyGround.getValue() && !player.isOnGround()) return false;
        if (!allowWeb.getValue() && isInWeb(player)) return false;
        float[] offset = new float[]{-0.25f, 0f, 0.25f};

        int progress = 0;

        if (mc.world.canCollide(player, new Box(new BlockPosX(player.getX() + 1, player.getY() + 0.5, player.getZ())))) progress++;
        if (mc.world.canCollide(player, new Box(new BlockPosX(player.getX() - 1, player.getY() + 0.5, player.getZ())))) progress++;
        if (mc.world.canCollide(player, new Box(new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ() + 1)))) progress++;
        if (mc.world.canCollide(player, new Box(new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ() - 1)))) progress++;

        for (float x : offset) {
            for (float z : offset) {
                BlockPosX playerPos = new BlockPosX(player.getX() + x, player.getY() + 0.5, player.getZ() + z);
                for (Direction i : Direction.values()) {
                    if (i == Direction.UP || i == Direction.DOWN) continue;
                    BlockPos pos = playerPos.offset(i);
                    if (isTargetHere(pos, player)) {
                        if (mc.world.canCollide(player, new Box(pos))) {
                            return true;
                        }
                        if (progress > surroundCheck.getValue() - 1) {
                            return true;
                        }
                    }
                }
            }
        }

        if (!mc.world.canCollide(player, new Box(new BlockPosX(player.getX(), player.getY() + 2.5, player.getZ())))) {
            for (Direction i : Direction.values()) {
                if (i == Direction.UP || i == Direction.DOWN) continue;
                BlockPos pos = EntityUtil.getEntityPos(player).offset(i);
                Box box = player.getBoundingBox().offset(new Vec3d(i.getOffsetX(), i.getOffsetY(), i.getOffsetZ()));
                if (getBlock(pos.up()) != Blocks.PISTON_HEAD && !mc.world.canCollide(player, box.offset(0, 1, 0)) && !isTargetHere(pos, player)) {
                    if (mc.world.canCollide(player, new Box(new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ())))) {
                        return true;
                    }
                }
            }
        }

        return progress > surroundCheck.getValue() - 1 || CombatUtil.isHard(new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ()));
    }
    private Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    private BlockState getBlockState(BlockPos pos) {
        return mc.world.getBlockState(pos);
    }
}