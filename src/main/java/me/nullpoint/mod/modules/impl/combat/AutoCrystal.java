package me.nullpoint.mod.modules.impl.combat;

import com.mojang.authlib.GameProfile;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.Render3DEvent;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.utils.combat.*;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import me.nullpoint.mod.modules.settings.SwingSide;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import static me.nullpoint.api.utils.world.BlockUtil.getBlock;

public class AutoCrystal extends Module {
    public static AutoCrystal INSTANCE;
    public static BlockPos tempPos;
    public static BlockPos crystalPos;
    private final EnumSetting<Page> page =
            add(new EnumSetting<>("Page", Page.General));
    //General
    private final BooleanSetting preferAnchor =
            add(new BooleanSetting("PreferAnchor", true, v -> page.getValue() == Page.General));
    private final EnumSetting<SwingSide> swingMode = add(new EnumSetting<>("Swing", SwingSide.Server, v -> page.getValue() == Page.General));
    private final BooleanSetting eatingPause =
            add(new BooleanSetting("EatingPause", true, v -> page.getValue() == Page.General));
    private final SliderSetting switchCooldown =
            add(new SliderSetting("SwitchPause", 100, 0, 1000, v -> page.getValue() == Page.General).setSuffix("ms"));
    private final SliderSetting targetRange =
            add(new SliderSetting("TargetRange", 12.0, 0.0, 20.0, v -> page.getValue() == Page.General).setSuffix("m"));
    private final SliderSetting updateDelay =
            add(new SliderSetting("UpdateDelay", 50, 0, 1000, v -> page.getValue() == Page.General).setSuffix("ms"));
    private final SliderSetting wallRange =
            add(new SliderSetting("WallRange", 6.0, 0.0, 6.0, v -> page.getValue() == Page.General).setSuffix("m"));

