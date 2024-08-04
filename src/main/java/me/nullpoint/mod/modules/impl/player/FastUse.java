package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class FastUse extends Module {
    private final SliderSetting delay = add(new SliderSetting("Delay", 0, 0, 4, 1));
    public FastUse() {
        super("FastUse", Category.Player);
    }

    @Override
    public void onUpdate() {
        if (mc.itemUseCooldown <= 4 - delay.getValueInt()) {
            mc.itemUseCooldown = 0;
        }
    }
}
