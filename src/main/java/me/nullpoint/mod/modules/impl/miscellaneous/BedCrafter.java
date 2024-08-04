package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Beta;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.BedItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@Beta
public class BedCrafter extends Module {
    public static BedCrafter INSTANCE;
    public BedCrafter() {
        super("BedCrafter", Category.Misc);
        INSTANCE = this;
    }
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", false));
    private final SliderSetting range = add(new SliderSetting("Range", 5, 0, 8));
    private final SliderSetting beds =
            add(new SliderSetting("Beds", 5, 1, 30));
    private final BooleanSetting disable =
            add(new BooleanSetting("Disable", true));

    boolean open = false;

    @Override
    public void onDisable() {
        open = false;
    }

    @Override
    public void onUpdate() {
        if (getEmptySlots() == 0) {
            if (mc.player.currentScreenHandler instanceof CraftingScreenHandler) {
                mc.player.closeHandledScreen();
            }
            if (disable.getValue()) disable();
            return;
        }
        if (mc.player.currentScreenHandler instanceof CraftingScreenHandler) {
            open = true;
            boolean craft = false;
            for (RecipeResultCollection recipeResult : mc.player.getRecipeBook().getOrderedResults()) {
                for (RecipeEntry<?> recipe : recipeResult.getRecipes(true)) {
                    if (recipe.value().getResult(mc.world.getRegistryManager()).getItem() instanceof BedItem) {
                        int bed = 0;
                        for (int i = 0; i < getEmptySlots(); ++i) {
                            craft = true;
                            if (bed >= beds.getValueInt()) {
                                break;
                            }
                            bed++;
                            mc.interactionManager.clickRecipe(mc.player.currentScreenHandler.syncId, recipe, false);
                            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 0, 1, SlotActionType.QUICK_MOVE, mc.player);
                        }
                        break;
                    }
                }
            }
            if (!craft) {
                if (mc.player.currentScreenHandler instanceof CraftingScreenHandler) {
                    mc.player.closeHandledScreen();
                }
                if (disable.getValue()) disable();
            }
        } else {
            if (disable.getValue() && open) {
                disable();
                return;
            }
            doPlace();
        }
    }
    private void doPlace() {
        BlockPos bestPos = null;
        double distance = 100;
        boolean place = true;
        for (BlockPos pos : BlockUtil.getSphere(range.getValueFloat())) {
            if (mc.world.getBlockState(pos).getBlock() == Blocks.CRAFTING_TABLE && BlockUtil.getClickSideStrict(pos) != null) {
                place = false;
                bestPos = pos;
                break;
            }
            if (BlockUtil.canPlace(pos)) {
                if (bestPos == null || MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.toCenterPos())) < distance) {
                    bestPos = pos;
                    distance = MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.toCenterPos()));
                }
            }
        }

        if (bestPos != null) {
            if (!place) {
                BlockUtil.clickBlock(bestPos, BlockUtil.getClickSide(bestPos), rotate.getValue());
            } else {
                if (InventoryUtil.findItem(Item.fromBlock(Blocks.CRAFTING_TABLE)) == -1) return;
                int old = mc.player.getInventory().selectedSlot;
                InventoryUtil.switchToSlot(InventoryUtil.findItem(Item.fromBlock(Blocks.CRAFTING_TABLE)));
                BlockUtil.placeBlock(bestPos, rotate.getValue());
                InventoryUtil.switchToSlot(old);
            }
        }
    }

    public static int getEmptySlots() {
        int emptySlots = 0;
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (itemStack != null && !(itemStack.getItem() instanceof AirBlockItem)) continue;
            ++emptySlots;
        }
        return emptySlots;
    }
}
