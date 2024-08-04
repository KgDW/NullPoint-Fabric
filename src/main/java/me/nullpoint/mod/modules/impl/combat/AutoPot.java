package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.asm.accessors.IEntity;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AutoPot extends Module {
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true).setParent());
    private final SliderSetting pitch =
            add(new SliderSetting("Pitch", 86, 80, 90, v -> rotate.isOpen()));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true));
    public final SliderSetting delay =
            add(new SliderSetting("Delay", 1050, 0, 2000));
    private final BooleanSetting hcehck =
            add(new BooleanSetting("HealthCheck", false).setParent());
    public final SliderSetting health =
            add(new SliderSetting("Health", 20, 0,  36, v -> hcehck.isOpen()).setSuffix("HP"));
    public final SliderSetting effectRange =
            add(new SliderSetting("EffectRange", 3.0, 0.0, 6.0, 0.1));
    private final SliderSetting predictTicks =
            add(new SliderSetting("Predict", 2, 0, 10).setSuffix("ticks"));
    public final BooleanSetting debug =
            add(new BooleanSetting("debug", false));

    private final Timer timer = new Timer();

    public AutoPot(){
        super("AutoPot", Category.Combat);
    }

    @Override
    public String getInfo() {
        return String.valueOf(InventoryUtil.getPotCount(StatusEffects.RESISTANCE));
    }

    @Override
    public void onUpdate(){
        if (!timer.passedMs(delay.getValueInt())) {
            return;
        }
        if(hcehck.getValue() && mc.player.getHealth() + mc.player.getAbsorptionAmount() >= health.getValue()){
            return;
        }
        if(mc.player.getPos().add(CombatUtil.getMotionVec(mc.player, predictTicks.getValueInt(), true)).squaredDistanceTo(calcTrajectory(Items.SPLASH_POTION, Nullpoint.ROTATE.rotateYaw, pitch.getValueFloat())) > effectRange.getValue()){
            return;
        }
        List<StatusEffectInstance> effects = new ArrayList<>((mc).player.getStatusEffects());
        for (StatusEffectInstance potionEffect : effects){
            if(potionEffect.getEffectType() == StatusEffects.RESISTANCE && (potionEffect.getAmplifier() + 1 ) > 1){
                return;
            }
        }
        doPot();
    }

    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        if(debug.getValue()){
            Vec3d pos = calcTrajectory(Items.SPLASH_POTION, Nullpoint.ROTATE.rotateYaw, pitch.getValueFloat());
            Render3DUtil.draw3DBox(matrixStack, new Box(new BlockPosX(pos.x, pos.y, pos.z)), new Color(255, 255, 255, 80));
            Render3DUtil.draw3DBox(matrixStack, ((IEntity) mc.player).getDimensions().getBoxAt(mc.player.getPos().add(CombatUtil.getMotionVec(mc.player, predictTicks.getValueInt(), true))).expand(0, 0.1, 0), new Color(0, 255, 255, 80), false, true);
        }
    }

    private void doPot(){
        int oldSlot = mc.player.getInventory().selectedSlot;
        int slot = findPot(StatusEffects.RESISTANCE);
        if(slot == -1){
            CommandManager.sendChatMessage("§c[!] No Potion found");
            disable();
            return;
        }
        timer.reset();
        doSwap(slot);
        if (this.rotate.getValue()) {
            /*if(Nullpoint.SPEED.getSpeedKpH() < 8){
                EntityUtil.sendYawAndPitch(Nullpoint.ROTATE.rotateYaw, -90);
            } else {
                EntityUtil.sendYawAndPitch(Nullpoint.ROTATE.rotateYaw, 88);
            }*/
            EntityUtil.sendYawAndPitch(Nullpoint.ROTATE.rotateYaw, pitch.getValueFloat());
        }
        mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, EntityUtil.getWorldActionId(mc.world)));
        if (inventory.getValue()) {
            doSwap(slot);
            EntityUtil.syncInventory();
        } else {
            doSwap(oldSlot);
        }
    }

    public int findPot(StatusEffect statusEffect) {
        if (inventory.getValue()) {
            return InventoryUtil.findPotInventorySlot(statusEffect);
        } else {
            return InventoryUtil.findPot(statusEffect);
        }
    }

    private void doSwap(int slot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private Vec3d calcTrajectory(Item item, float yaw, float pitch) {
        double x = MathUtil.interpolate(mc.player.prevX, mc.player.getX(), mc.getTickDelta());
        double y = MathUtil.interpolate(mc.player.prevY, mc.player.getY(), mc.getTickDelta());
        double z = MathUtil.interpolate(mc.player.prevZ, mc.player.getZ(), mc.getTickDelta());

        y = y + mc.player.getEyeHeight(mc.player.getPose()) - 0.1000000014901161;

        x = x - MathHelper.cos(yaw / 180.0f * 3.1415927f) * 0.16f;
        z = z - MathHelper.sin(yaw / 180.0f * 3.1415927f) * 0.16f;

        final float maxDist = getDistance(item);
        double motionX = -MathHelper.sin(yaw / 180.0f * 3.1415927f) * MathHelper.cos(pitch / 180.0f * 3.1415927f) * maxDist;
        double motionY = -MathHelper.sin((pitch - getThrowPitch(item)) / 180.0f * 3.141593f) * maxDist;
        double motionZ = MathHelper.cos(yaw / 180.0f * 3.1415927f) * MathHelper.cos(pitch / 180.0f * 3.1415927f) * maxDist;
        float power = mc.player.getItemUseTime() / 20.0f;
        power = (power * power + power * 2.0f) / 3.0f;
        if (power > 1.0f) {
            power = 1.0f;
        }
        final float distance = MathHelper.sqrt((float) (motionX * motionX + motionY * motionY + motionZ * motionZ));
        motionX /= distance;
        motionY /= distance;
        motionZ /= distance;

        final float pow = (item instanceof BowItem ? (power * 2.0f) : item instanceof CrossbowItem ? (2.2f) : 1.0f) * getThrowVelocity(item);

        motionX *= pow;
        motionY *= pow;
        motionZ *= pow;
        if (!mc.player.isOnGround())
            motionY += mc.player.getVelocity().getY();


        Vec3d lastPos;
        for (int i = 0; i < 300; i++) {
            lastPos = new Vec3d(x, y, z);
            x += motionX;
            y += motionY;
            z += motionZ;
            if (mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() == Blocks.WATER) {
                motionX *= 0.8;
                motionY *= 0.8;
                motionZ *= 0.8;
            } else {
                motionX *= 0.99;
                motionY *= 0.99;
                motionZ *= 0.99;
            }

            if (item instanceof BowItem) motionY -= 0.05000000074505806;
            else if (mc.player.getMainHandStack().getItem() instanceof CrossbowItem) motionY -= 0.05000000074505806;
            else motionY -= 0.03f;


            Vec3d pos = new Vec3d(x, y, z);


            BlockHitResult bhr = mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
            if (bhr != null && bhr.getType() == HitResult.Type.BLOCK) {
                return bhr.getPos();
            }
        }
        return null;
    }

    private float getDistance(Item item) {
        return item instanceof BowItem ? 1.0f : 0.4f;
    }

    private float getThrowVelocity(Item item) {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) return 0.5f;
        if (item instanceof ExperienceBottleItem) return 0.59f;
        if (item instanceof TridentItem) return 2f;
        return 1.5f;
    }

    private int getThrowPitch(Item item) {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem || item instanceof ExperienceBottleItem)
            return 20;
        return 0;
    }
}
