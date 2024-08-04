package me.nullpoint.api.utils.combat;

import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.world.BlockUtil;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;

public class ExplosionUtil implements Wrapper {
    public static float anchorDamage(BlockPos pos, PlayerEntity target, PlayerEntity predict){
        if (BlockUtil.getBlock(pos) == Blocks.RESPAWN_ANCHOR) {
            CombatUtil.modifyPos = pos;
            CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
            float damage = calculateDamage(pos.toCenterPos().getX(), pos.toCenterPos().getY(), pos.toCenterPos().getZ(), target, predict, 5);
            CombatUtil.modifyPos = null;
            return damage;
        } else {
            return calculateDamage(pos.toCenterPos().getX(), pos.toCenterPos().getY(), pos.toCenterPos().getZ(), target, predict, 5);
        }
    }
    public static float calculateDamage(double posX, double posY, double posZ, Entity entity, Entity predict, float power) {
        if (predict == null) predict = entity;
        float doubleExplosionSize = 2 * power;
        double distancedsize = MathHelper.sqrt((float) predict.squaredDistanceTo(posX, posY, posZ)) / (double) doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = 0.0;
        try {
            blockDensity = Explosion.getExposure(vec3d, predict);
        } catch (Exception exception) {

        }
        double v = (1.0 - distancedsize) * blockDensity;
        float damage = (int) ((v * v + v) / 2.0 * 7.0 * (double) doubleExplosionSize + 1.0);
        double finald = 1.0;
        if (entity instanceof LivingEntity) {
            finald = getBlastReduction((LivingEntity) entity, getDamageMultiplied(damage));
        }
        return (float) finald;
    }
    public static float getDamageAfterAbsorb(float damage, float totalArmor, float toughnessAttribute)
    {
        float f = 2.0F + toughnessAttribute / 4.0F;
        float f1 = MathHelper.clamp(totalArmor - damage / f, totalArmor * 0.2F, 20.0F);
        return damage * (1.0F - f1 / 25.0F);
    }

    public static float getBlastReduction(LivingEntity entity, float damageI) {
        float damage = damageI;
        if (entity instanceof PlayerEntity player) {
//            System.out.println("preDamage" + damage);
            damage = getDamageAfterAbsorb(damage, (float) player.getArmor(), (float) player.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
//            System.out.println("absorb" + damage);
            int k = getProtectionAmount(player.getArmorItems());
            float f = MathHelper.clamp((float) k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            System.out.println("project" + f);
            //System.out.println("project damage" + damage);
            if (player.hasStatusEffect(StatusEffects.RESISTANCE)) {
                damage -= damage / 4.0f;
            }
            damage = Math.max(damage, 0.0f);
/*            damage *= 0.12640740740;
            if (player.hasStatusEffect(StatusEffects.RESISTANCE)) {
                damage -= damage / 4.0f;
            }*/
            return damage;
        }
        damage = getDamageAfterAbsorb(damage, (float) entity.getArmor(), (float) entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
        return damage;
    }

    public static int getProtectionAmount(Iterable<ItemStack> armorItems) {
        int value = 0;
        for (ItemStack itemStack : armorItems) {
            int level = EnchantmentHelper.getLevel(Enchantments.PROTECTION, itemStack);
            if (level == 0) {
                value += EnchantmentHelper.getLevel(Enchantments.BLAST_PROTECTION, itemStack) * 2;
            } else {
                value += level;
            }
        }
        return value;
    }

    public static float getDamageMultiplied(float damage) {
        int diff = mc.world.getDifficulty().getId();
        return damage * (diff == 0 ? 0.0f : (diff == 2 ? 1.0f : (diff == 1 ? 0.5f : 1.5f)));
    }
}
