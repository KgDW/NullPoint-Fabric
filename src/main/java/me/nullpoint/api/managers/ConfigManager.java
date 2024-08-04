package me.nullpoint.api.managers;

import com.google.common.base.Splitter;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.mod.gui.clickgui.tabs.ClickGuiTab;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.Setting;
import me.nullpoint.mod.modules.settings.impl.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class ConfigManager implements Wrapper {
	public static File options = new File(mc.runDirectory, "nullpoint_options.txt");
	private final Hashtable<String, String> settings = new Hashtable<>();

	public ConfigManager() {
		readSettings();
	}

	public static void resetModule() {
		for (Module module : Nullpoint.MODULE.modules) {
			module.setState(false);
		}
	}
	public void loadSettings() {
		for (Module module : Nullpoint	.MODULE.modules) {
			for (Setting setting : module.getSettings()) {
				setting.loadSetting();
			}
			module.setState(Nullpoint.CONFIG.getBoolean(module.getName() + "_state", false));
		}
	}
	public void saveSettings() {
		PrintWriter printwriter = null;
		try {
			printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(options), StandardCharsets.UTF_8));

			printwriter.println("prefix:" + Nullpoint.PREFIX);
			// Write HUD information and 'other' settings.

			for (ClickGuiTab tab : Nullpoint.GUI.tabs) {
				printwriter.println(tab.getTitle() + "_x:" + tab.getX());
				printwriter.println(tab.getTitle() + "_y:" + tab.getY());
			}
			printwriter.println("armor_x:" + Nullpoint.GUI.armorHud.getX());
			printwriter.println("armor_y:" + Nullpoint.GUI.armorHud.getY());

			// Write Module Settings
			for (Module module : Nullpoint.MODULE.modules) {
				for (Setting setting : module.getSettings()) {
					if (setting instanceof BooleanSetting bs) {
						printwriter.println(bs.getLine() + ":" + bs.getValue());
					}else if (setting instanceof SliderSetting ss) {
						printwriter.println(ss.getLine() + ":" + ss.getValue());
					} else if (setting instanceof BindSetting bs) {
						printwriter.println(bs.getLine() + ":" + bs.getKey());
						printwriter.println(bs.getLine() + "_hold" + ":" + bs.isHoldEnable());
					} else if (setting instanceof EnumSetting es) {
						printwriter.println(es.getLine() + ":" + es.getValue().name());
					} else if (setting instanceof ColorSetting cs) {
						printwriter.println(cs.getLine() + ":" + cs.getValue().getRGB());
						printwriter.println(cs.getLine() + "Rainbow:" + cs.isRainbow);
						if (cs.injectBoolean) {
							printwriter.println(cs.getLine() + "Boolean:" + cs.booleanValue);
						}
					} else if (setting instanceof StringSetting ss) {
						printwriter.println(ss.getLine() + ":" + ss.getValue());
					}
				}
				printwriter.println(module.getName() + "_state:" + module.isOn());
			}
		} catch (Exception exception) {
			System.out.println("[" + Nullpoint.LOG_NAME + "] Failed to save settings");
		} finally {
			IOUtils.closeQuietly(printwriter);
		}
	}

	public void readSettings() {
		final Splitter COLON_SPLITTER = Splitter.on(':');
		try {
			if (!options.exists()) {
				return;
			}
			List<String> list = IOUtils.readLines(new FileInputStream(options), StandardCharsets.UTF_8);
			for (String s : list) {
				try {
					Iterator<String> iterator = COLON_SPLITTER.limit(2).split(s).iterator();
					settings.put(iterator.next(), iterator.next());
				} catch (Exception var10) {
					System.out.println("Skipping bad option: " + s);
				}
			}
			//KeyBinding.updateKeysByCode();
		} catch (Exception exception) {
			System.out.println("[" + Nullpoint.LOG_NAME + "] Failed to load settings");
		}
	}

	public static boolean isInteger(final String str) {
		final Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
		return pattern.matcher(str).matches();
	}

	public static boolean isFloat(String str) {
		String pattern = "^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$";
		return str.matches(pattern);
	}
	public int getInt(String setting, int defaultValue) {
		String s = settings.get(setting);
		if(s == null || !isInteger(s)) return defaultValue;
		return Integer.parseInt(s);
	}

	public float getFloat(String setting, float defaultValue) {
		String s = settings.get(setting);
		if (s == null || !isFloat(s)) return defaultValue;
		return Float.parseFloat(s);
	}
	public boolean getBoolean(String setting) {
		String s = settings.get(setting);
		return Boolean.parseBoolean(s);
	}

	public boolean getBoolean(String setting, boolean defaultValue) {
		if (settings.get(setting) != null) {
			String s = settings.get(setting);
			return Boolean.parseBoolean(s);
		} else {
			return defaultValue;
		}
	}

	public String getString(String setting) {
		return settings.get(setting);
	}

	public String getString(String setting, String defaultValue) {
		if (settings.get(setting) == null) {
			return defaultValue;
		}
		return settings.get(setting);
	}
}
