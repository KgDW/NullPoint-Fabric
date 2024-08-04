package me.nullpoint.mod.modules.settings.impl;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.ModuleManager;
import me.nullpoint.mod.modules.settings.Setting;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

public class BindSetting extends Setting {
    private boolean isListening = false;
    private int key;
    private boolean pressed = false;
    private boolean holdEnable = false;
    public boolean hold = false;
    public BindSetting(String name, int key) {
        super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
        this.key = key;
    }

    @Override
    public void loadSetting() {
        setKey(Nullpoint.CONFIG.getInt(getLine(), key));
        setHoldEnable(Nullpoint.CONFIG.getBoolean(getLine() + "_hold"));
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getBind() {
        if (key == -1) return "None";
        String kn;

        if (key >= 3 && key <= 4) {
            switch (key) {
                case 3:
                    kn = "Mouse_4";
                    break;
                case 4:
                    kn = "Mouse_5";
                    break;
                default:
                    kn = "None";
                    break;
            }
        } else {
            kn = this.key > 0 ? GLFW.glfwGetKeyName(this.key, GLFW.glfwGetKeyScancode(this.key)) : "None";
        }
        if (kn == null) {
            try {
                for (Field declaredField : GLFW.class.getDeclaredFields()) {
                    if (declaredField.getName().startsWith("GLFW_KEY_")) {
                        int a = (int) declaredField.get(null);
                        if (a == this.key) {
                            String nb = declaredField.getName().substring("GLFW_KEY_".length());
                            kn = nb.substring(0, 1).toUpperCase() + nb.substring(1).toLowerCase();
                        }
                    }
                }
            } catch (Exception ignored) {
                kn = "None";
            }
        }

        return (kn + "").toUpperCase();
    }

    public void setListening(boolean set) {
        isListening = set;
    }

    public boolean isListening() {
        return isListening;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setHoldEnable(boolean holdEnable) {
        this.holdEnable = holdEnable;
    }

    public boolean isHoldEnable() {
        return holdEnable;
    }
}
