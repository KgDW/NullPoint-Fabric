package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.mod.modules.Beta;
import me.nullpoint.mod.modules.Module;

@Beta
public class SilentDisconnect extends Module {
    public static SilentDisconnect INSTANCE;

    public SilentDisconnect() {
        super("SilentDisconnect", Category.Misc);
        INSTANCE = this;
    }
}
