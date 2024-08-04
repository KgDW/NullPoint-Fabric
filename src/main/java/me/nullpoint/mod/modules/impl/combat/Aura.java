package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.eventbus.EventPriority;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.JelloUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.asm.accessors.IEntity;
import me.nullpoint.asm.accessors.ILivingEntity;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.SwingSide;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class Aura extends Module {

    public static Aura INSTANCE;
    public static Entity target;
    public final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));

    public final SliderSetting range =
            add(new SliderSetting("Range", 6.0f, 0.1f, 7.0f, v -> page.getValue() == Page.General));
    private final BooleanSetting ghost =
            add(new BooleanSetting("SweepingBypass", false, v -> page.getValue() == Page.General));
    private final EnumSetting<Cooldown> cd = add(new EnumSetting<>("CooldownMode", Cooldown.Delay));
    private final SliderSetting cooldown =
            add(new SliderSetting("Cooldown", 1.1f, 0f, 1.2f, 0.01, v -> page.getValue() == Page.General));
     private final SliderSetting wallRange =
            add(new SliderSetting("WallRange", 6.0f, 0.1f, 7.0f, v -> page.getValue() == Page.General));
    private final BooleanSetting whileEating =
            add(new BooleanSetting("WhileUsing", true, v -> page.getValue() == Page.General));
    private final BooleanSetting weaponOnly =
            add(new BooleanSetting("WeaponOnly", true, v -> page.getValue() == Page.General));
    private final EnumSetting<SwingSide> swingMode = add(new EnumSetting<>("Swing", SwingSide.Server, v -> page.getValue() == Page.General));
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true, v -> page.getValue() == Page.Rotate).setParent());
    private final BooleanSetting newRotate =
            add(new BooleanSetting("NewRotate", true, v -> rotate.isOpen() && page.getValue() == Page.Rotate));
    private final SliderSetting yawStep =
            add(new SliderSetting("YawStep", 0.3f, 0.1f, 1.0f, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
    private final BooleanSetting checkLook =
            add(new BooleanSetting("CheckLook", true, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
    private final SliderSetting fov =
            add(new SliderSetting("Fov", 5f, 0f, 30f, v -> rotate.isOpen() && newRotate.getValue() && checkLook.getValue() && page.getValue() == Page.Rotate));

    private final EnumSetting<TargetMode> targetMode =
            add(new EnumSetting<>("Filter", TargetMode.DISTANCE, v -> page.getValue() == Page.Target));
    public final BooleanSetting Players = add(new BooleanSetting("Players", true, v -> page.getValue() == Page.Target));
    public final BooleanSetting Mobs = add(new BooleanSetting("Mobs", true, v -> page.getValue() == Page.Target));
    public final BooleanSetting Animals = add(new BooleanSetting("Animals", true, v -> page.getValue() == Page.Target));
    public final BooleanSetting Villagers = add(new BooleanSetting("Villagers", true, v -> page.getValue() == Page.Target));
    public final BooleanSetting Slimes = add(new BooleanSetting("Slimes", true, v -> page.getValue() == Page.Target));

    private final EnumSetting<TargetESP> mode = add(new EnumSetting<>("TargetESP", TargetESP.Jello, v -> page.getValue() == Page.Render));
    private final ColorSetting color = add(new ColorSetting("TargetColor", new Color(255, 255, 255, 250), v -> page.getValue() == Page.Render));
    public enum TargetESP {
        Box,
        Jello,
        None
    }
    public enum Cooldown {
        Vanilla,
        Delay
    }
    public Vec3d directionVec = null;
    private final Timer ghostTimer = new Timer();
    private final Timer tick = new Timer();
    private float lastYaw = 0;
    private float lastPitch = 0;
    int attackTicks;
    public Aura() {
        super("Aura", "Attacks players in radius", Category.Combat);
        INSTANCE = this;
    }

    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        if (tick.passed(50)) {
            attackTicks++;
            tick.reset();
        }
        if (target != null) doRender(matrixStack, partialTicks, target, color.getValue(), mode.getValue());
    }

    public static void doRender(MatrixStack matrixStack, float partialTicks, Entity entity, Color color, TargetESP mode) {
        switch (mode) {
            case Box -> Render3DUtil.draw3DBox(matrixStack, ((IEntity) entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), partialTicks), MathUtil.interpolate(entity.lastRenderY, entity.getY(), partialTicks), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), partialTicks))).expand(0, 0.1, 0), color, false, true);
            case Jello -> JelloUtil.drawJello(matrixStack, entity, color);
        }
    }

    @Override
    public String getInfo() {
        return target == null ? null : target.getName().getString();
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
        if (check()) {
            doAura();
        }
    }

    @EventHandler(priority =  EventPriority.HIGH - 2)
    public void onRotate(RotateEvent event) {
        if (target != null && newRotate.getValue() && directionVec != null) {
            float[] newAngle = injectStep(EntityUtil.getLegitRotations(directionVec), yawStep.getValueFloat());
            lastYaw = newAngle[0];
            lastPitch = newAngle[1];
            event.setYaw(lastYaw);
            event.setPitch(lastPitch);
        } else {
            lastYaw = Nullpoint.ROTATE.lastYaw;
            lastPitch = Nullpoint.ROTATE.lastPitch;
        }
    }
    private boolean check() {
        int at = attackTicks;
        if (cd.getValue() == Cooldown.Vanilla) {
            at = ((ILivingEntity) mc.player).getLastAttackedTicks();
        }
        if (!(Math.max(at / getAttackCooldownProgressPerTick(), 0.0F) >= cooldown.getValue()))
            return false;
        if (ghost.getValue()) {
            if (!ghostTimer.passedMs(600)) return false;
            if (InventoryUtil.findClassInventorySlot(SwordItem.class) == -1) {
                return false;
            }
        }
        return whileEating.getValue() || !mc.player.isUsingItem();
    }

    public static float getAttackCooldownProgressPerTick() {
        return (float) (1.0 / mc.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * 20.0);
    }

    public boolean sweeping = false;
    private void doAura() {
        if (!check()) {
            return;
        }
        if (rotate.getValue()) {
            if (!faceVector(target.getPos().add(0, 1.5, 0))) return;
        }
        int slot = InventoryUtil.findItemInventorySlot(Items.NETHERITE_SWORD);
        if (ghost.getValue()) {
            sweeping = true;
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        }
        ghostTimer.reset();
        if (!ghost.getValue() && Criticals.INSTANCE.isOn()) Criticals.INSTANCE.doCrit();
        mc.interactionManager.attackEntity(mc.player, target);
        EntityUtil.swingHand(Hand.MAIN_HAND, swingMode.getValue());
        if (ghost.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
            sweeping = false;
        }
        attackTicks = 0;
    }

    public boolean faceVector(Vec3d directionVec) {
        if (!newRotate.getValue()) {
            EntityUtil.faceVectorNoStay(directionVec);
            return true;
        } else {
            this.directionVec = directionVec;
            float[] angle = EntityUtil.getLegitRotations(directionVec);
            if (Math.abs(MathHelper.wrapDegrees(angle[0] - lastYaw)) < fov.getValueFloat() && Math.abs(MathHelper.wrapDegrees(angle[1] - lastPitch)) < fov.getValueFloat()) {
                EntityUtil.sendYawAndPitch(angle[0], angle[1]);
                return true;
            }
        }
        return !checkLook.getValue();
    }
    
    private Entity getTarget() {
        Entity target = null;
        double distance = range.getValue();
        double maxHealth = 36.0;
        for (Entity entity : mc.world.getEntities()) {
            if (!isEnemy(entity)) continue;
            if (!mc.player.canSee(entity) && mc.player.distanceTo(entity) > wallRange.getValue()) {
                continue;
            }
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
        return target;
    }
    private boolean isEnemy(Entity entity) {
        if (entity instanceof SlimeEntity && Slimes.getValue()) return true;
        if (entity instanceof PlayerEntity && Players.getValue()) return true;
        if (entity instanceof VillagerEntity && Villagers.getValue()) return true;
        if (!(entity instanceof VillagerEntity) && entity instanceof MobEntity && Mobs.getValue()) return true;
        if (entity instanceof AnimalEntity && Animals.getValue()) return true;
        return false;
    }

    private float[] injectStep(float[] angle, float steps) {
        if (steps < 0.1f) steps = 0.1f;

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
    private enum TargetMode {
        DISTANCE,
        HEALTH,
    }

    public enum Page {
        General,
        Rotate,
        Target,
        Render
    }
}