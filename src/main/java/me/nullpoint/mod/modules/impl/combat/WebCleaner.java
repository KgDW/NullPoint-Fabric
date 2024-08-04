package me.nullpoint.mod.modules.impl.combat;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.combat.ExplosionUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.settings.SwingSide;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static me.nullpoint.api.utils.world.BlockUtil.getBlock;
import static me.nullpoint.mod.modules.impl.combat.HoleKick.isTargetHere;

public class WebCleaner extends Module {
    public WebCleaner() {
        super("WebCleaner","anti-renzhemao",Category.Combat);
    }
    EnumSetting<Page> page =
            add(new EnumSetting<>("Page", Page.General));
    BooleanSetting eatingPause =
            add(new BooleanSetting("EatingPause", true, v -> page.getValue() == Page.General));
    BooleanSetting burOnly =
            add(new BooleanSetting("BurOnly", true, v -> page.getValue() == Page.General));
    SliderSetting burMin =add(new SliderSetting("BurMinWeb",6,1,8, v->page.getValue()==Page.General).setSuffix("m"));
    SliderSetting noBurMin =add(new SliderSetting("noBurMinWeb",3,1,8, v->page.getValue()==Page.General).setSuffix("m"));

    SliderSetting placeRange=add(new SliderSetting("PlaceRange",6,1,10,v->page.getValue()==Page.General).setSuffix("m"));
    SliderSetting wallRange = add(new SliderSetting("WallRange", 6.0, 0.0, 6.0,v->page.getValue()==Page.General).setSuffix("m"));
    SliderSetting maxSelfDmg=add(new SliderSetting("MaxSelfDMG",3,0,36,v->page.getValue()==Page.General));
    SliderSetting placeDelay =
            add(new SliderSetting("PlaceDelay", 300, 0, 1000, v -> page.getValue() == Page.General).setSuffix("ms"));
    SliderSetting breakDelay =
            add(new SliderSetting("BreakDelay", 0, 0, 1000, v -> page.getValue() == Page.General).setSuffix("ms"));
    SliderSetting updateDelay =
            add(new SliderSetting("UpdateDelay", 250, 0, 1000, v -> page.getValue() == Page.General).setSuffix("ms"));
    EnumSetting<SwapMode> autoSwap =
            add(new EnumSetting<>("AutoSwap", SwapMode.Off, v -> page.getValue() == Page.General));
    public enum SwapMode {
        Off, Normal, Silent, Inventory
    }
    EnumSetting<SwingSide> swingMode = add(new EnumSetting<>("Swing", SwingSide.Server, v -> page.getValue() == Page.General));
    BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true, v -> page.getValue() == Page.Rotate).setParent());
    BooleanSetting onBreak =
            add(new BooleanSetting("OnBreak", false, v -> rotate.isOpen() && page.getValue() == Page.Rotate));
    BooleanSetting yawStep =
            add(new BooleanSetting("YawStep", false, v -> rotate.isOpen() && page.getValue() == Page.Rotate));
    SliderSetting steps =
            add(new SliderSetting("Steps", 0.3f, 0.1f, 1.0f, 0.01f, v -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
    BooleanSetting random =
            add(new BooleanSetting("Random", true, v -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
    BooleanSetting packet =
            add(new BooleanSetting("Packet", false, v -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
    BooleanSetting checkLook =
            add(new BooleanSetting("CheckLook", true, v -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
    SliderSetting fov =
            add(new SliderSetting("Fov", 30f, 0f, 90f, v -> rotate.isOpen() && yawStep.getValue() && checkLook.getValue() && page.getValue() == Page.Rotate));
    public static final Timer placeTimer = new Timer();
    public final Timer lastBreakTimer = new Timer();
    private float lastYaw = 0f;
    private float lastPitch = 0f;
    public Vec3d directionVec = null;

    @EventHandler()
    public void onRotate(RotateEvent event) {
        if (rotate.getValue() && yawStep.getValue() && directionVec != null) {
            float[] newAngle = injectStep(EntityUtil.getLegitRotations(directionVec), steps.getValueFloat());
            lastYaw = newAngle[0];
            lastPitch = newAngle[1];
            if (random.getValue() && new Random().nextBoolean()) {
                lastPitch = Math.min(new Random().nextFloat() * 2 + lastPitch, 90);
            }
            event.setYaw(lastYaw);
            event.setPitch(lastPitch);
        } else {
            lastYaw = Nullpoint.ROTATE.lastYaw;
            lastPitch = Nullpoint.ROTATE.lastPitch;
        }
    }
    public List<BlockPos> getWebPos(PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        List<BlockPos> webPos=new ArrayList<>();
        for (float x : new float[]{0, 0.3F, -0.3f}) {
            for (float z : new float[]{0, 0.3F, -0.3f}) {
                for (int y : new int[]{0, 1, 2}) {
                    BlockPos pos = new BlockPosX(playerPos.getX() + x, playerPos.getY(), playerPos.getZ() + z).up(y);
                    if (isTargetHere(pos, player) && mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                        webPos.add(pos);
                    }
                }
            }
        }
        return webPos;
    }

    private final Timer delayTimer = new Timer();

    @Override
    public void onUpdate(){
        if (nullCheck()){
            return;
        }
        if(burOnly.getValue() && !EntityUtil.isInsideBlock()){
            return;
        }
        if(eatingPause.getValue() && mc.player.isUsingItem()){
            return;
        }
        if (!delayTimer.passedMs((long) updateDelay.getValue())) return;

        if(!HoleKick.isInWeb(mc.player)){
            return;
        }
        delayTimer.reset();
        List<BlockPos> webPos=getWebPos(mc.player);
        if((EntityUtil.isInsideBlock() && webPos.size()>= burMin.getValue()) || (!EntityUtil.isInsideBlock() && webPos.size()>=noBurMin.getValue())) {


            for (BlockPos pos : BlockUtil.getSphere(placeRange.getValueFloat())) {
                if (!HoleKick.isInWeb(mc.player)) {
                    webPos.clear();
                    return;
                }
                if(pos.getY()-mc.player.getY()>2){
                    //>2 can break?
                    continue;
                }
                if (!canTouch(pos.down())) continue;
                if (!canPlaceCrystal(pos, true, false))
                    continue;
                if (!willBlockBeDestroyedByExplosion(mc.world, webPos.get(0), pos, 6.0f)) {
                    //todo calc faster?
                    continue;
                }
                float selfDmg = ExplosionUtil.calculateDamage(pos.getX(), pos.getY(), pos.getZ(), mc.player, mc.player, 6);
                if (selfDmg > maxSelfDmg.getValue()) {
                    continue;
                }
                doPlace(pos);
                doBreak(pos);
            }
        }
        webPos.clear();

    }

    private void doPlace(BlockPos pos) {
        if (!mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) && !mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) && !findCrystal()) {
            return;
        }
        if (!canTouch(pos.down())) {
            return;
        }
        BlockPos obsPos = pos.down();
        Direction facing = BlockUtil.getClickSide(obsPos);
        Vec3d vec = obsPos.toCenterPos().add(facing.getVector().getX() * 0.5,facing.getVector().getY() * 0.5,facing.getVector().getZ() * 0.5);
        if (rotate.getValue()) {
            if (!faceVector(vec)) return;
        }
        if (!placeTimer.passedMs((long) placeDelay.getValue())) return;
        placeTimer.reset();
        if (mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) || mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL)) {
            placeCrystal(pos);
        } else if (findCrystal()) {
            int old = mc.player.getInventory().selectedSlot;
            int crystal = getCrystal();
            if (crystal == -1) return;
            doSwap(crystal);
            placeCrystal(pos);
            if (autoSwap.getValue() == SwapMode.Silent) {
                doSwap(old);
            } else if (autoSwap.getValue() == SwapMode.Inventory) {
                doSwap(crystal);
                EntityUtil.syncInventory();
            }
        }
    }
    private boolean findCrystal() {
        if (autoSwap.getValue() == SwapMode.Off) return false;
        return getCrystal() != -1;
    }
    private int getCrystal() {
        if (autoSwap.getValue() == SwapMode.Silent || autoSwap.getValue() == SwapMode.Normal) {
            return InventoryUtil.findItem(Items.END_CRYSTAL);
        } else if (autoSwap.getValue() == SwapMode.Inventory) {
            return InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL);
        }
        return -1;
    }
    private void doSwap(int slot) {
        if (autoSwap.getValue() == SwapMode.Silent || autoSwap.getValue() == SwapMode.Normal) {
            InventoryUtil.switchToSlot(slot);
        } else if (autoSwap.getValue() == SwapMode.Inventory) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        }
    }
    public void placeCrystal(BlockPos pos) {
        //PlaceRender.PlaceMap.put(pos, new PlaceRender.placePosition(pos));
        boolean offhand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
        BlockPos obsPos = pos.down();
        Direction facing = BlockUtil.getClickSide(obsPos);
        BlockUtil.clickBlock(obsPos, facing, false, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, SwingSide.All);
    }
    private void doBreak(BlockPos pos) {
        lastBreakTimer.reset();
        if (!CombatUtil.breakTimer.passedMs((long) breakDelay.getValue())) return;
        for (EndCrystalEntity entity : mc.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() +1, pos.getY() + 2, pos.getZ() + 1))) {
          
            if (rotate.getValue() && onBreak.getValue()) {
                if (!faceVector(entity.getPos().add(0, 0.25, 0))) return;
            }
            CombatUtil.breakTimer.reset();
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
            EntityUtil.swingHand(Hand.MAIN_HAND, swingMode.getValue());
            break;
        }
    }
    public void tryMergeStack(List<Pair<ItemStack, BlockPos>> stacks, ItemStack stack, BlockPos pos) {
        for(int i = 0; i < stacks.size(); ++i) {
            Pair<ItemStack, BlockPos> pair = (Pair)stacks.get(i);
            ItemStack itemStack = (ItemStack)pair.getFirst();
            if (ItemEntity.canMerge(itemStack, stack)) {
                stacks.set(i, Pair.of(ItemEntity.merge(itemStack, stack, 16), (BlockPos)pair.getSecond()));
                if (stack.isEmpty()) {
                    return;
                }
            }
        }

        stacks.add(Pair.of(stack, pos));
    }
    public boolean behindWall(BlockPos pos) {
        Vec3d testVec;
        if (CombatSetting.INSTANCE.lowVersion.getValue()) {
            testVec = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        } else {
            testVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 2 * 0.85, pos.getZ() + 0.5);
        }
        HitResult result = mc.world.raycast(new RaycastContext(EntityUtil.getEyesPos(), testVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
        if (result == null || result.getType() == HitResult.Type.MISS) return false;
        return mc.player.getEyePos().distanceTo(pos.toCenterPos().add(0, -0.5, 0)) > wallRange.getValue();
    }
    private boolean canTouch(BlockPos pos) {
        Direction side = BlockUtil.getClickSideStrict(pos);
        return side != null && pos.toCenterPos().add(new Vec3d(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5)).distanceTo(mc.player.getEyePos()) <= placeRange.getValue();
    }
    public boolean canPlaceCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        BlockPos obsPos = pos.down();
        BlockPos boost = obsPos.up();
        return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN)
                && BlockUtil.getClickSideStrict(obsPos) != null
                && !hasEntityBlockCrystal(boost, ignoreCrystal, ignoreItem)
                && !hasEntityBlockCrystal(boost.up(), ignoreCrystal, ignoreItem)
                && (getBlock(boost) == Blocks.AIR || hasEntityBlockCrystal(boost, false, ignoreItem) && getBlock(boost) == Blocks.FIRE)
                && (!CombatSetting.INSTANCE.lowVersion.getValue() || getBlock(boost.up()) == Blocks.AIR);
    }
    public boolean hasEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
            if (!entity.isAlive() || ignoreItem && entity instanceof ItemEntity || entity instanceof ArmorStandEntity && CombatSetting.INSTANCE.obsMode.getValue())
                continue;
            if (entity instanceof EndCrystalEntity) {
                if (!ignoreCrystal) return true;
                if (mc.player.canSee(entity) || mc.player.getEyePos().distanceTo(entity.getPos()) <= wallRange.getValue()) {
                    continue;
                }
            }
            return true;
        }
        return false;
    }

    public boolean willBlockBeDestroyedByExplosion(World world, BlockPos explosionPos, BlockPos pos2, float power) {
        Explosion explosion = new Explosion(world, null, explosionPos.getX(), explosionPos.getY(), explosionPos.getZ(), power, false, Explosion.DestructionType.DESTROY);

        BlockState blockState = world.getBlockState(pos2);
        float doubleExplosionSize = 2 * explosion.getPower();

        double distancedsize = MathHelper.sqrt((float) explosionPos.getSquaredDistance(pos2)) / (double) doubleExplosionSize;

        float exposure = getExposure(explosionPos.toCenterPos(), pos2);
        float blastResistance = blockState.getBlock().getBlastResistance();

        float damage = (float) ((1.0F - distancedsize) * exposure);
        if(damage>blastResistance){
            CommandManager.sendChatMessage(String.valueOf(damage));
        }
        return damage > blastResistance*3.5;//magic number 3.5 fix the bug LOL
    }


    public float getExposure(Vec3d source, BlockPos pos2) {
        Box box = new Box(pos2);
        double d = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double e = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double f = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
        double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
        double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
        if (!(d < 0.0) && !(e < 0.0) && !(f < 0.0)) {
            int i = 0;
            int j = 0;

            for(double k = 0.0; k <= 1.0; k += d) {
                for(double l = 0.0; l <= 1.0; l += e) {
                    for(double m = 0.0; m <= 1.0; m += f) {
                        double n = MathHelper.lerp(k, box.minX, box.maxX);
                        double o = MathHelper.lerp(l, box.minY, box.maxY);
                        double p = MathHelper.lerp(m, box.minZ, box.maxZ);
                        Vec3d vec3d = new Vec3d(n + g, o, p + h);
                        if (mc.world.raycast(new RaycastContext(vec3d, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,new PlayerAndPredict(pos2).predict)).getType() == HitResult.Type.MISS) {
                            ++i;
                        }

                        ++j;
                    }
                }
            }

            return (float)i / (float)j;
        } else {
            return 0.0F;
        }
    }
    @Override
    public void onEnable() {
        lastYaw = Nullpoint.ROTATE.lastYaw;
        lastPitch = Nullpoint.ROTATE.lastPitch;
        lastBreakTimer.reset();
    }
    public class PlayerAndPredict {
        final PlayerEntity predict;
        public PlayerAndPredict(BlockPos pos) {
            predict = new PlayerEntity(mc.world, pos.down(), 0, new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {
                @Override
                public boolean isSpectator() {
                    return false;
                }

                @Override
                public boolean isCreative() {
                    return false;
                }
            };
            predict.setPosition(pos.toCenterPos().add(0,-1,0));
            predict.setHealth(20);
            predict.setOnGround(true);


        }
    }
    public boolean faceVector(Vec3d directionVec) {
        if (!yawStep.getValue()) {
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



    public enum Page{
        General,
        Rotate
    }
}
