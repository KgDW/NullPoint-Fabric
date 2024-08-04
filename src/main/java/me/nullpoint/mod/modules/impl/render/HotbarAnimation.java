package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class HotbarAnimation extends Module {
    public static HotbarAnimation INSTANCE;
    public HotbarAnimation() {
        super("HotbarAnimation", Category.Render);
        INSTANCE = this;
    }
    public final EnumSetting<AnimateUtil.AnimMode> animMode = add(new EnumSetting<>("AnimMode", AnimateUtil.AnimMode.Mio));
    public final SliderSetting hotbarSpeed = add(new SliderSetting("HotbarSpeed", 0.2, 0.01, 1, 0.01));
}
