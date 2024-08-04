package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

import static me.nullpoint.api.utils.entity.InventoryUtil.findItemInventorySlot;

public class MCP extends Module {
	public static MCP INSTANCE;

	public MCP() {
		super("MCP", Category.Misc);
		INSTANCE = this;
	}
	private final BooleanSetting inventory =
			add(new BooleanSetting("InventorySwap", true));
	boolean click = false;

	@Override
	public void onUpdate() {
		if (nullCheck()) return;
		if (mc.mouse.wasMiddleButtonClicked()) {
			if (!click) {
				int pearl;
				if (mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
					EntityUtil.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
					mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
				}
				else if ((pearl = findItem(Items.ENDER_PEARL)) != -1) {
					int old = mc.player.getInventory().selectedSlot;
					doSwap(pearl);
					EntityUtil.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
					mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
					if (inventory.getValue()) {
						doSwap(pearl);
						EntityUtil.syncInventory();
					} else {
						doSwap(old);
					}

				}
				click = true;
			}
		} else click = false;
	}

	private void doSwap(int slot) {
		if (inventory.getValue()) {
			InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
		} else {
			InventoryUtil.switchToSlot(slot);
		}
	}

	public int findItem(Item item) {
		if (inventory.getValue()) {
			return InventoryUtil.findItemInventorySlot(item);
		} else {
			return InventoryUtil.findItem(item);
		}
	}
}