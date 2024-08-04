package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.Render3DEvent;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.settings.Placement;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class AutoTrap
        extends Module {
    final Timer timer = new Timer();
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true));
    private final SliderSetting blocksPer = add(new SliderSetting("BlocksPer", 1, 1, 8));
    private final BooleanSetting autoDisable =
            add(new BooleanSetting("AutoDisable", true));
    private final SliderSetting range =
            add(new SliderSetting("Range", 5.0f, 1.0f, 8.0f).setSuffix("m"));
    private final EnumSetting<TargetMode> targetMod =
            add(new EnumSetting<>("TargetMode", TargetMode.Single));
    private final BooleanSetting checkMine =
            add(new BooleanSetting("DetectMining", false));
    private final BooleanSetting helper =
            add(new BooleanSetting("Helper", true));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true));
    private final BooleanSetting extend =
            add(new BooleanSetting("Extend", true));
    private final BooleanSetting antiStep =
            add(new BooleanSetting("AntiStep", false));
    private final BooleanSetting onlyBreak =
            add(new BooleanSetting("OnlyBreak", false, v -> antiStep.getValue()));
    private final BooleanSetting head =
            add(new BooleanSetting("Head", true));
    private final BooleanSetting headExtend =
            add(new BooleanSetting("HeadExtend", true));
    private final BooleanSetting headAnchor =
            add(new BooleanSetting("HeadAnchor", true));
    private final BooleanSetting chestUp =
            add(new BooleanSetting("ChestUp", true));
    private final BooleanSetting onlyBreaking =
            add(new BooleanSetting("OnlyBreaking", false, v -> chestUp.getValue()));
    private final BooleanSetting chest =
            add(new BooleanSetting("Chest", true));
    private final BooleanSetting onlyGround =
            add(new BooleanSetting("OnlyGround", false, v -> chest.getValue()));
    private final BooleanSetting legs =
            add(new BooleanSetting("Legs", false));
    private final BooleanSetting legAnchor =
            add(new BooleanSetting("LegAnchor", true));
    private final BooleanSetting down =
            add(new BooleanSetting("Down", false));
    private final BooleanSetting onlyHole =
            add(new BooleanSetting("OnlyHole", false));
    private final BooleanSetting breakCrystal =
            add(new BooleanSetting("Break", true));
    private final BooleanSetting usingPause = add(new BooleanSetting("UsingPause", true));
    public final SliderSetting delay =
            add(new SliderSetting("Delay", 100, 0, 500).setSuffix("ms"));
    private final SliderSetting placeRange =
            add(new SliderSetting("PlaceRange", 4.0f, 1.0f, 6.0f).setSuffix("m"));
    private final BooleanSetting selfGround = add(new BooleanSetting("SelfGround", true));
    public final BooleanSetting render =
            add(new BooleanSetting("Render", true));
    public final BooleanSetting box = add(new BooleanSetting("Box", true, v -> render.getValue()));
    public final BooleanSetting outline = add(new BooleanSetting("Outline", false, v -> render.getValue()));
    public final ColorSetting color =  add(new ColorSetting("Color",new Color(255, 255, 255, 100), v -> render.getValue()));
    public final SliderSetting fadeTime = add(new SliderSetting("FadeTime", 500, 0, 5000, v -> render.getValue()).setSuffix("ms"));
    public final BooleanSetting pre = add(new BooleanSetting("Pre", false, v -> render.getValue()));
    public final BooleanSetting sync = add(new BooleanSetting("Sync", true, v -> render.getValue()));
    public PlayerEntity target;
    public static AutoTrap INSTANCE;

    public AutoTrap() {
        super("AutoTrap", "Automatically trap the enemy", Category.Combat);
        INSTANCE = this;
        Nullpoint.EVENT_BUS.subscribe(new AutoTrapRender());
    }

    public enum TargetMode {
        Single, Multi
    }

    int progress = 0;
    private final ArrayList<BlockPos> trapList = new ArrayList<>();
    private final ArrayList<BlockPos> placeList = new ArrayList<>();

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        trapList.clear();
        placeList.clear();
        progress = 0;
        if (selfGround.getValue() && !mc.player.isOnGround()) {
            target = null;
            return;
        }
        if (usingPause.getValue() && EntityUtil.isUsing()) {
            target = null;
            return;
        }
        if (!timer.passedMs((long) delay.getValue())) {
            return;
        }
        if (targetMod.getValue() == TargetMode.Single) {
            target = CombatUtil.getClosestEnemy(range.getValue());
            if (target == null) {
                if (autoDisable.getValue()) disable();
                return;
            }
            trapTarget(target);
        } else if (targetMod.getValue() == TargetMode.Multi) {
            boolean found = false;
            for (PlayerEntity player : CombatUtil.getEnemies(range.getValue())) {
                found = true;
                target = player;
                trapTarget(target);
            }
            if (!found) {
                if (autoDisable.getValue()) disable();
                target = null;
            }
        }
    }

    private void trapTarget(PlayerEntity target) {
        if (onlyHole.getValue() && !BlockUtil.isHole(EntityUtil.getEntityPos(target))) return;
        doTrap(EntityUtil.getEntityPos(target, true));
    }

    private void doTrap(BlockPos pos) {
        if (trapList.contains(pos)) return;
        trapList.add(pos);
        if (legs.getValue()) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos offsetPos = pos.offset(i);
                tryPlaceBlock(offsetPos, legAnchor.getValue());
                if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, breakCrystal.getValue()) && getHelper(offsetPos) != null)
                    tryPlaceObsidian(getHelper(offsetPos));
            }
        }
        if (headExtend.getValue()) {
            for (int x : new int[]{1, 0, -1}) {
                for (int z : new int[]{1, 0, -1}) {
                    BlockPos offsetPos = pos.add(z, 0, x);
                    if (checkEntity(new BlockPos(offsetPos))) tryPlaceBlock(offsetPos.up(2), headAnchor.getValue());
                }
            }
        }
        if (head.getValue()) {
            if (BlockUtil.clientCanPlace(pos.up(2), breakCrystal.getValue())) {
                if (BlockUtil.getPlaceSide(pos.up(2)) == null) {
                    boolean trapChest = helper.getValue();
                    if (getHelper(pos.up(2)) != null) {
                        tryPlaceObsidian(getHelper(pos.up(2)));
                        trapChest = false;
                    }
                    if (trapChest) {
                        for (Direction i : Direction.values()) {
                            if (i == Direction.DOWN || i == Direction.UP) continue;
                            BlockPos offsetPos = pos.offset(i).up();
                            if (BlockUtil.clientCanPlace(offsetPos.up(), breakCrystal.getValue())) {
                                if (BlockUtil.canPlace(offsetPos, placeRange.getValue(), breakCrystal.getValue())) {
                                    tryPlaceObsidian(offsetPos);
                                    trapChest = false;
                                    break;
                                }
                            }
                        }
                        if (trapChest) {
                            for (Direction i : Direction.values()) {
                                if (i == Direction.DOWN || i == Direction.UP) continue;
                                BlockPos offsetPos = pos.offset(i).up();
                                if (BlockUtil.clientCanPlace(offsetPos.up(), breakCrystal.getValue())) {
                                    if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, breakCrystal.getValue()) && getHelper(offsetPos) != null) {
                                        tryPlaceObsidian(getHelper(offsetPos));
                                        trapChest = false;
                                        break;
                                    }
                                }
                            }
                            if (trapChest) {
                                for (Direction i : Direction.values()) {
                                    if (i == Direction.DOWN || i == Direction.UP) continue;
                                    BlockPos offsetPos = pos.offset(i).up();
                                    if (BlockUtil.clientCanPlace(offsetPos.up(), breakCrystal.getValue())) {
                                        if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, breakCrystal.getValue()) && getHelper(offsetPos) != null) {
                                            if (BlockUtil.getPlaceSide(offsetPos.down()) == null && BlockUtil.clientCanPlace(offsetPos.down(), breakCrystal.getValue()) && getHelper(offsetPos.down()) != null) {
                                                tryPlaceObsidian(getHelper(offsetPos.down()));
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                tryPlaceBlock(pos.up(2), headAnchor.getValue());
            }
        }
        if (antiStep.getValue() && (BlockUtil.isMining(pos.up(2)) || !onlyBreak.getValue())) {
            if (BlockUtil.getPlaceSide(pos.up(3)) == null && BlockUtil.clientCanPlace(pos.up(3), breakCrystal.getValue())) {
                if (getHelper(pos.up(3), Direction.DOWN) != null) {
                    tryPlaceObsidian(getHelper(pos.up(3)));
                }
            }
            tryPlaceObsidian(pos.up(3));
        }
        if (down.getValue()) {
            BlockPos offsetPos = pos.down();
            tryPlaceObsidian(offsetPos);
            if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, breakCrystal.getValue()) && getHelper(offsetPos) != null)
                tryPlaceObsidian(getHelper(offsetPos));
        }
        if (chestUp.getValue()) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos offsetPos = pos.offset(i).up(2);
                if (!onlyBreaking.getValue() || BlockUtil.isMining(pos.up(2))) {
                    tryPlaceObsidian(offsetPos);
                    if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, breakCrystal.getValue())) {
                        if (getHelper(offsetPos) != null) {
                            tryPlaceObsidian(getHelper(offsetPos));
                        } else if (BlockUtil.getPlaceSide(offsetPos.down()) == null && BlockUtil.clientCanPlace(offsetPos.down(), breakCrystal.getValue()) && getHelper(offsetPos.down()) != null) {
                            tryPlaceObsidian(getHelper(offsetPos.down()));
                        }
                    }
                }
            }
        }
        if (chest.getValue() && (!onlyGround.getValue() || target.isOnGround())) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos offsetPos = pos.offset(i).up();
                tryPlaceObsidian(offsetPos);
                if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, breakCrystal.getValue())) {
                    if (getHelper(offsetPos) != null) {
                        tryPlaceObsidian(getHelper(offsetPos));
                    } else
                    if (BlockUtil.getPlaceSide(offsetPos.down()) == null && BlockUtil.clientCanPlace(offsetPos.down(), breakCrystal.getValue()) && getHelper(offsetPos.down()) != null) {
                        tryPlaceObsidian(getHelper(offsetPos.down()));
                    }
                }
            }
        }
        if (extend.getValue()) {
            for (int x : new int[]{1, 0, -1}) {
                for (int z : new int[]{1, 0, -1}) {
                    BlockPos offsetPos = pos.add(x, 0, z);
                    if (checkEntity(new BlockPos(offsetPos))) doTrap(offsetPos);
                }
            }
        }
    }

    @Override
    public String getInfo() {
        if (target != null) {
            return target.getName().getString();
        }
        return null;
    }

    public BlockPos getHelper(BlockPos pos) {
        if (!helper.getValue()) return null;
        for (Direction i : Direction.values()) {
            if (checkMine.getValue() && BlockUtil.isMining(pos.offset(i))) continue;
            if (CombatSetting.INSTANCE.placement.getValue() == Placement.Strict && !BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite(), true)) continue;
            if (BlockUtil.canPlace(pos.offset(i), placeRange.getValue(), breakCrystal.getValue())) return pos.offset(i);
        }
        return null;
    }

    public BlockPos getHelper(BlockPos pos, Direction ignore) {
        if (!helper.getValue()) return null;
        for (Direction i : Direction.values()) {
            if (i == ignore) continue;
            if (checkMine.getValue() && BlockUtil.isMining(pos.offset(i))) continue;
            if (!BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite(), true)) continue;
            if (BlockUtil.canPlace(pos.offset(i), placeRange.getValue(), breakCrystal.getValue())) return pos.offset(i);
        }
        return null;
    }
    private boolean checkEntity(BlockPos pos) {
        if (mc.player.getBoundingBox().intersects(new Box(pos))) return false;
        for (Entity entity : mc.world.getNonSpectatingEntities(PlayerEntity.class, new Box(pos))) {
            if (entity.isAlive())
                return true;
        }
        return false;
    }

    private void tryPlaceBlock(BlockPos pos, boolean anchor) {
        if (pre.getValue()) AutoTrapRender.addBlock(pos);
        if (placeList.contains(pos)) return;
        if (BlockUtil.isMining(pos)) return;
        if (!BlockUtil.canPlace(pos, 6, breakCrystal.getValue())) return;
        if (!(progress < blocksPer.getValue())) return;
        if (MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos())) > placeRange.getValue())
            return;
        int old = mc.player.getInventory().selectedSlot;
        int block = anchor && getAnchor() != -1 ? getAnchor() : getBlock();
        if (block == -1) return;
        if (!pre.getValue()) AutoTrapRender.addBlock(pos);
        placeList.add(pos);
        CombatUtil.attackCrystal(pos, rotate.getValue(), usingPause.getValue());
        doSwap(block);
        BlockUtil.placeBlock(pos, rotate.getValue());
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.syncInventory();
        } else {
            doSwap(old);
        }
        timer.reset();
        progress++;
    }
    private void tryPlaceObsidian(BlockPos pos) {
        if (pre.getValue()) AutoTrapRender.addBlock(pos);
        if (placeList.contains(pos)) return;
        if (BlockUtil.isMining(pos)) return;
        if (!BlockUtil.canPlace(pos, 6, breakCrystal.getValue())) return;
        if (!(progress < blocksPer.getValue())) return;
        if (MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos())) > placeRange.getValue()) return;
        int old = mc.player.getInventory().selectedSlot;
        int block = getBlock();
        if (block == -1) return;
        if (!pre.getValue()) AutoTrapRender.addBlock(pos);
        placeList.add(pos);
        CombatUtil.attackCrystal(pos, rotate.getValue(), usingPause.getValue());
        doSwap(block);
        BlockUtil.placeBlock(pos, rotate.getValue());
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.syncInventory();
        } else {
            doSwap(old);
        }
        timer.reset();
        progress++;
    }

    private void doSwap(int slot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getBlock() {
        if (inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
        } else {
            return InventoryUtil.findBlock(Blocks.OBSIDIAN);
        }
    }

    private int getAnchor() {
        if (inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.RESPAWN_ANCHOR);
        } else {
            return InventoryUtil.findBlock(Blocks.RESPAWN_ANCHOR);
        }
    }

    public class AutoTrapRender {
        public static final HashMap<BlockPos, placePosition> PlaceMap = new HashMap<>();
        public static void addBlock(BlockPos pos) {
            if (BlockUtil.clientCanPlace(pos, true) && !PlaceMap.containsKey(pos)) PlaceMap.put(pos, new placePosition(pos));
        }

        private void drawBlock(BlockPos pos, double alpha, Color color, MatrixStack matrixStack) {
            if (sync.getValue()) {
                color = INSTANCE.color.getValue();
            }
            Render3DUtil.draw3DBox(matrixStack, new Box(pos), ColorUtil.injectAlpha(color, (int) alpha), outline.getValue(), box.getValue());
        }

        @EventHandler
        public void onRender3D(Render3DEvent event) {
            if (!render.getValue()) return;
            if (PlaceMap.isEmpty()) return;
            boolean shouldClear = true;
            for (placePosition placePosition : PlaceMap.values()) {
                if (!BlockUtil.clientCanPlace(placePosition.pos, true)) {
                    placePosition.isAir = false;
                }
                if (!placePosition.timer.passedMs((long) (delay.getValue() + 100)) && placePosition.isAir) {
                    placePosition.firstFade.reset();
                }
                if (placePosition.firstFade.getQuad(FadeUtils.Quad.In2) == 1) continue;
                shouldClear = false;
                drawBlock(placePosition.pos, (double) color.getValue().getAlpha() * (1 - placePosition.firstFade.getQuad(FadeUtils.Quad.In2)), placePosition.posColor, event.getMatrixStack());
            }
            if (shouldClear) PlaceMap.clear();
        }

        public static class placePosition {
            public final FadeUtils firstFade;
            public final BlockPos pos;
            public final Color posColor;
            public final Timer timer;
            public boolean isAir;
            public placePosition(BlockPos placePos) {
                this.firstFade = new FadeUtils((long) AutoTrap.INSTANCE.fadeTime.getValue());
                this.pos = placePos;
                this.posColor = AutoTrap.INSTANCE.color.getValue();
                this.timer = new Timer();
                this.isAir = true;
                timer.reset();
            }
        }
    }
}
