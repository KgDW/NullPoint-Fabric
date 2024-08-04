/**
 * An setting that holds a variable of true or false.
 */
package me.nullpoint.mod.modules.settings.impl;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.ModuleManager;
import me.nullpoint.mod.modules.settings.Setting;

import java.util.function.Predicate;

public class BooleanSetting extends Setting {
	public boolean parent = false;
	public boolean popped = false;
	private boolean value;

	public BooleanSetting(String name, boolean defaultValue) {
		super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
		this.value = defaultValue;
	}

	public BooleanSetting(String name, boolean defaultValue, Predicate visibilityIn) {
		super(name, ModuleManager.lastLoadMod.getName() + "_" + name, visibilityIn);
		this.value = defaultValue;
	}

	public final boolean getValue() {
		return this.value;
	}
	
	public final void setValue(boolean value) {
		this.value = value;
	}
	
	public final void toggleValue() {
		this.value = !value;
	}
	public final boolean isOpen() {
		if (parent) {
			return popped;
		} else {
			return true;
		}
	}
	@Override
	public void loadSetting() {
		this.value = Nullpoint.CONFIG.getBoolean(this.getLine(), value);
	}

	public BooleanSetting setParent() {
		parent = true;
		return this;
	}
}
