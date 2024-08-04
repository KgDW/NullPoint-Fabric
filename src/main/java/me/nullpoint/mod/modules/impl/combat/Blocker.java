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
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Blocker extends Module {
    public static Blocker INSTANCE ;
    final Timer timer = new Timer();
    private final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));
    private final SliderSetting delay =
            add(new SliderSetting("PlaceDelay", 50, 0, 500, v -> page.getValue() == Page.General));
    private final SliderSetting blocksPer =
            add(new SliderSetting("BlocksPer", 1, 1, 8, v -> page.getValue() == Page.General));
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true, v -> page.getValue() == Page.General));
    private final BooleanSetting breakCrystal =
            add(new BooleanSetting("Break", true, v -> page.getValue() == Page.General));
    private final BooleanSetting inventorySwap =
            add(new BooleanSetting("InventorySwap", true, v -> page.getValue() == Page.General));

    private final BooleanSetting bevelCev =
            add(new BooleanSetting("BevelCev", true, v -> page.getValue() == Page.Target));
    private final BooleanSetting feet =
            add(new BooleanSetting("Feet", true, v -> page.getValue() == Page.Target).setParent());
    private final BooleanSetting onlySurround =
            add(new BooleanSetting("OnlySurround", true, v -> page.getValue() == Page.Target && feet.isOpen()));

    private final BooleanSetting inAirPause =
            add(new BooleanSetting("InAirPause", false, v -> page.getValue() == Page.Check));
    private final BooleanSetting detectMining =
            add(new BooleanSetting("DetectMining", true, v -> page.getValue() == Page.Check));
    private final BooleanSetting eatingPause = add(new BooleanSetting("EatingPause", true, v -> page.getValue() == Page.Check));

    public final BooleanSetting render =
            add(new BooleanSetting("Render", true, v -> page.getValue() == Page.Render));
    final ColorSetting box =
            add(new ColorSetting("Box", new Color(255, 255, 255, 255), v -> page.getValue() == Page.Render).injectBoolean(true));
    final ColorSetting fill =
            add(new ColorSetting("Fill", new Color(255, 255, 255, 100), v -> page.getValue() == Page.Render).injectBoolean(true));
    public final SliderSetting fadeTime = add(new SliderSetting("FadeTime", 500, 0, 5000, v -> render.getValue()));

    private final List<BlockPos> placePos = new ArrayList<>();
    private int placeProgress = 0;

    public Blocker() {
        super("Blocker", Category.Combat);
        INSTANCE = this;
        Nullpoint.EVENT_BUS.subscribe(new BlockerRenderer());
    }

    private BlockPos playerBP;

    @Override
    public void onUpdate() {
        if (!timer.passedMs(delay.getValue())) return;
        if (eatingPause.getValue() && EntityUtil.isUsing()) return;
        placeProgress = 0;

        if (playerBP != null && !playerBP.equals(EntityUtil.getPlayerPos(true))) {
            placePos.clear();
        }
        playerBP = EntityUtil.getPlayerPos(true);

        if (bevelCev.getValue()) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN) continue;
                if (isBedrock(playerBP.offset(i).up())) continue;

                BlockPos blockerPos = playerBP.offset(i).up(2);
                if (crystalHere(blockerPos) && !placePos.contains(blockerPos)) {
                    placePos.add(blockerPos);
                }
            }
        }
        if (getObsidian() == -1) {
            return;
        }

        if (inAirPause.getValue() && !mc.player.isOnGround()) return;
        placePos.removeIf((pos) -> !BlockUtil.clientCanPlace(pos, true));
        if (feet.getValue() && (!onlySurround.getValue() || FeetTrap.INSTANCE.isOn())) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos surroundPos = playerBP.offset(i);
                if (isBedrock(surroundPos)) continue;
                if (BlockUtil.isMining(surroundPos)) {
                    for (Direction direction : Direction.values()) {
                        if (direction == Direction.DOWN || direction == Direction.UP) continue;
                        BlockPos defensePos = playerBP.offset(i).offset(direction);
                        if (breakCrystal.getValue()) {
                            CombatUtil.attackCrystal(defensePos, rotate.getValue(), false);
                        }
                        if (BlockUtil.canPlace(defensePos, 6, breakCrystal.getValue())) {
                            tryPlaceObsidian(defensePos);
                        }
                    }
                    BlockPos defensePos = playerBP.offset(i).up();
                    if (breakCrystal.getValue()) {
                        CombatUtil.attackCrystal(defensePos, rotate.getValue(), false);
                    }
                    if (BlockUtil.canPlace(defensePos, 6, breakCrystal.getValue())) {
                        tryPlaceObsidian(defensePos);
                    }
                }
            }
        }

        for (BlockPos defensePos : placePos) {
            if (breakCrystal.getValue() && crystalHere(defensePos)) {
                CombatUtil.attackCrystal(defensePos, rotate.getValue(), false);
            }
            if (BlockUtil.canPlace(defensePos, 6, breakCrystal.getValue())) {
                tryPlaceObsidian(defensePos);
            }
        }
    }

    private boolean crystalHere(BlockPos pos) {
        return mc.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(pos)).stream().anyMatch(entity -> entity.getBlockPos().equals(pos));
    }

    private boolean isBedrock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK;
    }
    private void tryPlaceObsidian(BlockPos pos) {
        if (!(placeProgress < blocksPer.getValue())) return;
        if (detectMining.getValue() && BlockUtil.isMining(pos)) {
            return;
        }
        int oldSlot = mc.player.getInventory().selectedSlot;
        int block;
        if ((block = getObsidian()) == -1) {
            return;
        }
        doSwap(block);
        BlockUtil.placeBlock(pos, rotate.getValue());
        if (inventorySwap.getValue()) {
            doSwap(block);
            EntityUtil.syncInventory();
        } else {
            doSwap(oldSlot);
        }
        placeProgress++;
        BlockerRenderer.addBlock(pos);
        timer.reset();
    }

    private void doSwap(int slot) {
        if (inventorySwap.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }
    private int getObsidian() {
        if (inventorySwap.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
        } else {
            return InventoryUtil.findBlock(Blocks.OBSIDIAN);
        }
    }

    public enum Page {
        General,
        Target,
        Check,
        Render
    }

    public class BlockerRenderer {
        public static final HashMap<BlockPos, placePosition> renderMap = new HashMap<>();
        public static void addBlock(BlockPos pos) {
            renderMap.put(pos, new placePosition(pos));
        }

        @EventHandler
        public void onRender3D(Render3DEvent event) {
            if (!INSTANCE.render.getValue()) return;
            if (renderMap.isEmpty()) return;
            boolean shouldClear = true;
            for (placePosition placePosition : renderMap.values()) {
                if (!BlockUtil.clientCanPlace(placePosition.pos, true)) {
                    placePosition.isAir = false;
                }
                if (!placePosition.timer.passedMs((long) (delay.getValue() + 100)) && placePosition.isAir) {
                    placePosition.firstFade.reset();
                }
                if (placePosition.firstFade.getQuad(FadeUtils.Quad.In2) == 1) continue;
                shouldClear = false;
                MatrixStack matrixStack = event.getMatrixStack();
                if (INSTANCE.fill.booleanValue) {
                    Render3DUtil.drawFill(matrixStack, new Box(placePosition.pos), ColorUtil.injectAlpha(INSTANCE.fill.getValue(), (int) ((double) fill.getValue().getAlpha() * (1 - placePosition.firstFade.getQuad(FadeUtils.Quad.In2)))));
                }
                if (INSTANCE.box.booleanValue) {
                    Render3DUtil.drawBox(matrixStack, new Box(placePosition.pos), ColorUtil.injectAlpha(INSTANCE.box.getValue(), (int) ((double) box.getValue().getAlpha() * (1 - placePosition.firstFade.getQuad(FadeUtils.Quad.In2)))));
                }
            }
            if (shouldClear) renderMap.clear();
        }


        public static class placePosition {
            public final FadeUtils firstFade;
            public final BlockPos pos;
            public final Timer timer;
            public boolean isAir;
            public placePosition(BlockPos placePos) {
                this.firstFade = new FadeUtils((long) INSTANCE.fadeTime.getValue());
                this.pos = placePos;
                this.timer = new Timer();
                this.isAir = true;
            }
        }
    }
}