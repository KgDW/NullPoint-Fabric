package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.mod.modules.Module;
import net.minecraft.util.Util;

import java.io.IOException;

public class AutoPorn extends Module {
    public AutoPorn(){
        super("AutoPorn", Category.Misc);
    }



@Override
    public void onEnable() throws IOException {
    Util.getOperatingSystem().open("https://x10liumr8kvzin.com:58008/");
    }

}

