package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;

public class AutoPearl extends Module {
    public static AutoPearl INSTANCE;

    public AutoPearl() {
        super("AutoPearl", Category.Misc);
        INSTANCE = this;
    }

    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true));
    boolean shouldThrow = false;

    @Override
    public void onEnable() {
        if (nullCheck()) {
            disable();
            return;
        }
        if (getBind().isHoldEnable()) {
            shouldThrow = true;
            return;
        }
        throwPearl(mc.player.getYaw(), mc.player.getPitch());
        disable();
    }

    public static boolean throwing = false;

    public void throwPearl(float yaw, float pitch) {
        throwing = true;
        int pearl;
        if (mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
            EntityUtil.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround()));
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
        } else if (inventory.getValue() && (pearl = InventoryUtil.findItemInventorySlot(Items.ENDER_PEARL)) != -1) {
            InventoryUtil.inventorySwap(pearl, mc.player.getInventory().selectedSlot);
            EntityUtil.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround()));
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
            InventoryUtil.inventorySwap(pearl, mc.player.getInventory().selectedSlot);
            EntityUtil.syncInventory();
        } else if ((pearl = InventoryUtil.findItem(Items.ENDER_PEARL)) != -1) {
            int old = mc.player.getInventory().selectedSlot;
            InventoryUtil.switchToSlot(pearl);
            EntityUtil.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround()));
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
            InventoryUtil.switchToSlot(old);
        }
        throwing = false;
    }

    @Override
    public void onDisable() {
        if (nullCheck()) {
            return;
        }
        if (shouldThrow && getBind().isHoldEnable()) {
            shouldThrow = false;
            throwPearl(mc.player.getYaw(), mc.player.getPitch());
        }
    }
}