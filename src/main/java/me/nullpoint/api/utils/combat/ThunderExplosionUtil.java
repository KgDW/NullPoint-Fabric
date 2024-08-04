package me.nullpoint.api.utils.combat;

import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.asm.accessors.IExplosion;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.explosion.Explosion;

import java.util.Objects;

public class ThunderExplosionUtil implements Wrapper {
    public static final Explosion explosion = new Explosion(mc.world, null, 0, 0, 0, 6f, false, Explosion.DestructionType.DESTROY);
    public static float anchorDamage(BlockPos pos, PlayerEntity target, PlayerEntity predict){
        if (BlockUtil.getBlock(pos) == Blocks.RESPAWN_ANCHOR) {
            CombatUtil.modifyPos = pos;
            CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
            float damage = calculateDamage(pos.toCenterPos(), target, predict, 5);
            CombatUtil.modifyPos = null;
            return damage;
        } else {
            return calculateDamage(pos.toCenterPos(), target, predict, 5);
        }
    }
    public static float calculateDamage(BlockPos pos, PlayerEntity target, PlayerEntity predict, float power){
        return calculateDamage(pos.toCenterPos().add(0,-0.5,0), target, predict, power);
    }
    public static float calculateDamage(Vec3d explosionPos, PlayerEntity target, PlayerEntity predict, float power) {
        if (mc.world.getDifficulty() == Difficulty.PEACEFUL)
            return 0f;
        if (target.getAbilities().creativeMode) return 0;
        if (predict == null) predict = target;
        ((IExplosion) explosion).setWorld(mc.world);
        ((IExplosion) explosion).setX(explosionPos.x);
        ((IExplosion) explosion).setY(explosionPos.y);
        ((IExplosion) explosion).setZ(explosionPos.z);
        ((IExplosion) explosion).setPower(power);

        if (!new Box(
                MathHelper.floor(explosionPos.x - 11d),
                MathHelper.floor(explosionPos.y - 11d),
                MathHelper.floor(explosionPos.z - 11d),
                MathHelper.floor(explosionPos.x + 13d),
                MathHelper.floor(explosionPos.y + 13d),
                MathHelper.floor(explosionPos.z + 13d)).intersects(predict.getBoundingBox())
        ) {
            return 0f;
        }

        if (!target.isImmuneToExplosion(explosion) && !target.isInvulnerable()) {
            double distExposure = MathHelper.sqrt((float) predict.squaredDistanceTo(explosionPos)) / 12d;
            if (distExposure <= 1.0) {
                double xDiff = predict.getX() - explosionPos.x;
                double yDiff = predict.getY() - explosionPos.y;
                double zDiff = predict.getX() - explosionPos.z;
                double diff = MathHelper.sqrt((float) (xDiff * xDiff + yDiff * yDiff + zDiff * zDiff));
                if (diff != 0.0) {
                    double exposure = Explosion.getExposure(explosionPos, predict);
                    double finalExposure = (1.0 - distExposure) * exposure;

                    float toDamage = (float) Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * 12d + 1.0);

                    if (mc.world.getDifficulty() == Difficulty.EASY) {
                        toDamage = Math.min(toDamage / 2f + 1f, toDamage);
                    } else if (mc.world.getDifficulty() == Difficulty.HARD) {
                        toDamage = toDamage * 3f / 2f;
                    }

                    toDamage = net.minecraft.entity.DamageUtil.getDamageLeft(toDamage, target.getArmor(), (float) Objects.requireNonNull(target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)).getValue());

                    if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                        int resistance = 25 - (Objects.requireNonNull(target.getStatusEffect(StatusEffects.RESISTANCE)).getAmplifier() + 1) * 5;
                        float resistance_1 = toDamage * resistance;
                        toDamage = Math.max(resistance_1 / 25f, 0f);
                    }

                    if (toDamage <= 0f) {
                        toDamage = 0f;
                    } else {
                        int protAmount = EnchantmentHelper.getProtectionAmount(target.getArmorItems(), mc.world.getDamageSources().explosion(explosion));
                        if (protAmount > 0) {
                            toDamage = net.minecraft.entity.DamageUtil.getInflictedDamage(toDamage, protAmount);
                        }
                    }
                    return toDamage;
                }
            }
        }
        return 0f;
    }
}
