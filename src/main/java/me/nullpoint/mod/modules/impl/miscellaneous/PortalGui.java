package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.mod.modules.Module;

public class PortalGui extends Module {
    public static PortalGui INSTANCE;

    public PortalGui() {
        super("PortalGui", Category.Misc);
        INSTANCE = this;
    }
}
