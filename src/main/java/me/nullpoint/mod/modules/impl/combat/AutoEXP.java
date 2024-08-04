package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.eventbus.EventPriority;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;


public class AutoEXP extends Module {


    public static AutoEXP INSTANCE;
    private final SliderSetting delay =
            add(new SliderSetting("Delay", 3, 0, 5));
    public final BooleanSetting down =
            add(new BooleanSetting("Down", true));
    public final BooleanSetting onlyBroken =
            add(new BooleanSetting("OnlyBroken", true));
    private final BooleanSetting usingPause =
            add(new BooleanSetting("UsingPause", true));
    private final BooleanSetting onlyGround =
            add(new BooleanSetting("OnlyGround", true));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true));
    private final Timer delayTimer = new Timer();

    public AutoEXP() {
        super("AutoEXP", Category.Combat);
        INSTANCE = this;
    }

    private boolean throwing = false;

    @Override
    public void onDisable() {
        throwing = false;
    }

    int exp = 0;
    @Override
    public void onUpdate() {
        if (!getBind().isPressed()) {
            disable();
            return;
        }
        throwing = checkThrow();
        if (isThrow() && delayTimer.passedMs(delay.getValueInt() * 20L) && (!onlyGround.getValue() || mc.player.isOnGround())) {
            exp = InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE) - 1;
            throwExp();
        }
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            disable();
            return;
        }
        exp = InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE);
    }

    @Override
    public String getInfo() {
        return String.valueOf(exp);
    }

    public void throwExp() {
        int oldSlot = mc.player.getInventory().selectedSlot;
        int newSlot;
        if (inventory.getValue() && (newSlot = InventoryUtil.findItemInventorySlot(Items.EXPERIENCE_BOTTLE)) != -1) {
            InventoryUtil.inventorySwap(newSlot, mc.player.getInventory().selectedSlot);
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, EntityUtil.getWorldActionId(mc.world)));
            InventoryUtil.inventorySwap(newSlot, mc.player.getInventory().selectedSlot);
            EntityUtil.syncInventory();
            delayTimer.reset();
        } else if ((newSlot = InventoryUtil.findItem(Items.EXPERIENCE_BOTTLE)) != -1) {
            InventoryUtil.switchToSlot(newSlot);
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, EntityUtil.getWorldActionId(mc.world)));
            InventoryUtil.switchToSlot(oldSlot);
            delayTimer.reset();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void RotateEvent(RotateEvent event) {
        if (!down.getValue()) return;
        if (isThrow()) event.setPitch(88);
    }

    public boolean isThrow() {
        return throwing;
    }

    public boolean checkThrow() {
        if (isOff()) return false;
        if (mc.currentScreen instanceof ChatScreen) return false;
        if (mc.currentScreen != null) return false;
        if (usingPause.getValue() && mc.player.isUsingItem()) {
            return false;
        }
        if (InventoryUtil.findItem(Items.EXPERIENCE_BOTTLE) == -1 && (!inventory.getValue() || InventoryUtil.findItemInventorySlot(Items.EXPERIENCE_BOTTLE) == -1))
            return false;
        if (onlyBroken.getValue()) {
            DefaultedList<ItemStack> armors = mc.player.getInventory().armor;
            for (ItemStack armor : armors) {
                if (armor.isEmpty()) continue;
                if (EntityUtil.getDamagePercent(armor) >= 100) continue;
                return true;
            }
        } else {
            return true;
        }
        return false;
    }
}
