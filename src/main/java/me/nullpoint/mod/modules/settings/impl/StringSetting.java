package me.nullpoint.mod.modules.settings.impl;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.ModuleManager;
import me.nullpoint.mod.modules.settings.Setting;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.SelectionManager;
import org.lwjgl.glfw.GLFW;

import java.util.function.Predicate;

import static me.nullpoint.api.utils.Wrapper.mc;
public class StringSetting extends Setting {
    public static StringSetting current;
    private boolean isListening = false;
    private String text;
    public StringSetting(String name, String text) {
        super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
        this.text = text;
    }

    public StringSetting(String name, String text, Predicate visibilityIn) {
        super(name, ModuleManager.lastLoadMod.getName() + "_" + name, visibilityIn);
        this.text = text;
    }
    @Override
    public void loadSetting() {
        setValue(Nullpoint.CONFIG.getString(getLine(), text));
    }

    public String getValue() {
        return this.text;
    }

    public void setValue(String text) {
        this.text = text;
    }
    public void setListening(boolean set) {
        isListening = set;
        if (isListening) {
            current = this;
        }
    }

    public boolean isListening() {
        return isListening && current == this;
    }

    public void keyType(int keyCode) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_V -> {
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
                    setValue(getValue() + SelectionManager.getClipboard(mc));
                }
                return;
            }
            case GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                setListening(false);
                return;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                setValue(removeLastChar(getValue()));
                return;
            }
        }
        //if(GLFW.glfwGetKeyName(keyCode, 0) == null) return;
        //setValue(getValue() + GLFW.glfwGetKeyName(keyCode, 0));
    }

    public void charType(char c) {
        setValue(getValue() + c);
    }
    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && !str.isEmpty()) {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

}
