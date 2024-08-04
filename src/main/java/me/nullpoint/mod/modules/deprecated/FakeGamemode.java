package me.nullpoint.mod.modules.deprecated;

import me.nullpoint.mod.modules.Module;
import net.minecraft.world.GameMode;

@Deprecated
public class FakeGamemode extends Module {
    public FakeGamemode() {
        super("FakeGamemode", Category.Player);
    }

    boolean set = false;
    @Override
    public void onEnable() {
        if (nullCheck()) {
            return;
        }
        if (mc.interactionManager.getCurrentGameMode() == GameMode.CREATIVE) {
            return;
        }
        set = true;
        mc.interactionManager.setGameMode(GameMode.CREATIVE);
    }

    @Override
    public void onDisable() {
        if (set) {
            set = false;
            if (mc.interactionManager.getCurrentGameMode() == GameMode.CREATIVE) {
                mc.interactionManager.setGameMode(mc.interactionManager.getPreviousGameMode());
            }
        }
    }
}
