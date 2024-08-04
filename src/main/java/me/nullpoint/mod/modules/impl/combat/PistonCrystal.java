package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.managers.RotateManager;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import static me.nullpoint.api.utils.combat.ThunderExplosionUtil.calculateDamage;
import static me.nullpoint.api.utils.world.BlockUtil.*;

public class PistonCrystal extends Module {
    public static PistonCrystal INSTANCE;

    public final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", false, v -> page.getValue() == Page.Rotate));
    private final BooleanSetting newRotate =
            add(new BooleanSetting("NewRotate", false, v -> rotate.isOpen() &&  page.getValue() == Page.Rotate));
    private final SliderSetting yawStep =
            add(new SliderSetting("YawStep", 0.3f, 0.1f, 1.0f, 0.01f, v -> rotate.isOpen() && newRotate.getValue() &&  page.getValue() == Page.Rotate));
    private final BooleanSetting packet =
            add(new BooleanSetting("Packet", false, v -> rotate.isOpen() && newRotate.getValue() &&  page.getValue() == Page.Rotate));
    private final BooleanSetting checkLook =
            add(new BooleanSetting("CheckLook", true, v -> rotate.isOpen() && newRotate.getValue() &&  page.getValue() == Page.Rotate));
    private final SliderSetting fov =
            add(new SliderSetting("Fov", 5f, 0f, 30f, v -> rotate.isOpen() && newRotate.getValue() && checkLook.getValue() &&  page.getValue() == Page.Rotate));

    public final BooleanSetting yawDeceive = add(new BooleanSetting("YawDeceive", true, v -> page.getValue() == Page.Rotate));
    private final BooleanSetting autoYaw = add(new BooleanSetting("AutoYaw", true, v -> page.getValue() == Page.Rotate));
    private final BooleanSetting preferAnchor =
            add(new BooleanSetting("PreferAnchor", true, v -> page.getValue() == Page.General));
    private final BooleanSetting preferCrystal =
            add(new BooleanSetting("PreferCrystal", true, v -> page.getValue() == Page.General));
    private final SliderSetting placeRange =
            add(new SliderSetting("PlaceRange", 5.0f, 1.0f, 8.0f, v -> page.getValue() == Page.General));
    private final SliderSetting range =
            add(new SliderSetting("TargetRange", 4.0f, 1.0f, 8.0f, v -> page.getValue() == Page.General));
    private final SliderSetting updateDelay =
            add(new SliderSetting("PlaceDelay", 100, 0, 500, v -> page.getValue() == Page.General));
    private final SliderSetting posUpdateDelay =
            add(new SliderSetting("UpdateDelay", 500, 0, 1000, v -> page.getValue() == Page.General));
    private final SliderSetting stageSetting =
            add(new SliderSetting("Stage", 4, 1, 10, v -> page.getValue() == Page.General));
    private final SliderSetting pistonStage =
            add(new SliderSetting("PistonStage", 1, 1, 10, v -> page.getValue() == Page.General));
    private final SliderSetting pistonMaxStage =
            add(new SliderSetting("PistonMaxStage", 1, 1, 10, v -> page.getValue() == Page.General));
    private final SliderSetting powerStage =
            add(new SliderSetting("PowerStage", 3, 1, 10, v -> page.getValue() == Page.General));
    private final SliderSetting powerMaxStage =
            add(new SliderSetting("PowerMaxStage", 3, 1, 10, v -> page.getValue() == Page.General));
    private final SliderSetting crystalStage =
            add(new SliderSetting("CrystalStage", 4, 1, 10, v -> page.getValue() == Page.General));
    private final SliderSetting crystalMaxStage =
            add(new SliderSetting("CrystalMaxStage", 4, 1, 10, v -> page.getValue() == Page.General));
    private final SliderSetting fireStage =
            add(new SliderSetting("FireStage", 2, 1, 10, v -> page.getValue() == Page.General));
    private final SliderSetting fireMaxStage =
            add(new SliderSetting("FireMaxStage", 2, 1, 10, v -> page.getValue() == Page.General));