    //Rotate
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true, v -> page.getValue() == Page.Rotate).setParent());
    private final BooleanSetting onBreak =
            add(new BooleanSetting("OnBreak", false, v -> rotate.isOpen() && page.getValue() == Page.Rotate));
    private final BooleanSetting yawStep =
            add(new BooleanSetting("YawStep", false, v -> rotate.isOpen() && page.getValue() == Page.Rotate));
    private final SliderSetting steps =
            add(new SliderSetting("Steps", 0.3f, 0.1f, 1.0f, 0.01f, v -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
    private final BooleanSetting random =
            add(new BooleanSetting("Random", true, v -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
    private final BooleanSetting packet =
            add(new BooleanSetting("Packet", false, v -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
    private final BooleanSetting checkLook =
            add(new BooleanSetting("CheckLook", true, v -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
    private final SliderSetting fov =
            add(new SliderSetting("Fov", 30f, 0f, 90f, v -> rotate.isOpen() && yawStep.getValue() && checkLook.getValue() && page.getValue() == Page.Rotate));

    //Place
    private final SliderSetting minDamage =
            add(new SliderSetting("Min", 5.0, 0.0, 36.0, v -> page.getValue() == Page.Interact).setSuffix("dmg"));
    private final SliderSetting maxSelf =
            add(new SliderSetting("Self", 12.0, 0.0, 36.0, v -> page.getValue() == Page.Interact).setSuffix("dmg"));
    private final SliderSetting range =
            add(new SliderSetting("Range", 5.0, 0.0, 6, v -> page.getValue() == Page.Interact).setSuffix("m"));
    private final SliderSetting noSuicide =
            add(new SliderSetting("NoSuicide", 3.0, 0.0, 10.0, v -> page.getValue() == Page.Interact).setSuffix("dmg"));

    private final BooleanSetting place =
            add(new BooleanSetting("Place", true, v -> page.getValue() == Page.Interact).setParent());
    private final SliderSetting placeDelay =
            add(new SliderSetting("PlaceDelay", 300, 0, 1000, v -> page.getValue() == Page.Interact && place.isOpen()).setSuffix("ms"));
    private final EnumSetting<SwapMode> autoSwap =
            add(new EnumSetting<>("AutoSwap", SwapMode.Off, v -> page.getValue() == Page.Interact && place.isOpen()));
    private final BooleanSetting spam =
            add(new BooleanSetting("Spam", true, v -> page.getValue() == Page.Interact && place.isOpen()));

    private final BooleanSetting Break =
            add(new BooleanSetting("Break", true, v -> page.getValue() == Page.Interact).setParent());
    private final SliderSetting breakDelay =
            add(new SliderSetting("BreakDelay", 300, 0, 1000, v -> page.getValue() == Page.Interact && Break.isOpen()).setSuffix("ms"));
    private final BooleanSetting breakOnlyHasCrystal =
            add(new BooleanSetting("OnlyHasCrystal", false, v -> page.getValue() == Page.Interact && Break.isOpen()));
    private final BooleanSetting breakRemove =
            add(new BooleanSetting("Remove", false, v -> page.getValue() == Page.Interact && Break.isOpen()));
    //Render
    private final EnumSetting<Aura.TargetESP> mode = add(new EnumSetting<>("TargetESP", Aura.TargetESP.Jello, v -> page.getValue() == Page.Render));
    private final ColorSetting color = add(new ColorSetting("TargetColor", new Color(255, 255, 255, 250), v -> page.getValue() == Page.Render));
    final ColorSetting text =
            add(new ColorSetting("Text", new Color(-1), v -> page.getValue() == Page.Render).injectBoolean(true));
    final BooleanSetting render =
            add(new BooleanSetting("Render", true, v -> page.getValue() == Page.Render));
    final BooleanSetting shrink =
            add(new BooleanSetting("Shrink", true, v -> page.getValue() == Page.Render && render.getValue()));
    final ColorSetting box =
            add(new ColorSetting("Box", new Color(255, 255, 255, 255), v -> page.getValue() == Page.Render && render.getValue()).injectBoolean(true));
    private final BooleanSetting bold = add(new BooleanSetting("Bold", false, v -> page.getValue() == Page.Render && box.booleanValue)).setParent();
    private final SliderSetting lineWidth = add(new SliderSetting("LineWidth", 4, 1, 5, v -> page.getValue() == Page.Render && bold.isOpen() && box.booleanValue));

    final ColorSetting fill =
            add(new ColorSetting("Fill", new Color(255, 255, 255, 100), v -> page.getValue() == Page.Render && render.getValue()).injectBoolean(true));
    final SliderSetting sliderSpeed = add(new SliderSetting("SliderSpeed", 0.2, 0.01, 1, 0.01, v -> page.getValue() == Page.Render && render.getValue()));
    final SliderSetting startFadeTime =
            add(new SliderSetting("StartFade", 0.3d, 0d, 2d, 0.01, v -> page.getValue() == Page.Render && render.getValue()).setSuffix("s"));
    final SliderSetting fadeSpeed =
            add(new SliderSetting("FadeSpeed", 0.2d, 0.01d, 1d, 0.01, v -> page.getValue() == Page.Render && render.getValue()));
    //Calc
    private final BooleanSetting smart =
            add(new BooleanSetting("Smart", true, v -> page.getValue() == Page.Calc));
    private final BooleanSetting useThread =
            add(new BooleanSetting("UseThread", true, v -> page.getValue() == Page.Calc));
    private final BooleanSetting doCrystal =
            add(new BooleanSetting("CalcDoCrystal", false, v -> page.getValue() == Page.Calc));
    private final BooleanSetting lite =
            add(new BooleanSetting("Lite", false, v -> page.getValue() == Page.Calc));
    private final EnumSetting<AnchorAura.CalcMode> calcMode = add(new EnumSetting<>("CalcMode", AnchorAura.CalcMode.OyVey, v -> page.getValue() == Page.Calc));
    private final SliderSetting predictTicks =
            add(new SliderSetting("Predict", 4, 0, 10, v -> page.getValue() == Page.Calc).setSuffix("ticks"));
    private final BooleanSetting terrainIgnore =
            add(new BooleanSetting("TerrainIgnore", true, v -> page.getValue() == Page.Calc));
    //Misc
    private final BooleanSetting antiSurround =
            add(new BooleanSetting("AntiSurround", true, v -> page.getValue() == Page.Misc).setParent());
    private final SliderSetting antiSurroundMax =
            add(new SliderSetting("WhenLower", 5.0, 0.0, 36.0, v -> page.getValue() == Page.Misc && antiSurround.isOpen()).setSuffix("dmg"));
    private final BooleanSetting slowPlace =
            add(new BooleanSetting("Timeout", true, v -> page.getValue() == Page.Misc).setParent());
    private final SliderSetting slowDelay =
            add(new SliderSetting("TimeoutDelay", 600, 0, 2000, v -> page.getValue() == Page.Misc && slowPlace.isOpen()).setSuffix("ms"));
    private final SliderSetting slowMinDamage =
            add(new SliderSetting("TimeoutMin", 1.5, 0.0, 36.0, v -> page.getValue() == Page.Misc && slowPlace.isOpen()).setSuffix("dmg"));
    private final BooleanSetting forcePlace =
            add(new BooleanSetting("ForcePlace", true, v -> page.getValue() == Page.Misc).setParent());
    private final SliderSetting forceMaxHealth =
            add(new SliderSetting("LowerThan", 7, 0, 36, v -> page.getValue() == Page.Misc && forcePlace.isOpen()).setSuffix("health"));
    private final SliderSetting forceMin =
            add(new SliderSetting("ForceMin", 1.5, 0.0, 36.0, v -> page.getValue() == Page.Misc && forcePlace.isOpen()).setSuffix("dmg"));
    private final BooleanSetting armorBreaker =
            add(new BooleanSetting("ArmorBreaker", true, v -> page.getValue() == Page.Misc).setParent());
    private final SliderSetting maxDurable =
            add(new SliderSetting("MaxDurable", 8, 0, 100, v -> page.getValue() == Page.Misc && armorBreaker.isOpen()).setSuffix("%"));
    private final SliderSetting armorBreakerDamage =
            add(new SliderSetting("BreakerMin", 3.0, 0.0, 36.0, v -> page.getValue() == Page.Misc && armorBreaker.isOpen()).setSuffix("dmg"));
    private final SliderSetting hurtTime =
            add(new SliderSetting("HurtTime", 10, 0, 10, 1, v -> page.getValue() == Page.Misc));
    private final Timer switchTimer = new Timer();
    private final Timer delayTimer = new Timer();
    public static final Timer placeTimer = new Timer();
    public final Timer lastBreakTimer = new Timer();
    final Timer noPosTimer = new Timer();
    public PlayerEntity displayTarget;
    private float lastYaw = 0f;
    private float lastPitch = 0f;
    private int lastHotbar = -1;
    public float tempDamage;
    public float lastDamage;
    public Vec3d directionVec = null;
    public static Thread thread;

    public AutoCrystal() {
        super("AutoCrystal", "Recode", Category.Combat);
        INSTANCE = this;
        Nullpoint.EVENT_BUS.subscribe(new CrystalRender());
    }

    @Override
    public String getInfo() {
        if (displayTarget != null && lastDamage > 0)
            return displayTarget.getName().getString() + ", " + new DecimalFormat("0.0").format(lastDamage);
        return null;
    }

    @Override
    public void onDisable() {
        crystalPos = null;
        tempPos = null;
    }

    @Override
    public void onEnable() {
        lastYaw = Nullpoint.ROTATE.lastYaw;
        lastPitch = Nullpoint.ROTATE.lastPitch;
        lastBreakTimer.reset();
    }

    @Override
    public void onUpdate() {
        if (useThread.getValue()) {
            if (thread == null || !thread.isAlive()) {
                thread = new Thread(() -> {
                    while (INSTANCE.isOn() && useThread.getValue()) {
                        updateCrystalPos();
                    }
                    crystalPos = null;
                    tempPos = null;
                });
                try {
                    thread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            updateCrystalPos();
        }
        if (crystalPos != null) {
            doCrystal(crystalPos);
        }
    }


    @EventHandler
    public void onUpdateWalking(UpdateWalkingEvent event) {
        if (!useThread.getValue()) updateCrystalPos();
        if (crystalPos != null) {
            doCrystal(crystalPos);
        }
    }

    static Vec3d placeVec3d;
    static Vec3d curVec3d;
    double fade = 0;

    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        if (!useThread.getValue()) updateCrystalPos();
        if (crystalPos != null) {
            doCrystal(crystalPos);
        }
        if (AutoCrystal.INSTANCE.displayTarget != null && !AutoCrystal.INSTANCE.noPosTimer.passedMs(500)) {
            Aura.doRender(matrixStack, partialTicks, AutoCrystal.INSTANCE.displayTarget, color.getValue(), mode.getValue());
        }
    }

    @EventHandler()
    public void onRotate(RotateEvent event) {
        if (rotate.getValue() && yawStep.getValue() && directionVec != null && !noPosTimer.passed(1000)) {
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

    @EventHandler(priority = -199)
    public void onPacketSend(PacketEvent.Send event) {
        if (event.isCancelled()) return;
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket updateSelectedSlotC2SPacket
        ) {
            if (updateSelectedSlotC2SPacket.getSelectedSlot() != lastHotbar) {
                lastHotbar = updateSelectedSlotC2SPacket.getSelectedSlot();
                switchTimer.reset();
            }
        }
    }

    private void updateCrystalPos() {
        update();
        lastDamage = tempDamage;
        crystalPos = tempPos;
    }

    private void update() {
        if (nullCheck()) return;
        if (!delayTimer.passedMs((long) updateDelay.getValue())) return;
        if (eatingPause.getValue() && EntityUtil.isUsing()) {
            lastBreakTimer.reset();
            tempPos = null;
            return;
        }
        if (preferAnchor.getValue() && AnchorAura.INSTANCE.currentPos != null) {
            lastBreakTimer.reset();
            tempPos = null;
            return;
        }
        if (breakOnlyHasCrystal.getValue() && !mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) && !mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) && !findCrystal()) {
            lastBreakTimer.reset();
            tempPos = null;
            return;
        }
        if (!switchTimer.passedMs((long) switchCooldown.getValue())) {
            //tempPos = null;
            return;
        }
        delayTimer.reset();

        tempPos = null;
        tempDamage = 0f;
        ArrayList<PlayerAndPredict> list = new ArrayList<>();
        for (PlayerEntity target : CombatUtil.getEnemies(targetRange.getValue())) {
            if (target.hurtTime <= hurtTime.getValueInt()) {
                list.add(new PlayerAndPredict(target));
            }
        }
        PlayerAndPredict self = new PlayerAndPredict(mc.player);
        if (list.isEmpty()) {
            lastBreakTimer.reset();
        } else {
            for (BlockPos pos : BlockUtil.getSphere((float) range.getValue() + 1)) {
                if (behindWall(pos)) continue;
                if (mc.player.getPos().distanceTo(pos.toCenterPos().add(0, -0.5, 0)) > range.getValue()) {
                    continue;
                }
                if (!canTouch(pos.down())) continue;
                if (!canPlaceCrystal(pos, true, false))
                    continue;
                for (PlayerAndPredict pap : list) {
                    if (lite.getValue() && liteCheck(pos.toCenterPos().add(0, -0.5, 0), pap.predict.getPos())) {
                        continue;
                    }
                    float damage = calculateDamage(pos, pap.player, pap.predict);
                    if (tempPos == null || damage > tempDamage) {
                        float selfDamage = calculateDamage(pos, self.player, self.predict);
                        if (selfDamage > maxSelf.getValue())
                            continue;
                        if (noSuicide.getValue() > 0 && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount() - noSuicide.getValue())
                            continue;
                        if (damage < EntityUtil.getHealth(pap.player)) {
                            if (damage < getDamage(pap.player)) continue;
                            if (smart.getValue()) {
                                if (getDamage(pap.player) == forceMin.getValue()) {
                                    if (damage < selfDamage - 2.5) {
                                        continue;
                                    }
                                } else {
                                    if (damage < selfDamage) {
                                        continue;
                                    }
                                }
                            }
                        }

                        displayTarget = pap.player;
                        tempPos = pos;
                        tempDamage = damage;
                    }
                }
            }
            if (antiSurround.getValue() && SpeedMine.breakPos != null && SpeedMine.progress >= 0.9 && !BlockUtil.hasEntity(SpeedMine.breakPos, false)) {
                if (tempDamage <= antiSurroundMax.getValueFloat()) {
                    for (PlayerAndPredict pap : list) {
                        for (Direction i : Direction.values()) {
                            if (i == Direction.DOWN || i == Direction.UP) continue;
                            BlockPos offsetPos = new BlockPosX(pap.player.getPos().add(0, 0.5, 0)).offset(i);
                            if (offsetPos.equals(SpeedMine.breakPos)) {
                                if (canPlaceCrystal(offsetPos.offset(i), false, false)) {
                                    float selfDamage = calculateDamage(offsetPos.offset(i), self.player, self.predict);
                                    if (selfDamage < maxSelf.getValue() && !(noSuicide.getValue() > 0 && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount() - noSuicide.getValue())) {
                                        tempPos = offsetPos.offset(i);
                                        if (doCrystal.getValue() && tempPos != null) {
                                            doCrystal(tempPos);
                                        }
                                        return;
                                    }
                                }
                                for (Direction ii : Direction.values()) {
                                    if (ii == Direction.DOWN || ii == i) continue;
                                    if (canPlaceCrystal(offsetPos.offset(ii), false, false)) {
                                        float selfDamage = calculateDamage(offsetPos.offset(ii), self.player, self.predict);
                                        if (selfDamage < maxSelf.getValue() && !(noSuicide.getValue() > 0 && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount() - noSuicide.getValue())) {
                                            tempPos = offsetPos.offset(ii);
                                            if (doCrystal.getValue() && tempPos != null) {
                                                doCrystal(tempPos);
                                            }
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (doCrystal.getValue() && tempPos != null) {
            doCrystal(tempPos);
        }
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

    public static boolean liteCheck(Vec3d from, Vec3d to) {
        return !canSee(from, to) && !canSee(from, to.add(0, 1.8, 0));
    }

    public static boolean canSee(Vec3d from, Vec3d to) {
        HitResult result = mc.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
        return result == null || result.getType() == HitResult.Type.MISS;
    }

    private boolean canTouch(BlockPos pos) {
        Direction side = BlockUtil.getClickSideStrict(pos);
        return side != null && pos.toCenterPos().add(new Vec3d(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5)).distanceTo(mc.player.getEyePos()) <= range.getValue();
    }

    public void doCrystal(BlockPos pos) {
        if (canPlaceCrystal(pos, false, true)) {
            if (mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) || mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) || findCrystal()) {
                doPlace(pos);
            }
        } else {
            doBreak(pos);
        }
    }

    public float calculateDamage(BlockPos pos, PlayerEntity player, PlayerEntity predict) {
        return calculateDamage(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5), player, predict);
    }

    public float calculateDamage(Vec3d pos, PlayerEntity player, PlayerEntity predict) {
        if (terrainIgnore.getValue()) {
            CombatUtil.terrainIgnore = true;
        }
        float damage = 0;
        switch (calcMode.getValue()) {
            case Meteor -> damage = (float) MeteorExplosionUtil.explosionDamage(player, pos, predict, 6);
            case Thunder -> damage = ThunderExplosionUtil.calculateDamage(pos, player, predict, 6);
            case OyVey ->
                    damage = OyveyExplosionUtil.calculateDamage(pos.getX(), pos.getY(), pos.getZ(), player, predict, 6);
            case Edit -> damage = ExplosionUtil.calculateDamage(pos.getX(), pos.getY(), pos.getZ(), player, predict, 6);
            case Mio -> damage = MioExplosionUtil.calculateDamage(pos, player, predict, 6);
        }
        CombatUtil.terrainIgnore = false;
        return damage;
    }

    private double getDamage(PlayerEntity target) {
        if (!SpeedMine.INSTANCE.obsidian.isPressed() && slowPlace.getValue() && lastBreakTimer.passedMs((long) slowDelay.getValue()) && !(BedAura.INSTANCE.isOn() && BedAura.INSTANCE.getBed() != -1)) {
            return slowMinDamage.getValue();
        }
        if (forcePlace.getValue() && EntityUtil.getHealth(target) <= forceMaxHealth.getValue() && !SpeedMine.INSTANCE.obsidian.isPressed()) {
            return forceMin.getValue();
        }
        if (armorBreaker.getValue()) {
            DefaultedList<ItemStack> armors = target.getInventory().armor;
            for (ItemStack armor : armors) {
                if (armor.isEmpty()) continue;
                if (EntityUtil.getDamagePercent(armor) > maxDurable.getValue()) continue;
                return armorBreakerDamage.getValue();
            }
        }
        return minDamage.getValue();
    }

    private boolean findCrystal() {
        if (autoSwap.getValue() == SwapMode.Off) return false;
        return getCrystal() != -1;
    }

    private void doBreak(BlockPos pos) {
        lastBreakTimer.reset();
        if (!Break.getValue()) return;
        if (!CombatUtil.breakTimer.passedMs((long) breakDelay.getValue())) return;
        for (EndCrystalEntity entity : mc.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1))) {
            if (rotate.getValue() && onBreak.getValue()) {
                if (!faceVector(entity.getPos().add(0, 0.25, 0))) return;
            }
            CombatUtil.breakTimer.reset();
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
            EntityUtil.swingHand(Hand.MAIN_HAND, swingMode.getValue());
            if (breakRemove.getValue()) {
                mc.world.removeEntity(entity.getId(), Entity.RemovalReason.KILLED);
            }
            if (tempDamage >= minDamage.getValueFloat() && spam.getValue()) {
                doPlace(pos);
            }
            break;
        }
    }

    private void doPlace(BlockPos pos) {
        if (!place.getValue()) return;
        if (!mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) && !mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) && !findCrystal()) {
            return;
        }
        if (!canTouch(pos.down())) {
            return;
        }
        BlockPos obsPos = pos.down();
        Direction facing = BlockUtil.getClickSide(obsPos);
        Vec3d vec = obsPos.toCenterPos().add(facing.getVector().getX() * 0.5, facing.getVector().getY() * 0.5, facing.getVector().getZ() * 0.5);
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

    private void doSwap(int slot) {
        if (autoSwap.getValue() == SwapMode.Silent || autoSwap.getValue() == SwapMode.Normal) {
            InventoryUtil.switchToSlot(slot);
        } else if (autoSwap.getValue() == SwapMode.Inventory) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        }
    }

    private int getCrystal() {
        if (autoSwap.getValue() == SwapMode.Silent || autoSwap.getValue() == SwapMode.Normal) {
            return InventoryUtil.findItem(Items.END_CRYSTAL);
        } else if (autoSwap.getValue() == SwapMode.Inventory) {
            return InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL);
        }
        return -1;
    }

    public void placeCrystal(BlockPos pos) {
        //PlaceRender.PlaceMap.put(pos, new PlaceRender.placePosition(pos));
        boolean offhand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
        BlockPos obsPos = pos.down();
        Direction facing = BlockUtil.getClickSide(obsPos);
        BlockUtil.clickBlock(obsPos, facing, false, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, swingMode.getValue());
    }

    public enum Page {
        General,
        Interact,
        Misc,
        Rotate,
        Calc,
        Render
    }

    public enum SwapMode {
        Off, Normal, Silent, Inventory
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

    public class PlayerAndPredict {
        final PlayerEntity player;
        final PlayerEntity predict;

        public PlayerAndPredict(PlayerEntity player) {
            this.player = player;
            if (predictTicks.getValueFloat() > 0) {
                predict = new PlayerEntity(mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {
                    @Override
                    public boolean isSpectator() {
                        return false;
                    }

                    @Override
                    public boolean isCreative() {
                        return false;
                    }
                };
                predict.setPosition(player.getPos().add(CombatUtil.getMotionVec(player, INSTANCE.predictTicks.getValueInt(), true)));
                predict.setHealth(player.getHealth());
                predict.prevX = player.prevX;
                predict.prevZ = player.prevZ;
                predict.prevY = player.prevY;
                predict.setOnGround(player.isOnGround());
                predict.getInventory().clone(player.getInventory());
                predict.setPose(player.getPose());
                for (StatusEffectInstance se : player.getStatusEffects()) {
                    predict.addStatusEffect(se);
                }
            } else {
                predict = player;
            }
        }
    }

    public class CrystalRender {

        @EventHandler
        public void onRender3D(Render3DEvent event) {
            if (crystalPos != null) {
                noPosTimer.reset();
                placeVec3d = crystalPos.down().toCenterPos();
            }
            if (placeVec3d == null) {
                return;
            }
            if (fadeSpeed.getValue() >= 1) {
                fade = noPosTimer.passedMs((long) (startFadeTime.getValue() * 1000)) ? 0 : 0.5;
            } else {
                fade = AnimateUtil.animate(fade, noPosTimer.passedMs((long) (startFadeTime.getValue() * 1000)) ? 0 : 0.5, fadeSpeed.getValue() / 10);
            }
            if (fade == 0) {
                curVec3d = null;
                return;
            }
            if (curVec3d == null || sliderSpeed.getValue() >= 1) {
                curVec3d = placeVec3d;
            } else {
                curVec3d = new Vec3d(AnimateUtil.animate(curVec3d.x, placeVec3d.x, sliderSpeed.getValue() / 10),
                        AnimateUtil.animate(curVec3d.y, placeVec3d.y, sliderSpeed.getValue() / 10),
                        AnimateUtil.animate(curVec3d.z, placeVec3d.z, sliderSpeed.getValue() / 10));
            }

            if (render.getValue()) {
                Box cbox = new Box(curVec3d, curVec3d);
                if (shrink.getValue()) {
                    cbox = cbox.expand(fade);
                } else {
                    cbox = cbox.expand(0.5);
                }
                MatrixStack matrixStack = event.getMatrixStack();
                if (fill.booleanValue) {
                    Render3DUtil.drawFill(matrixStack, cbox, ColorUtil.injectAlpha(fill.getValue(), (int) (fill.getValue().getAlpha() * fade * 2D)));
                }
                if (box.booleanValue) {
                    if (!bold.getValue()) {
                        Render3DUtil.drawBox(matrixStack, cbox, ColorUtil.injectAlpha(box.getValue(), (int) (box.getValue().getAlpha() * fade * 2D)));
                    } else {
                        Render3DUtil.drawLine(cbox, ColorUtil.injectAlpha(box.getValue(), (int) (box.getValue().getAlpha() * fade * 2D)), lineWidth.getValueInt());

                    }
                }
            }
            if (text.booleanValue && lastDamage > 0) {
                Render3DUtil.drawText3D(String.format("%.2f", lastDamage), curVec3d, text.getValue());
            }
        }
    }
}
