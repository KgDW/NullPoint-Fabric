package me.nullpoint.mod.gui.clickgui;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.mod.gui.clickgui.particle.Snow;
import me.nullpoint.mod.gui.clickgui.tabs.Tab;
import me.nullpoint.mod.modules.impl.client.UIModule;
import me.nullpoint.mod.modules.settings.impl.StringSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Random;

public class ClickGuiScreen extends Screen implements Wrapper {

    public ClickGuiScreen() {
        super(Text.of("ClickGui"));
    }
    private final ArrayList<Snow> snow = new ArrayList<>();
    public static boolean clicked = false;
    public static boolean rightClicked = false;
    public static boolean hoverClicked = false;
    public static boolean MOUSE_BUTTON_4 = false;
    public static boolean MOUSE_BUTTON_5 = false;

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
        super.render(drawContext, mouseX, mouseY, partialTicks);
        if (UIModule.INSTANCE.snow.getValue()) this.snow.forEach(snow -> snow.drawSnow(drawContext));
        Nullpoint.GUI.draw(mouseX, mouseY, drawContext, partialTicks);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Nullpoint.MODULE.modules.forEach(module -> {
            module.getSettings().stream()
                    .filter(setting -> setting instanceof StringSetting)
                    .map(setting -> (StringSetting) setting)
                    .filter(StringSetting::isListening)
                    .forEach(setting -> setting.keyType(keyCode));
        });
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            hoverClicked = false;
            clicked = true;
        } else if (button == 1) {
            rightClicked = true;
        } else  if(button == 3){
            MOUSE_BUTTON_4 = true;
        } else  if(button == 4){
            MOUSE_BUTTON_5 = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            clicked = false;
            hoverClicked = false;
        } else if (button == 1) {
            rightClicked = false;
        } else  if(button == 3){
            MOUSE_BUTTON_4 = false;
        } else  if(button == 4){
            MOUSE_BUTTON_5 = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        super.close();
        rightClicked = false;
        hoverClicked = false;
        MOUSE_BUTTON_4 = false;
        MOUSE_BUTTON_5 = false;
        clicked = false;
    }

    @Override
    public void onDisplayed() {
        super.onDisplayed();
        snow.clear();
        Random random = new Random();
        for (int i = 0; i < 100; ++i) {
            for (int y = 0; y < 3; ++y) {
                Snow snow = new Snow(25 * i, y * -50, random.nextInt(3) + 1, random.nextInt(2) + 1);
                this.snow.add(snow);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (Tab tab : Nullpoint.GUI.tabs) {
            tab.setY((int) (tab.getY() + (verticalAmount * 30)));
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
