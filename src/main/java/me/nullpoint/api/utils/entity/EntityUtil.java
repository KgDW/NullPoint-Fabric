package me.nullpoint.api.utils.entity;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.RotateManager;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.asm.accessors.IClientWorld;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.impl.render.SwingModifer;
import me.nullpoint.mod.modules.settings.SwingSide;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import static me.nullpoint.api.utils.Wrapper.mc;

public class EntityUtil implements Wrapper {
    public static boolean rotating = false;
    public static boolean isHoldingWeapon(PlayerEntity player) {
        return player.getMainHandStack().getItem() instanceof SwordItem || player.getMainHandStack().getItem() instanceof AxeItem;
    }
    public static boolean isUsing() {
        return mc.player.isUsingItem();
    }
    public static boolean isInsideBlock() {
        if (BlockUtil.getBlock(EntityUtil.getPlayerPos(true)) == Blocks.ENDER_CHEST) return true;
        return mc.world.canCollide(mc.player, mc.player.getBoundingBox());
    }
    public static int getDamagePercent(ItemStack stack) {
        return (int) ((stack.getMaxDamage() - stack.getDamage()) / Math.max(0.1, stack.getMaxDamage()) * 100.0f);
    }
    public static boolean isArmorLow(PlayerEntity player, int durability) {
        for (ItemStack piece : player.getArmorItems()) {

            if (piece == null || piece.isEmpty()) {
                return true;
            }

            if (getDamagePercent(piece) >= durability) continue;
            return true;
        }
        return false;
    }

    public static boolean isInWeb() {
        Box pBox = mc.player.getBoundingBox();
        BlockPos pBlockPos = BlockPos.ofFloored(mc.player.getPos());

        for (int x = pBlockPos.getX() - 2; x <= pBlockPos.getX() + 2; x++) {
            for (int y = pBlockPos.getY() - 1; y <= pBlockPos.getY() + 4; y++) {
                for (int z = pBlockPos.getZ() - 2; z <= pBlockPos.getZ() + 2; z++) {
                    BlockPos bp = new BlockPos(x, y, z);
                    if (pBox.intersects(new Box(bp)) && mc.world.getBlockState(bp).getBlock() == Blocks.COBWEB)
                        return true;
                }
            }
        }

        return false;
    }

    public static float[] getLegitRotations(Vec3d vec) {
        Vec3d eyesPos = getEyesPos();
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[]{mc.player.getYaw() + MathHelper.wrapDegrees(yaw - mc.player.getYaw()), mc.player.getPitch() + MathHelper.wrapDegrees(pitch - mc.player.getPitch())};
    }
    public static float getHealth(Entity entity) {
        if (entity.isLiving()) {
            LivingEntity livingBase = (LivingEntity) entity;
            return livingBase.getHealth() + livingBase.getAbsorptionAmount();
        }
        return 0.0f;
    }
    public static BlockPos getPlayerPos() {
        return new BlockPosX(mc.player.getPos());
    }

    public static BlockPos getEntityPos(Entity entity) {
        return new BlockPosX(entity.getPos());
    }

    public static BlockPos getPlayerPos(boolean fix) {
        return new BlockPosX(mc.player.getPos(), fix);
    }

    public static BlockPos getEntityPos(Entity entity, boolean fix) {
        return new BlockPosX(entity.getPos(), fix);
    }

    public static Vec3d getEyesPos() {
        return mc.player.getEyePos();
    }

    public static boolean canSee(BlockPos pos, Direction side) {
        Vec3d testVec = pos.toCenterPos().add(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5);
        HitResult result = mc.world.raycast(new RaycastContext(getEyesPos(), testVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
        return result == null || result.getType() == HitResult.Type.MISS;
    }

    public static void sendYawAndPitch(float yaw, float pitch) {
        sendLook(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround()));
    }

    public static void faceVector(Vec3d directionVec) {
        RotateManager.ROTATE_TIMER.reset();
        RotateManager.directionVec = directionVec;
        float[] angle = getLegitRotations(directionVec);
        if (angle[0] == Nullpoint.ROTATE.lastYaw && angle[1] == Nullpoint.ROTATE.lastPitch) return;
        sendLook(new PlayerMoveC2SPacket.LookAndOnGround(angle[0], angle[1], mc.player.isOnGround()));
    }

    public static void faceVectorNoStay(Vec3d directionVec) {
        float[] angle = getLegitRotations(directionVec);
        if (angle[0] == Nullpoint.ROTATE.lastYaw && angle[1] == Nullpoint.ROTATE.lastPitch) return;
        sendLook(new PlayerMoveC2SPacket.LookAndOnGround(angle[0], angle[1], mc.player.isOnGround()));
    }
    public static void sendLook(PlayerMoveC2SPacket packet) {
        if (!packet.changesLook() || packet.getYaw(114514) == Nullpoint.ROTATE.lastYaw && packet.getPitch(114514) == Nullpoint.ROTATE.lastPitch) {
            return;
        }
        rotating = true;
        Nullpoint.ROTATE.setRotation(packet.getYaw(0), packet.getPitch(0), true);
        mc.player.networkHandler.sendPacket(packet);
        rotating = false;
    }


    public static void facePosSide(BlockPos pos, Direction side) {
        final Vec3d hitVec = pos.toCenterPos().add(new Vec3d(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5));
        faceVector(hitVec);
    }

    public static void facePosSideNoStay(BlockPos pos, Direction side) {
        final Vec3d hitVec = pos.toCenterPos().add(new Vec3d(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5));
        faceVectorNoStay(hitVec);
    }

    public static int getWorldActionId(ClientWorld world) {
        PendingUpdateManager pum = getUpdateManager(world);
        int p = pum.getSequence();
        pum.close();
        return p;
    }
    public static boolean isElytraFlying() {
        return mc.player.isFallFlying();
    }

    static PendingUpdateManager getUpdateManager(ClientWorld world) {
        return ((IClientWorld) world).acquirePendingUpdateManager();
    }

    public static void swingHand(Hand hand, SwingSide side) {
        boolean isCustom = SwingModifer.instance.isOn();


        if(!isCustom) {
            switch (side) {
                case All -> mc.player.swingHand(hand);
                case Client -> mc.player.swingHand(hand, false);
                case Server -> mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
            }
        }else{
            Hand custom=null;
            if(SwingModifer.instance.mode.getValue()== SwingModifer.Mode.Main){
                custom=Hand.MAIN_HAND;
            }
            else if(SwingModifer.instance.mode.getValue()== SwingModifer.Mode.OFF){
                custom=Hand.OFF_HAND;
            }
            if(custom==null){
                return;
            }
            switch (side) {
                case All -> mc.player.swingHand(custom);
                case Client -> mc.player.swingHand(custom, false);
                case Server -> mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(custom));
            }
        }
    }

    public static void syncInventory() {
        if (CombatSetting.INSTANCE.inventorySync.getValue()) mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
    }


}

