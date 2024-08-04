/**
 * Anti-Invis Module
 */
package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.asm.accessors.IUpdateSelectedSlotS2CPacket;
import me.nullpoint.mod.modules.Module;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;

public class NoSwap extends Module {
	public static NoSwap INSTANCE;
	public NoSwap() {
		super("NoSwap", Category.Player);
		INSTANCE = this;
	}

	@EventHandler
	public void onPacketReceive(PacketEvent.Receive event){
		if(nullCheck()) return;
		if(event.getPacket() instanceof UpdateSelectedSlotS2CPacket packet){
			int slot = mc.player.getInventory().selectedSlot;
			if(packet.getSlot() != slot){
				((IUpdateSelectedSlotS2CPacket)packet).setslot(slot);
				InventoryUtil.switchToSlot(slot);
				EntityUtil.syncInventory();
			}
		}
	}
}