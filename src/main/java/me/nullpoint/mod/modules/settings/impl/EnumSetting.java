package me.nullpoint.mod.modules.settings.impl;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.ModuleManager;
import me.nullpoint.mod.modules.settings.EnumConverter;
import me.nullpoint.mod.modules.settings.Setting;

import java.util.function.Predicate;

public class EnumSetting<T extends Enum<T>> extends Setting {
    private T value;
    public boolean popped = false;
    public EnumSetting(String name, T defaultValue) {
        super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
        value = defaultValue;
    }

    public EnumSetting(String name, T defaultValue, Predicate visibilityIn) {
        super(name, ModuleManager.lastLoadMod.getName() + "_" + name, visibilityIn);
        value = defaultValue;
    }

    public void increaseEnum() {
        value = (T) EnumConverter.increaseEnum(value);
    }

    public final T getValue() {
        return this.value;
    }
    public void setEnumValue(String value) {
        for (Enum e : this.value.getClass().getEnumConstants()) {
            if (!e.name().equalsIgnoreCase(value)) continue;
            this.value = (T) e;
        }
    }
    @Override
    public void loadSetting() {
        EnumConverter converter = new EnumConverter(value.getClass());
        String enumString = Nullpoint.CONFIG.getString(this.getLine());
        if (enumString == null) {
            return;
        }
        Enum value = converter.doBackward(enumString);
        if (value != null) {
            this.value = (T) value;
        }
    }
}