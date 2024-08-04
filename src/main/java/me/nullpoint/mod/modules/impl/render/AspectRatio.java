package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class AspectRatio extends Module {
    public static AspectRatio INSTANCE;

    public final SliderSetting ratio =
            add(new SliderSetting("Ratio", 1.78, 0.0, 5.0, 0.01));
    public AspectRatio() {
        super("AspectRatio", Category.Render);
        INSTANCE = this;
    }
}