    public SliderSetting speed = add(new SliderSetting("MaxSpeed", 8, 0, 20, v -> page.getValue() == Page.General));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true, v -> page.getValue() == Page.Misc));
    private final BooleanSetting endSwing =
            add(new BooleanSetting("EndSwing", true, v -> page.getValue() == Page.Misc));
    private final BooleanSetting debug =
            add(new BooleanSetting("Debug", false, v -> page.getValue() == Page.Misc));
    private final BooleanSetting fire =
            add(new BooleanSetting("Fire", true, v -> page.getValue() == Page.Misc));
    private final BooleanSetting switchPos =
            add(new BooleanSetting("Switch", false, v -> page.getValue() == Page.Misc));
    private final BooleanSetting onlyGround =
            add(new BooleanSetting("SelfGround", true, v -> page.getValue() == Page.Misc));
    private final BooleanSetting onlyStatic =
            add(new BooleanSetting("MovingPause", true, v -> page.getValue() == Page.Misc));
    private final BooleanSetting noEating = add(new BooleanSetting("NoEating", true, v -> page.getValue() == Page.Misc));
    private final BooleanSetting eatingBreak = add(new BooleanSetting("EatingBreak", false, v -> page.getValue() == Page.Misc));
    private PlayerEntity target = null;
    public Vec3d directionVec = null;
    private float lastYaw = 0;
    private float lastPitch = 0;
    public PistonCrystal() {
        super("PistonCrystal", Category.Combat);
        INSTANCE = this;
    }

    @EventHandler
    public void onRotate(RotateEvent event) {
        if (newRotate.getValue() && directionVec != null) {
            float[] newAngle = injectStep(EntityUtil.getLegitRotations(directionVec), yawStep.getValueFloat());
            if (newAngle != null) {
                lastYaw = newAngle[0];
            }
            if (newAngle != null) {
                lastPitch = newAngle[1];
            }
            event.setYaw(lastYaw);
            event.setPitch(lastPitch);
        } else {
            lastYaw = Nullpoint.ROTATE.lastYaw;
            lastPitch = Nullpoint.ROTATE.lastPitch;
        }
    }

    private final Timer timer = new Timer();
    private final Timer crystalTimer = new Timer();
    public BlockPos bestPos = null;
    public BlockPos bestOPos = null;
    public Direction bestFacing = null;
    public double distance = 100;
    public boolean getPos = false;
    public int stage = 1;


    public void onTick() {
        if (pistonStage.getValue() > stageSetting.getValue()) {
            pistonStage.setValue(stageSetting.getValue());
        }
        if (fireStage.getValue() > stageSetting.getValue()) {
            fireStage.setValue(stageSetting.getValue());
        }
        if (powerStage.getValue() > stageSetting.getValue()) {
            powerStage.setValue(stageSetting.getValue());
        }
        if (crystalStage.getValue() > stageSetting.getValue()) {
            crystalStage.setValue(stageSetting.getValue());
        }

        if (pistonMaxStage.getValue() > stageSetting.getValue()) {
            pistonMaxStage.setValue(stageSetting.getValue());
        }
        if (fireMaxStage.getValue() > stageSetting.getValue()) {
            fireMaxStage.setValue(stageSetting.getValue());
        }
        if (powerMaxStage.getValue() > stageSetting.getValue()) {
            powerMaxStage.setValue(stageSetting.getValue());
        }
        if (crystalMaxStage.getValue() > stageSetting.getValue()) {
            crystalMaxStage.setValue(stageSetting.getValue());
        }

        if (crystalMaxStage.getValue() < crystalStage.getValue()) {
            crystalStage.setValue(crystalMaxStage.getValue());
        }
        if (powerMaxStage.getValue() < powerStage.getValue()) {
            powerStage.setValue(powerMaxStage.getValue());
        }
        if (pistonMaxStage.getValue() < pistonStage.getValue()) {
            pistonStage.setValue(pistonMaxStage.getValue());
        }
        if (fireMaxStage.getValue() < fireStage.getValue()) {
            fireStage.setValue(fireMaxStage.getValue());
        }
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

    @Override
    public void onUpdate() {
        onTick();
        directionVec = null;
        target = CombatUtil.getClosestEnemy(range.getValue());
        if (target == null) {
            return;
        }
        if (preferAnchor.getValue() && AnchorAura.INSTANCE.currentPos != null) {
            return;
        }
        if (preferCrystal.getValue() && AutoCrystal.crystalPos != null) {
            return;
        }
        if (noEating.getValue() && EntityUtil.isUsing())
            return;
        if (mc.player != null && check(onlyStatic.getValue(), !mc.player.isOnGround(), onlyGround.getValue())) return;
        BlockPos pos = EntityUtil.getEntityPos(target, true);
        if (!EntityUtil.isUsing() || eatingBreak.getValue()) {
            if (checkCrystal(pos.up(0))) {
                attackCrystal(pos.up(0), rotate.getValue(), false);
            }
            if (checkCrystal(pos.up(1))) {
                attackCrystal(pos.up(1), rotate.getValue(), false);
            }
            if (checkCrystal(pos.up(2))) {
                attackCrystal(pos.up(2), rotate.getValue(), false);
            }
        }
        if (mc.world != null && bestPos != null) {
            mc.world.getBlockState(bestPos).getBlock();
        }
        if (crystalTimer.passedMs(posUpdateDelay.getValueInt())) {
            stage = 0;
            distance = 100;
            getPos = false;

            getBestPos(pos.up(2));
            getBestPos(pos.up());
        }
        if (!timer.passedMs(updateDelay.getValueInt())) return;
        if (getPos && bestPos != null) {
            timer.reset();
            if (debug.getValue()) {
                CommandManager.sendChatMessage("[Debug] PistonPos:" + bestPos + " Facing:" + bestFacing + " CrystalPos:" + bestOPos.offset(bestFacing));
            }
            if (check(onlyStatic.getValue(), !mc.player.isOnGround(), onlyGround.getValue())) return;
            doPistonAura(bestPos, bestFacing, bestOPos);
        }
    }

    public void attackCrystal(BlockPos pos, boolean rotate, boolean eatingPause) {
        for (EndCrystalEntity entity : mc.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(pos))) {
            attackCrystal(entity, rotate, eatingPause);
            break;
        }
    }

    public void attackCrystal(Entity crystal, boolean rotate, boolean usingPause) {
        if (!CombatUtil.breakTimer.passedMs((long) (CombatSetting.INSTANCE.attackDelay.getValue() * 1000))) return;
        if (usingPause && EntityUtil.isUsing())
            return;
        if (crystal != null) {
            CombatUtil.breakTimer.reset();
            if (rotate && CombatSetting.INSTANCE.attackRotate.getValue()){
                if (!faceVector(new Vec3d(crystal.getX(), crystal.getY() + 0.25, crystal.getZ()))) return;
            }
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
            mc.player.resetLastAttackedTicks();
            EntityUtil.swingHand(Hand.MAIN_HAND, CombatSetting.INSTANCE.swingMode.getValue());
        }
    }


    public boolean check(boolean onlyStatic, boolean onGround, boolean onlyGround) {
        if (MovementUtil.isMoving() && onlyStatic) return true;
        if (onGround && onlyGround) return true;
        if (findBlock(Blocks.REDSTONE_BLOCK) == -1) return true;
        if (findClass(PistonBlock.class) == -1) return true;
        return findItem(Items.END_CRYSTAL) == -1;
    }
    private boolean checkCrystal(BlockPos pos) {
        if (mc.world != null) {
            for (Entity entity : mc.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(pos))) {
                float damage = calculateDamage(entity.getPos(), target, target, 6);
                if (damage > 6) return true;
            }
        }
        return false;
    }

    private boolean checkCrystal2(BlockPos pos) {
        if (mc.world != null) {
            for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
                if (entity instanceof EndCrystalEntity && EntityUtil.getEntityPos(entity).equals(pos)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getInfo() {
        if (target != null) return target.getName().getString();
        return null;
    }

    private void getBestPos(BlockPos pos) {
        for (Direction i : Direction.values()) {
            if (i == Direction.DOWN || i == Direction.UP) continue;
            getPos(pos, i);
        }
    }

    private void getPos(BlockPos pos, Direction i) {
        if (!BlockUtil.canPlaceCrystal(pos.offset(i)) && !checkCrystal2(pos.offset(i))) return;
        getPos(pos.offset(i, 3), i, pos);
        getPos(pos.offset(i, 3).up(), i, pos);
        int offsetX = pos.offset(i).getX() - pos.getX();
        int offsetZ = pos.offset(i).getZ() - pos.getZ();
        getPos(pos.offset(i, 3).add(offsetZ, 0, offsetX), i, pos);
        getPos(pos.offset(i, 3).add(-offsetZ, 0, -offsetX), i, pos);
        getPos(pos.offset(i, 3).add(offsetZ, 1, offsetX), i, pos);
        getPos(pos.offset(i, 3).add(-offsetZ, 1, -offsetX), i, pos);

        getPos(pos.offset(i, 2), i, pos);
        getPos(pos.offset(i, 2).up(), i, pos);

        getPos(pos.offset(i, 2).add(offsetZ, 0, offsetX), i, pos);
        getPos(pos.offset(i, 2).add(-offsetZ, 0, -offsetX), i, pos);
        getPos(pos.offset(i, 2).add(offsetZ, 1, offsetX), i, pos);
        getPos(pos.offset(i, 2).add(-offsetZ, 1, -offsetX), i, pos);
    }

    private void getPos(BlockPos pos, Direction facing, BlockPos oPos) {
        if (mc.world != null && switchPos.getValue() && bestPos != null && bestPos.equals(pos) && mc.world.isAir(bestPos)) {
            return;
        }
        if (!BlockUtil.canPlace(pos, placeRange.getValue()) && !(getBlock(pos) instanceof PistonBlock)) return;
        if (findClass(PistonBlock.class) == -1) return;
        if (!(getBlock(pos) instanceof PistonBlock)) {
            if (mc.player != null && (mc.player.getY() - pos.getY() <= -2.0 || mc.player.getY() - pos.getY() >= 3.0) && BlockUtil.distanceToXZ(pos.getX() + 0.5, pos.getZ() + 0.5) < 2.6) {
                return;
            }
            if (!isTrueFacing(pos, facing)) {
                return;
            }
        }
        if (!mc.world.isAir(pos.offset(facing, -1)) || mc.world.getBlockState(pos.offset(facing, -1)).getBlock() == Blocks.FIRE || getBlock(pos.offset(facing.getOpposite())) == Blocks.MOVING_PISTON && !checkCrystal2(pos.offset(facing.getOpposite()))) {
            return;
        }
        if (!BlockUtil.canPlace(pos, placeRange.getValue()) && !isPiston(pos, facing)) {
            return;
        }
        if (!(MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos())) < distance || bestPos == null)) {
            return;
        }
        bestPos = pos;
        bestOPos = oPos;
        bestFacing = facing;
        distance = MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos()));
        getPos = true;
        crystalTimer.reset();
    }

    private void doPistonAura(BlockPos pos, Direction facing, BlockPos oPos) {
        if (stage >= stageSetting.getValue()) {
            stage = 0;
        }
        stage++;
        if (mc.world != null && mc.world.isAir(pos)) {
            if (BlockUtil.canPlace(pos)) {
                if (stage >= pistonStage.getValue() && stage <= pistonMaxStage.getValue()) {
                    Direction side = BlockUtil.getPlaceSide(pos);
                    if (side == null) {
                        return;
                    }
                    int old = 0;
                    if (mc.player != null) {
                        old = mc.player.getInventory().selectedSlot;
                    }
                    BlockPos neighbour = pos.offset(side);
                    Direction opposite = side.getOpposite();
                    if (rotate.getValue()) {
                        Vec3d hitVec = pos.toCenterPos().add(new Vec3d(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5));
                        if (!faceVector(hitVec)) return;
                    }
                    if (shouldYawCheck()) pistonFacing(facing);
                    int piston = findClass(PistonBlock.class);
                    doSwap(piston);
                    placeBlock(pos, false, endSwing.getValue());
                    if (inventory.getValue()) {
                        doSwap(piston);
                        EntityUtil.syncInventory();
                    } else {
                        doSwap(old);
                    }
                    if (rotate.getValue()) {
                        EntityUtil.facePosSide(neighbour, opposite);
                    }
                }
            } else {
                return;
            }
        }
        if (stage >= powerStage.getValue() && stage <= powerMaxStage.getValue()) {
            doRedStone(pos, facing, oPos.offset(facing));
        }
        if (stage >= crystalStage.getValue() && stage <= crystalMaxStage.getValue()) {
            placeCrystal(oPos, facing);
        }
        if (stage >= fireStage.getValue() && stage <= fireMaxStage.getValue()) {
            doFire(oPos, facing);
        }
    }

    private void placeCrystal(BlockPos pos, Direction facing) {
        if (!BlockUtil.canPlaceCrystal(pos.offset(facing))) return;
        int crystal = findItem(Items.END_CRYSTAL);
        if (crystal == -1) return;
        int old = 0;
        if (mc.player != null) {
            old = mc.player.getInventory().selectedSlot;
        }
        doSwap(crystal);
        placeCrystal(pos.offset(facing), rotate.getValue());
        if (inventory.getValue()) {
            doSwap(crystal);
            EntityUtil.syncInventory();
        } else {
            doSwap(old);
        }
    }

    public void placeCrystal(BlockPos pos, boolean rotate) {
        BlockPos obsPos = pos.down();
        Direction facing = BlockUtil.getClickSide(obsPos);
        clickBlock(obsPos, facing, rotate);
    }

    private boolean isPiston(BlockPos pos, Direction facing) {
        if (mc.world != null && !(mc.world.getBlockState(pos).getBlock() instanceof PistonBlock)) return false;
        if (mc.world.getBlockState(pos).get(FacingBlock.FACING).getOpposite() != facing) return false;
        return mc.world.isAir(pos.offset(facing, -1)) || getBlock(pos.offset(facing, -1)) == Blocks.FIRE || getBlock(pos.offset(facing.getOpposite())) == Blocks.MOVING_PISTON;
    }

    private void doFire(BlockPos pos, Direction facing) {
        if (!fire.getValue()) return;
        int fire = findItem(Items.FLINT_AND_STEEL);
        if (fire == -1) return;
        int old = 0;
        if (mc.player != null) {
            old = mc.player.getInventory().selectedSlot;
        }

        int[] xOffset = {0, facing.getOffsetZ(), -facing.getOffsetZ()};
        int[] yOffset = {0, 1};
        int[] zOffset = {0, facing.getOffsetX(), -facing.getOffsetX()};
        for (int x : xOffset) {
            for (int y : yOffset) {
                for (int z : zOffset) {
                    if (getBlock(pos.add(x, y, z)) == Blocks.FIRE) {
                        return;
                    }
                }
            }
        }
        for (int x : xOffset) {
            for (int y : yOffset) {
                for (int z : zOffset) {
                    if (canFire(pos.add(x, y, z))) {
                        doSwap(fire);
                        placeFire(pos.add(x, y, z));
                        if (inventory.getValue()) {
                            doSwap(fire);
                            EntityUtil.syncInventory();
                        } else {
                            doSwap(old);
                        }
                        return;
                    }
                }
            }
        }
    }

    public void placeFire(BlockPos pos) {
        BlockPos neighbour = pos.offset(Direction.DOWN);
        clickBlock(neighbour, Direction.UP, this.rotate.getValue());
    }

    private static boolean canFire(BlockPos pos) {
        if (BlockUtil.canReplace(pos.down())) return false;
        if (mc.world != null && !mc.world.isAir(pos)) return false;
        if (!BlockUtil.canClick(pos.offset(Direction.DOWN))) return false;
        return BlockUtil.isStrictDirection(pos.down(), Direction.UP);
    }

    private void doRedStone(BlockPos pos, Direction facing, BlockPos crystalPos) {
        if (mc.world != null && !mc.world.isAir(pos.offset(facing, -1)) && getBlock(pos.offset(facing, -1)) != Blocks.FIRE && getBlock(pos.offset(facing.getOpposite())) != Blocks.MOVING_PISTON)
            return;
        for (Direction i : Direction.values()) {
            if (getBlock(pos.offset(i)) == Blocks.REDSTONE_BLOCK) return;
        }
        int power = findBlock(Blocks.REDSTONE_BLOCK);
        if (power == -1) return;
        int old = 0;
        if (mc.player != null) {
            old = mc.player.getInventory().selectedSlot;
        }
        Direction bestNeighboring = BlockUtil.getBestNeighboring(pos, facing);
        if (bestNeighboring != null && bestNeighboring != facing.getOpposite() && BlockUtil.canPlace(pos.offset(bestNeighboring), placeRange.getValue()) && !pos.offset(bestNeighboring).equals(crystalPos)) {
            doSwap(power);
            placeBlock(pos.offset(bestNeighboring), rotate.getValue(), endSwing.getValue());
            if (inventory.getValue()) {
                doSwap(power);
                EntityUtil.syncInventory();
            } else {
                doSwap(old);
            }
            return;
        }
        for (Direction i : Direction.values()) {
            if (!BlockUtil.canPlace(pos.offset(i), placeRange.getValue()) || pos.offset(i).equals(crystalPos) || i == facing.getOpposite())
                continue;
            doSwap(power);
            placeBlock(pos.offset(i), rotate.getValue(), endSwing.getValue());
            if (inventory.getValue()) {
                doSwap(power);
                EntityUtil.syncInventory();
            } else {
                doSwap(old);
            }
            return;
        }
    }


    private boolean shouldYawCheck() {
        return yawDeceive.getValue() || (autoYaw.getValue() && !EntityUtil.isInsideBlock());
    }
    private boolean isTrueFacing(BlockPos pos, Direction facing) {
        if (shouldYawCheck()) return true;
        Direction side = BlockUtil.getPlaceSide(pos);
        if (side == null) side = Direction.UP;
        side = side.getOpposite();
        Vec3d hitVec = pos.offset(side.getOpposite()).toCenterPos().add(new Vec3d(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5));
        return Direction.fromRotation(EntityUtil.getLegitRotations(hitVec)[0]) == facing;
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
    public int findItem(Item itemIn) {
        if (inventory.getValue()) {
            return InventoryUtil.findItemInventorySlot(itemIn);
        } else {
            return InventoryUtil.findItem(itemIn);
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
    private Block getBlock(BlockPos pos) {
        if (mc.world != null) {
            return mc.world.getBlockState(pos).getBlock();
        }
        return null;
    }

    public void placeBlock(BlockPos pos, boolean rotate, boolean bypass) {
        if (airPlace()) {
            for (Direction i : Direction.values()) {
                if (mc.world != null && mc.world.isAir(pos.offset(i))) {
                    clickBlock(pos, i, rotate);
                    return;
                }
            }
        }
        Direction side = getPlaceSide(pos);
        if (side == null) return;
        Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
        EntityUtil.swingHand(Hand.MAIN_HAND, CombatSetting.INSTANCE.swingMode.getValue());
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        placedPos.add(pos);
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
        if (bypass)
            EntityUtil.swingHand(Hand.MAIN_HAND, CombatSetting.INSTANCE.swingMode.getValue());
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

        if (angle != null) {
            return new float[]{
                    angle[0],
                    angle[1]
            };
        }
        return null;
    }

    public boolean clickBlock(BlockPos pos, Direction side, boolean rotate) {
        Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
        if (rotate) {
            if (!faceVector(directionVec)) return false;
        }
        EntityUtil.swingHand(Hand.MAIN_HAND, CombatSetting.INSTANCE.swingMode.getValue());
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        if (mc.player != null) {
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, getWorldActionId(mc.world)));
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
    public enum Page {
        General,
        Misc,
        Rotate,
    }
}
