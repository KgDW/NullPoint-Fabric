package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.potion.PotionUtil;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoRegear extends Module {
     private final BooleanSetting autoDisable = add(new BooleanSetting("AutoDisable", true));
    private final SliderSetting disableTime =
            add(new SliderSetting("DisableTime", 500, 0, 1000));
    public final BooleanSetting rotate = add(new BooleanSetting("Rotate", true));
    private final BooleanSetting place = add(new BooleanSetting("Place", true));
    private final BooleanSetting detectMining =
            add(new BooleanSetting("DetectMining", true));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true));
    private final BooleanSetting preferOpen = add(new BooleanSetting("PerferOpen", true));
    private final BooleanSetting open = add(new BooleanSetting("Open", true));
    private final BooleanSetting close = add(new BooleanSetting("Close", true));
    private final SliderSetting range = add(new SliderSetting("Range", 4.0f, 0.0f, 6f));
    private final SliderSetting minRange = add(new SliderSetting("MinRange", 1.0f, 0.0f, 3f));
    private final BooleanSetting mine = add(new BooleanSetting("Mine", true));
    private final BooleanSetting take = add(new BooleanSetting("Take", true));
    private final SliderSetting empty = add(new SliderSetting("Empty", 1, 0, 36, v -> take.getValue()));
    private final BooleanSetting smart = add(new BooleanSetting("Smart", true, v -> take.getValue()).setParent());
    private final SliderSetting helmet = add(new SliderSetting("Helmet", 1, 0, 36, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting chestplate = add(new SliderSetting("ChestPlate", 1, 0, 36, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting leggings = add(new SliderSetting("Leggings", 1, 0, 36, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting boots = add(new SliderSetting("Boots", 1, 0, 36, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting elytra = add(new SliderSetting("Elytra", 1, 0, 36, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting sword = add(new SliderSetting("Sword", 1, 0, 36, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting pickaxe = add(new SliderSetting("Pickaxe", 1, 0, 36, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting crystal = add(new SliderSetting("Crystal", 256, 0, 512, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting exp = add(new SliderSetting("Exp", 256, 0, 512, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting totem = add(new SliderSetting("Totem", 6, 0, 36, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting turtleMaster = add(new SliderSetting("Turtle_Master", 6, 0, 36, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting gapple = add(new SliderSetting("Gapple", 128, 0, 512, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting cfruit = add(new SliderSetting("Cfruit", 64, 0, 512, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting endChest = add(new SliderSetting("EndChest", 64, 0, 512, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting web = add(new SliderSetting("Web", 64, 0, 512, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting glowstone = add(new SliderSetting("Glowstone", 256, 0, 512, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting anchor = add(new SliderSetting("Anchor", 256, 0, 512, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting piston = add(new SliderSetting("Piston", 64, 0, 512, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting redstone = add(new SliderSetting("RedStone", 64, 0, 512, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting pearl = add(new SliderSetting("Pearl", 16, 0, 64, v -> take.getValue() && smart.isOpen()));
    final int[] stealCountList = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    public AutoRegear() {
        super("AutoRegear", "Auto place shulker and replenish", Category.Combat);
    }

    public int findShulker() {
        final AtomicInteger atomicInteger = new AtomicInteger(-1);
        if (findClass(ShulkerBoxBlock.class) != -1) {
            atomicInteger.set(findClass(ShulkerBoxBlock.class));
        }
        return atomicInteger.get();
    }

    public int findClass(Class clazz) {
        if (inventory.getValue()) {
            return InventoryUtil.findClassInventorySlot(clazz);
        } else {
            return InventoryUtil.findClass(clazz);
        }
    }

    private final Timer timer = new Timer();
    BlockPos placePos = null;
    private final Timer disableTimer = new Timer();
    @Override
    public void onEnable() {
        openPos = null;
        disableTimer.reset();
        placePos = null;
        if (nullCheck()) {
            return;
        }
        int oldSlot = mc.player.getInventory().selectedSlot;
        if (!this.place.getValue()) {
            return;
        }
        double distance = 100;
        BlockPos bestPos = null;
        for (BlockPos pos : BlockUtil.getSphere((float) range.getValue())) {
            if (!BlockUtil.isAir(pos.up())) continue;
            if (preferOpen.getValue() && mc.world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock) return;
            if (MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.toCenterPos())) < minRange.getValue()) continue;
            if (!BlockUtil.clientCanPlace(pos, false)
                    || !BlockUtil.isStrictDirection(pos.offset(Direction.DOWN), Direction.UP)
                    || !BlockUtil.canClick(pos.offset(Direction.DOWN))
            ) continue;
            if (detectMining.getValue() && (Nullpoint.BREAK.isMining(pos) || pos.equals(SpeedMine.breakPos))) continue;
            if (bestPos == null || MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.toCenterPos())) < distance) {
                distance = MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.toCenterPos()));
                bestPos = pos;
            }
        }
        if (bestPos != null) {
            int slot = findShulker();
            if (slot == -1) {
                CommandManager.sendChatMessage("§c[!] No shulkerbox found");
                return;
            }
            doSwap(slot);
            placeBlock(bestPos);
            placePos = bestPos;
            if (inventory.getValue()) {
                doSwap(slot);
                EntityUtil.syncInventory();
            } else {
                doSwap(oldSlot);
            }
            timer.reset();
        } else {
            CommandManager.sendChatMessage("§c[!] No place pos found");
        }
    }

    private void doSwap(int slot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private void update() {
        this.stealCountList[0] = (int) (this.crystal.getValue() - InventoryUtil.getItemCount(Items.END_CRYSTAL));
        this.stealCountList[1] = (int) (this.exp.getValue() - InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE));
        this.stealCountList[2] = (int) (this.totem.getValue() - InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING));
        this.stealCountList[3] = (int) (this.gapple.getValue() - InventoryUtil.getItemCount(Items.ENCHANTED_GOLDEN_APPLE));
        this.stealCountList[4] = (int) (this.endChest.getValue() - InventoryUtil.getItemCount(Item.fromBlock(Blocks.ENDER_CHEST)));
        this.stealCountList[5] = (int) (this.web.getValue() - InventoryUtil.getItemCount(Item.fromBlock(Blocks.COBWEB)));
        this.stealCountList[6] = (int) (this.glowstone.getValue() - InventoryUtil.getItemCount(Item.fromBlock(Blocks.GLOWSTONE)));
        this.stealCountList[7] = (int) (this.anchor.getValue() - InventoryUtil.getItemCount(Item.fromBlock(Blocks.RESPAWN_ANCHOR)));
        this.stealCountList[8] = (int) (this.pearl.getValue() - InventoryUtil.getItemCount(Items.ENDER_PEARL));
        this.stealCountList[9] = (int) (this.turtleMaster.getValue() - InventoryUtil.getPotCount(StatusEffects.RESISTANCE));
        this.stealCountList[10] = (int) (this.helmet.getValue() - InventoryUtil.getArmorCount(ArmorItem.Type.HELMET));
        this.stealCountList[11] = (int) (this.chestplate.getValue() - InventoryUtil.getArmorCount(ArmorItem.Type.CHESTPLATE));
        this.stealCountList[12] = (int) (this.leggings.getValue() - InventoryUtil.getArmorCount(ArmorItem.Type.LEGGINGS));
        this.stealCountList[13] = (int) (this.boots.getValue() - InventoryUtil.getArmorCount(ArmorItem.Type.BOOTS));
        this.stealCountList[14] = (int) (this.elytra.getValue() - InventoryUtil.getItemCount(Items.ELYTRA));
        this.stealCountList[15] = (int) (this.sword.getValue() - InventoryUtil.getClassCount(SwordItem.class));
        this.stealCountList[16] = (int) (this.pickaxe.getValue() - InventoryUtil.getClassCount(PickaxeItem.class));
        this.stealCountList[17] = (int) (this.piston.getValue() - InventoryUtil.getClassCount(PistonBlock.class));
        this.stealCountList[18] = (int) (this.redstone.getValue() - InventoryUtil.getItemCount(Item.fromBlock(Blocks.REDSTONE_BLOCK)));
        this.stealCountList[19] = (int) (this.cfruit.getValue() - InventoryUtil.getItemCount(Items.CHORUS_FRUIT));
    }

    @Override
    public void onDisable() {
        opend = false;
        if (mine.getValue()) {
            if (placePos != null) {
                SpeedMine.INSTANCE.mine(placePos);
            }
        }
    }
    BlockPos openPos;

    boolean opend = false;
    @Override
    public void onUpdate() {
        if (smart.getValue()) update();
        if (!(mc.currentScreen instanceof ShulkerBoxScreen)) {
            if (opend) {
                opend = false;
                if (autoDisable.getValue()) disable2();
                if (mine.getValue()) {
                    if (openPos != null) {
                        if (mc.world.getBlockState(openPos).getBlock() instanceof ShulkerBoxBlock) {
                            SpeedMine.INSTANCE.mine(openPos);
                        } else {
                            openPos = null;
                        }
                    }
                }
                return;
            }
            if (open.getValue()) {
                if (placePos != null && MathHelper.sqrt((float) mc.player.squaredDistanceTo(placePos.toCenterPos())) <= range.getValue() && mc.world.isAir(placePos.up()) && (!timer.passedMs(500) || mc.world.getBlockState(placePos).getBlock() instanceof ShulkerBoxBlock)) {
                    if (mc.world.getBlockState(placePos).getBlock() instanceof ShulkerBoxBlock) {
                        openPos = placePos;
                        BlockUtil.clickBlock(placePos, BlockUtil.getClickSide(placePos), rotate.getValue());
                    }
                } else {
                    boolean found = false;
                    for (BlockPos pos : BlockUtil.getSphere((float) range.getValue())) {
                        if (!BlockUtil.isAir(pos.up())) continue;
                        if (mc.world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock) {
                            openPos = pos;
                            BlockUtil.clickBlock(pos, BlockUtil.getClickSide(pos), rotate.getValue());
                            found = true;
                            break;
                        }
                    }
                    if (!found && autoDisable.getValue()) this.disable2();
                }
            } else if (!this.take.getValue()) {
                if (autoDisable.getValue()) this.disable2();
            }
            return;
        }
        opend = true;
        if (!this.take.getValue()) {
            if (autoDisable.getValue()) this.disable2();
            return;
        }
        boolean take = false;
        if (mc.player.currentScreenHandler instanceof ShulkerBoxScreenHandler shulker) {
            for (Slot slot : shulker.slots) {
                if (slot.id < 27 && !slot.getStack().isEmpty() && (!smart.getValue() || needSteal(slot.getStack())) && InventoryUtil.getEmptySlotCount() > empty.getValue()) {
                    mc.interactionManager.clickSlot(shulker.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                    take = true;
                }
            }

        }
        if (autoDisable.getValue() && !take) this.disable2();
    }

    private void disable2() {
        if (disableTimer.passedMs(disableTime.getValueInt())){
            if(close.getValue()){
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                mc.player.closeHandledScreen();
            }
            disable();
        }
    }
    private boolean needSteal(final ItemStack i) {
        if (i.getItem().equals(Items.END_CRYSTAL) && this.stealCountList[0] > 0) {
            stealCountList[0] = stealCountList[0] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Items.EXPERIENCE_BOTTLE) && this.stealCountList[1] > 0) {
            stealCountList[1] = stealCountList[1] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Items.TOTEM_OF_UNDYING) && this.stealCountList[2] > 0) {
            stealCountList[2] = stealCountList[2] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE) && this.stealCountList[3] > 0) {
            stealCountList[3] = stealCountList[3] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Item.fromBlock(Blocks.ENDER_CHEST)) && this.stealCountList[4] > 0) {
            stealCountList[4] = stealCountList[4] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Item.fromBlock(Blocks.COBWEB)) && this.stealCountList[5] > 0) {
            stealCountList[5] = stealCountList[5] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Item.fromBlock(Blocks.GLOWSTONE)) && this.stealCountList[6] > 0) {
            stealCountList[6] = stealCountList[6] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Item.fromBlock(Blocks.RESPAWN_ANCHOR)) && this.stealCountList[7] > 0) {
            stealCountList[7] = stealCountList[7] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Items.ENDER_PEARL) && this.stealCountList[8] > 0) {
            stealCountList[8] = stealCountList[8] - i.getCount();
            return true;
        }
        if(i.getItem().equals(Items.SPLASH_POTION) && this.stealCountList[9] > 0){
            List<StatusEffectInstance> effects = new ArrayList<>(PotionUtil.getPotionEffects(i));
            for(StatusEffectInstance potionEffect :  effects){
                if(potionEffect.getEffectType() == StatusEffects.RESISTANCE){
                    stealCountList[9] = stealCountList[9] - i.getCount();
                    return true;
                }
            }
        }
        if (InventoryUtil.CheckArmorType(i.getItem(), ArmorItem.Type.HELMET) && this.stealCountList[10] > 0) {
            stealCountList[10] = stealCountList[10] - i.getCount();
            return true;
        }
        if (InventoryUtil.CheckArmorType(i.getItem(), ArmorItem.Type.CHESTPLATE) && this.stealCountList[11] > 0) {
            stealCountList[11] = stealCountList[11] - i.getCount();
            return true;
        }
        if (InventoryUtil.CheckArmorType(i.getItem(), ArmorItem.Type.LEGGINGS) && this.stealCountList[12] > 0) {
            stealCountList[12] = stealCountList[12] - i.getCount();
            return true;
        }
        if (InventoryUtil.CheckArmorType(i.getItem(), ArmorItem.Type.BOOTS) && this.stealCountList[13] > 0) {
            stealCountList[13] = stealCountList[13] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Items.ELYTRA) && this.stealCountList[14] > 0) {
            stealCountList[14] = stealCountList[14] - i.getCount();
            return true;
        }
        if (i.getItem() instanceof SwordItem && this.stealCountList[15] > 0) {
            stealCountList[15] = stealCountList[15] - i.getCount();
            return true;
        }
        if (i.getItem() instanceof PickaxeItem && this.stealCountList[16] > 0) {
            stealCountList[16] = stealCountList[16] - i.getCount();
            return true;
        }
        if (i.getItem() instanceof BlockItem && ((BlockItem)i.getItem()).getBlock() instanceof PistonBlock && this.stealCountList[17] > 0) {
            stealCountList[17] = stealCountList[17] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Item.fromBlock(Blocks.REDSTONE_BLOCK)) && this.stealCountList[18] > 0) {
            stealCountList[18] = stealCountList[18] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Items.CHORUS_FRUIT) && this.stealCountList[19] > 0) {
            stealCountList[19] = stealCountList[19] - i.getCount();
            return true;
        }
        return false;
    }
    private void placeBlock(BlockPos pos) {
        BlockUtil.clickBlock(pos.offset(Direction.DOWN), Direction.UP, rotate.getValue());
    }
}