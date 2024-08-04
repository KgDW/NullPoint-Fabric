package me.nullpoint.mod.modules.impl.combat;

import com.google.common.collect.Lists;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.path.PathUtils;
import me.nullpoint.api.utils.path.Vec3;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.asm.accessors.IEntity;
import me.nullpoint.asm.accessors.ILivingEntity;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.SwingSide;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TPAura extends Module {

    public static TPAura INSTANCE;
    public static LivingEntity target;
    public final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));

    public final SliderSetting range =
            add(new SliderSetting("Range", 60.0f, 0.1f, 250.0f, v -> page.getValue() == Page.General));
    private final SliderSetting cooldown =
            add(new SliderSetting("Cooldown", 1.1f, 0f, 1.2f, 0.01, v -> page.getValue() == Page.General));
    private final EnumSetting<Aura.Cooldown> cd = add(new EnumSetting<>("CooldownMode", Aura.Cooldown.Delay));
    private final BooleanSetting whileEating =
            add(new BooleanSetting("WhileUsing", true, v -> page.getValue() == Page.General));

    private final BooleanSetting cc =
            add(new BooleanSetting("TPOff", true, v -> page.getValue() == Page.General));
    private final BooleanSetting test =
            add(new BooleanSetting("TPOffTest", true, v -> page.getValue() == Page.General));
    private final BooleanSetting weaponOnly =
            add(new BooleanSetting("WeaponOnly", true, v -> page.getValue() == Page.General));
    private final EnumSetting<SwingSide> swingMode = add(new EnumSetting<>("Swing", SwingSide.Server, v -> page.getValue() == Page.General));

    private final EnumSetting<TargetMode> targetMode =
            add(new EnumSetting<>("Filter", TargetMode.DISTANCE, v -> page.getValue() == Page.Target));
    public final BooleanSetting Players = add(new BooleanSetting("Players", true, v -> page.getValue() == Page.Target));
    public final BooleanSetting Mobs = add(new BooleanSetting("Mobs", true, v -> page.getValue() == Page.Target));
    public final BooleanSetting Animals = add(new BooleanSetting("Animals", true, v -> page.getValue() == Page.Target));
    public final BooleanSetting Villagers = add(new BooleanSetting("Villagers", true, v -> page.getValue() == Page.Target));
    public final BooleanSetting Slimes = add(new BooleanSetting("Slimes", true, v -> page.getValue() == Page.Target));
    public TPAura() {
        super("TPAura", "Attacks players in radius", Category.Combat);
        INSTANCE = this;
    }
    int attackTicks;
    private final Timer tick = new Timer();
    private ArrayList<Vec3> lastPath;
    public static boolean attacking = false;
    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        if (tick.passed(50)) {
            attackTicks++;
            tick.reset();
        }
        if (lastPath != null) {
            for (Vec3 vec3 : lastPath) {
                Render3DUtil.draw3DBox(matrixStack, ((IEntity) mc.player).getDimensions().getBoxAt(vec3.mc()), new Color(255, 255 ,255 ,150), true, true);
            }
        }
    }

    @Override
    public void onUpdate() {
        if (tick.passed(50)) {
            attackTicks++;
            tick.reset();
        }
        if (weaponOnly.getValue() && !EntityUtil.isHoldingWeapon(mc.player)) {
            target = null;
            return;
        }
        target = getTarget();
        if (target == null) {
            return;
        }
        if (auraReady()) {
            doTPHit(target);
        } else {
            target = null;
        }
    }
    
    private boolean auraReady() {
        int at = attackTicks;
        if (cd.getValue() == Aura.Cooldown.Vanilla) {
            at = ((ILivingEntity) mc.player).getLastAttackedTicks();
        }
        if (!(Math.max(at / getAttackCooldownProgressPerTick(), 0.0F) >= cooldown.getValue()))
            return false;
        return whileEating.getValue() || !mc.player.isUsingItem();
    }

    public static float getAttackCooldownProgressPerTick() {
        return (float) (1.0 / mc.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * 20.0);
    }
    
    private LivingEntity getTarget() {
        LivingEntity target = null;
        double distance = range.getValue();
        double maxHealth = 36.0;
        for (Entity e : mc.world.getEntities()) {
            if (e instanceof LivingEntity entity) {
                if (!isEnemy(entity)) continue;
                if (!CombatUtil.isValid(entity, range.getValue())) continue;

                if (target == null) {
                    target = entity;
                    distance = mc.player.distanceTo(entity);
                    maxHealth = EntityUtil.getHealth(entity);
                } else {
                    if (entity instanceof PlayerEntity && EntityUtil.isArmorLow((PlayerEntity) entity, 10)) {
                        target = entity;
                        break;
                    }
                    if (targetMode.getValue() == TargetMode.HEALTH && EntityUtil.getHealth(entity) < maxHealth) {
                        target = entity;
                        maxHealth = EntityUtil.getHealth(entity);
                        continue;
                    }
                    if (targetMode.getValue() == TargetMode.DISTANCE && mc.player.distanceTo(entity) < distance) {
                        target = entity;
                        distance = mc.player.distanceTo(entity);
                    }
                }
            }
        }
        return target;
    }
    private void doTPHit(LivingEntity entity) {
        attacking = true;
        List<Vec3> tpPath = PathUtils.computePath(mc.player, entity);
        lastPath = new ArrayList<>(tpPath);
        tpPath.forEach((vec3) -> mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(vec3.getX(), vec3.getY(), vec3.getZ(), false)));
        mc.interactionManager.attackEntity(mc.player, target);
        EntityUtil.swingHand(Hand.MAIN_HAND, swingMode.getValue());
        tpPath = Lists.reverse(tpPath);
        tpPath.forEach((vec3) -> mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(vec3.getX(), vec3.getY(), vec3.getZ(), false)));
        if (test.getValue()) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(0, -0.354844, 0, false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(0, +0.325488, 0, false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(0, -0.15441, 0, false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(0, -0.15444, 0, false));
        }
        attacking = false;
        attackTicks = 0;
    }



    private boolean isEnemy(Entity entity) {
        if (entity instanceof SlimeEntity && Slimes.getValue()) return true;
        if (entity instanceof PlayerEntity && Players.getValue()) return true;
        if (entity instanceof VillagerEntity && Villagers.getValue()) return true;
        if (!(entity instanceof VillagerEntity) && entity instanceof MobEntity && Mobs.getValue()) return true;
        if (entity instanceof AnimalEntity && Animals.getValue()) return true;
        return false;
    }
    private enum TargetMode {
        DISTANCE,
        HEALTH,
    }

    public enum Page {
        General,
        Target
    }
}