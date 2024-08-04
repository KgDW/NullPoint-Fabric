package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import me.nullpoint.api.utils.math.Timer;

public class Replenish extends Module {
    public Replenish() {
        super("Replenish", Category.Player);
    }

    private final SliderSetting delay = add(new SliderSetting("Delay", 0.5, 0, 4, 0.01));
    private final SliderSetting min = add(new SliderSetting("Min", 16, 1, 64));
    private final Timer timer = new Timer();


    @Override
    public void onUpdate() {
        if (mc.currentScreen != null && !(mc.currentScreen instanceof ClickGuiScreen)) return;
        if (!timer.passedMs((long) (delay.getValue() * 1000))) return;
        for (int i = 0; i < 9; ++i) {
            if (replenish(i)) {
                timer.reset();
                return;
            }
        }
    }

    private boolean replenish(int slot) {
        ItemStack stack = mc.player.getInventory().getStack(slot);

        if (stack.isEmpty()) return false;
        if (!stack.isStackable()) return false;
        if (stack.getCount() >= min.getValue()) return false;
        if (stack.getCount() == stack.getMaxCount()) return false;

        for (int i = 9; i < 36; ++i) {
            ItemStack item = mc.player.getInventory().getStack(i);
            if (item.isEmpty() || !canMerge(stack, item)) continue;
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            return true;
        }
        return false;
    }

    private boolean canMerge(ItemStack source, ItemStack stack) {
        return source.getItem() == stack.getItem() && source.getName().equals(stack.getName());
    }
}
