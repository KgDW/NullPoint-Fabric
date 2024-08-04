package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class MCF extends Module {
	public static MCF INSTANCE;

	public MCF() {
		super("MCF", Category.Misc);
		INSTANCE = this;
	}
	boolean click = false;

	@Override
	public void onUpdate() {
		if (nullCheck()) return;
		if (mc.mouse.wasMiddleButtonClicked()) {
			if (!click) {
				onClick();
			}
			click = true;
		}
		else click = false;
	}

	private void onClick() {
		HitResult result = mc.crosshairTarget;

		if (result != null && result.getType() == HitResult.Type.ENTITY && result instanceof EntityHitResult entityHitResult) {
			if(entityHitResult.getEntity() instanceof PlayerEntity entity){
				if (Nullpoint.FRIEND.isFriend(entity.getName().getString())) {
					Nullpoint.FRIEND.removeFriend(entity.getName().getString());
					CommandManager.sendChatMessage(Formatting.RED + entity.getName().getString() + Formatting.RED + " has been unfriended.");

				} else {
					Nullpoint.FRIEND.addFriend(entity.getName().getString());
					CommandManager.sendChatMessage(Formatting.AQUA + entity.getName().getString() + Formatting.GREEN + " has been friended.");
				}
			}

		}
		click = true;
	}
}