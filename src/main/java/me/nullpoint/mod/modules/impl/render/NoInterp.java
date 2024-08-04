package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.mod.modules.Beta;
import me.nullpoint.mod.modules.Module;

@Beta
public class NoInterp extends Module {
    public static NoInterp INSTANCE;
    public NoInterp() {
        super("NoInterp", Category.Render);
        INSTANCE = this;
    }
}
