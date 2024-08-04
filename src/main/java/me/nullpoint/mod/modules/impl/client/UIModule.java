package me.nullpoint.mod.modules.impl.client;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.GuiManager;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.mod.gui.clickgui.components.Component;
import me.nullpoint.mod.gui.clickgui.components.impl.BooleanComponent;
import me.nullpoint.mod.gui.clickgui.components.impl.ColorComponents;
import me.nullpoint.mod.gui.clickgui.components.impl.ModuleComponent;
import me.nullpoint.mod.gui.clickgui.components.impl.SliderComponent;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.gui.clickgui.tabs.ClickGuiTab;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.InputStream;

public class UIModule extends Module {
	public static UIModule INSTANCE;
	private final EnumSetting<Pages> page = add(new EnumSetting<>("Page", Pages.General));
	public final EnumSetting<AnimateUtil.AnimMode> animMode = add(new EnumSetting<>("AnimMode", AnimateUtil.AnimMode.Mio, v -> page.getValue() == Pages.General));
	public final EnumSetting<Menu> mainMenu = add(new EnumSetting<>("MainMenu", Menu.Isolation, v -> page.getValue() == Pages.General));
	public final SliderSetting height = add(new SliderSetting("Height", 16, 10, 20, 1, v -> page.getValue() == Pages.General));
	public final ColorSetting bindC = add(new ColorSetting("BindText", new Color(255, 255, 255), v -> page.getValue() == Pages.General).injectBoolean(true));
	public final ColorSetting gearColor = add(new ColorSetting("Gear", new Color(150, 150, 150), v -> page.getValue() == Pages.General).injectBoolean(true));
	public final EnumSetting<Mode> mode = add(new EnumSetting<>("EnableAnim", Mode.Reset, v -> page.getValue() == Pages.General));
	public final BooleanSetting scissor = add(new BooleanSetting("Scissor", true, v -> page.getValue() == Pages.General));
	public final BooleanSetting snow = add(new BooleanSetting("Snow", false, v -> page.getValue() == Pages.General));
	public final SliderSetting animationSpeed = add(new SliderSetting("AnimationSpeed", 0.2, 0.01, 1, 0.01, v -> page.getValue() == Pages.General));
	public final SliderSetting sliderSpeed = add(new SliderSetting("SliderSpeed", 0.2, 0.01, 1, 0.01, v -> page.getValue() == Pages.General));
	public final SliderSetting booleanSpeed = add(new SliderSetting("BooleanSpeed", 0.2, 0.01, 1, 0.01, v -> page.getValue() == Pages.General));
	public final BooleanSetting customFont = add(new BooleanSetting("CustomFont", false, v -> page.getValue() == Pages.General));
	public final ColorSetting color = add(new ColorSetting("Main", new Color(140, 146, 255), v -> page.getValue() == Pages.Color));
	public final ColorSetting mainHover = add(new ColorSetting("MainHover", new Color(186, 188, 252), v -> page.getValue() == Pages.Color));
	public final ColorSetting categoryEnd = add(new ColorSetting("CategoryEnd", -2113929216, v -> page.getValue() == Pages.Color).injectBoolean(true));
	public final ColorSetting disableText = add(new ColorSetting("DisableText", new Color(255, 255, 255), v -> page.getValue() == Pages.Color));
	public final ColorSetting enableText = add(new ColorSetting("EnableText", new Color(130, 135, 255), v -> page.getValue() == Pages.Color));
	public final ColorSetting mbgColor = add(new ColorSetting("Module", new Color(63, 63, 63, 42), v -> page.getValue() == Pages.Color));
	public final ColorSetting moduleEnd = add(new ColorSetting("ModuleEnd", -2113929216, v -> page.getValue() == Pages.Color).injectBoolean(true));
	public final ColorSetting moduleEnable = add(new ColorSetting("ModuleEnable", new Color(170, 182, 255), v -> page.getValue() == Pages.Color));
	public final ColorSetting mhColor = add(new ColorSetting("ModuleHover", new Color(152, 152, 152, 123), v -> page.getValue() == Pages.Color));
	public final ColorSetting sbgColor = add(new ColorSetting("Setting", new Color(24, 24, 24, 0), v -> page.getValue() == Pages.Color));
	public final ColorSetting shColor = add(new ColorSetting("SettingHover", new Color(152, 152, 152, 123), v -> page.getValue() == Pages.Color));
	public final ColorSetting bgColor = add(new ColorSetting("Background", new Color(24, 24, 24, 42), v -> page.getValue() == Pages.Color));
	public UIModule() {
		super("UI", Category.Client);
		INSTANCE = this;
	}

	public static final FadeUtils fade = new FadeUtils(300);

	@Override
	public void onUpdate() {
		if (!(mc.currentScreen instanceof ClickGuiScreen)) {
			disable();
		}
	}

	int lastHeight;
	@Override
	public void onEnable() {

		if (lastHeight != height.getValueInt()) {
			for (ClickGuiTab tab : Nullpoint.GUI.tabs) {
				for (Component component : tab.getChildren()) {
					if (component instanceof ModuleComponent moduleComponent) {
						for (Component settingComponent : moduleComponent.getSettingsList()) {
							settingComponent.setHeight(height.getValueInt());
							settingComponent.defaultHeight = height.getValueInt();
						}
					}
					component.setHeight(height.getValueInt());
					component.defaultHeight = height.getValueInt();
				}
			}
			lastHeight = height.getValueInt();
		}
		if (mode.getValue() == Mode.Reset) {
			for (ClickGuiTab tab : Nullpoint.GUI.tabs) {
				for (Component component : tab.getChildren()) {
					component.currentOffset = 0;
					if (component instanceof ModuleComponent moduleComponent) {
						moduleComponent.isPopped = false;
						for (Component settingComponent : moduleComponent.getSettingsList()) {
							settingComponent.currentOffset = 0;
							if (settingComponent instanceof SliderComponent sliderComponent) {
								sliderComponent.renderSliderPosition = 0;
							} else if (settingComponent instanceof BooleanComponent booleanComponent) {
								booleanComponent.currentWidth = 0;
							} else if (settingComponent instanceof ColorComponents colorComponents) {
								colorComponents.currentWidth = 0;
							}
						}
					}
				}
				tab.currentHeight = 0;
			}
		}
		Notify.notifyList.clear();
		sendNotify("This is a exmaple notify");
		fade.reset();
		if (nullCheck()) {
			disable();
			return;
		}
		mc.setScreen(GuiManager.clickGui);
	}



	@Override
	public void onDisable() {
		if (mc.currentScreen instanceof ClickGuiScreen) {
			mc.setScreen(null);
		}
	}

	public enum Mode {
		Scale, Pull, Scissor, Reset, None
	}

	private enum Pages {
		General,
		Color
	}
	public enum Menu {
		DreamDev,
		Isolation,
		Epsilon
	}
}