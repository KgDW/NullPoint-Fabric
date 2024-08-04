package me.nullpoint.api.utils.entity;

import me.nullpoint.api.utils.Wrapper;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;

public class PearlUtil implements Wrapper {

    public static void doPearl(float yaw, float pitch, boolean inv){
        int pearl;
        if (mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
            EntityUtil.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
        }
        else if ((pearl = findItem(Items.ENDER_PEARL, inv)) != -1) {
            int old = mc.player.getInventory().selectedSlot;
            doSwap(pearl, inv);
            EntityUtil.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
            if (inv) {
                doSwap(pearl, true);
                EntityUtil.syncInventory();
            } else {
                doSwap(old, false);
            }

        }
    }

    private static void doSwap(int slot, boolean inv) {
        if (inv) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    public static int findItem(Item item, boolean inv) {
        if (inv) {
            return InventoryUtil.findItemInventorySlot(item);
        } else {
            return InventoryUtil.findItem(item);
        }
    }
}
