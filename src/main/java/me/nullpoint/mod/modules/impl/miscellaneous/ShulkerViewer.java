package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.mod.modules.Module;

public class ShulkerViewer extends Module {
	public static ShulkerViewer INSTANCE;
	public ShulkerViewer() {
		super("ShulkerViewer", Category.Misc);
		INSTANCE = this;
	}
}