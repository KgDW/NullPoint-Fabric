/**
 * An setting that holds a variable of true or false.
 */
package me.nullpoint.mod.modules.settings.impl;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.ModuleManager;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.settings.Setting;

import java.awt.*;
import java.util.function.Predicate;

public class ColorSetting extends Setting {
	public boolean isRainbow = false;
	private Color value;
	public final Timer timer = new Timer();
	public boolean injectBoolean = false;
	public boolean booleanValue = false;
	public final float effectSpeed = 4;
	public ColorSetting(String name, Color defaultValue) {
		super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
		this.value = defaultValue;
	}

	public ColorSetting(String name, Color defaultValue, Predicate visibilityIn) {
		super(name, ModuleManager.lastLoadMod.getName() + "_" + name, visibilityIn);
		this.value = defaultValue;
	}

	public ColorSetting(String name, int defaultValue) {
		this(name, new Color(defaultValue));
	}

	public ColorSetting(String name, int defaultValue, Predicate visibilityIn) {
		this(name, new Color(defaultValue), visibilityIn);
	}

	public final Color getValue() {
		if (isRainbow) {
			float[] HSB = Color.RGBtoHSB(value.getRed(), value.getGreen(), value.getBlue(), null);
			Color preColor = Color.getHSBColor(((float) timer.getPassedTimeMs() * 0.36f * effectSpeed / 20f) % 361 / 360, HSB[1], HSB[2]);
			setValue(new Color(preColor.getRed(), preColor.getGreen(), preColor.getBlue(), value.getAlpha()));
		}
		return this.value;
	}
	
	public final void setValue(Color value) {
		this.value = value;
	}

	public final void setValue(int value) {
		this.value = new Color(value, true);
	}
	public final void setRainbow(boolean rainbow) {
		this.isRainbow = rainbow;
	}
	public ColorSetting injectBoolean(boolean value) {
		injectBoolean = true;
		booleanValue = value;
		return this;
	}
	@Override
	public void loadSetting() {
		this.value = new Color(Nullpoint.CONFIG.getInt(this.getLine(), value.getRGB()), true);
		this.isRainbow = Nullpoint.CONFIG.getBoolean(this.getLine() + "Rainbow");
		if (injectBoolean) {
			this.booleanValue = Nullpoint.CONFIG.getBoolean(this.getLine() + "Boolean", booleanValue);
		}
	}
}
