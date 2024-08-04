package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.mod.modules.Module;
import net.minecraft.client.gui.screen.DeathScreen;

public class AutoRespawn extends Module{
	
	public AutoRespawn() {
		super("AutoRespawn", Category.Player);
		this.setDescription("Automatically respawns when you die.");
	}

	@Override
	public void onUpdate() {
		if (mc.currentScreen instanceof DeathScreen) {
			mc.player.requestRespawn();
			mc.setScreen(null);
		}
	}
}
