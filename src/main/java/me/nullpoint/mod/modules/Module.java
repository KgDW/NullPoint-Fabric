/**
 * A class to represent a generic module.
 */
package me.nullpoint.mod.modules;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.managers.ModuleManager;
import me.nullpoint.mod.Mod;
import me.nullpoint.mod.modules.impl.client.ChatSetting;
import me.nullpoint.mod.modules.impl.client.Notify;
import me.nullpoint.mod.modules.settings.*;
import me.nullpoint.mod.modules.settings.impl.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Module extends Mod {

	public Module(String name, Category category) {
		this(name, "", category);
	}

	public Module(String name, String description, Category category) {
		super(name);
		this.category = category;
		this.description = description;
		ModuleManager.lastLoadMod = this;
		bindSetting = new BindSetting("Key", name.equalsIgnoreCase("UI") ? GLFW.GLFW_KEY_Y : -1);
		drawnSetting = add(new BooleanSetting("Drawn", true));
		drawnSetting.hide();
	}
	private String description;
	private final Category category;
	private final BindSetting bindSetting;
	public final BooleanSetting drawnSetting;
	public boolean state;

	private final List<Setting> settings = new ArrayList<>();

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Module.Category getCategory() {
		return this.category;
	}
	public void sendNotify(String string){
		Notify.notifyList.add(new Notify.Notifys(string));
	}

	public BindSetting getBind() {
		return this.bindSetting;
	}
	public boolean isOn() {
		return this.state;
	}

	public boolean isOff() {
		return !isOn();
	}

	public void toggle() {
		if (this.isOn()) {
			disable();
		} else {
			enable();
		}
	}

	public void enable() {
		if (this.state) return;
		if (!nullCheck() && drawnSetting.getValue()) {
			if(Notify.INSTANCE.isOn() && Notify.INSTANCE.type.getValue()== Notify.Notifys.type.Both){
				switch (ChatSetting.INSTANCE.messageStyle.getValue()) {
					case Mio -> CommandManager.sendChatMessageWidthId("§2[+] §f" + getName(), -1);
					case Basic -> CommandManager.sendChatMessageWidthId("§f" + getName() + " §aEnabled", -1);
					case Future -> CommandManager.sendChatMessageWidthId("§7" + getName() + " toggled §2on §f", -1);
					case Earth -> CommandManager.sendChatMessageWidthIdNoSync("§l" + getName() + " §aEnabled", -1);
				}
				sendNotify("§f" + "[" + "§b" + this.getName() + "§f" + "]" + "§7" + " toggled " + "§a" + "on");
			}
			else if(Notify.INSTANCE.isOn() && Notify.INSTANCE.type.getValue()== Notify.Notifys.type.Notify){
				sendNotify("§f" + "[" + "§b" + this.getName() + "§f" + "]" + "§7" + " toggled " + "§a" + "on");
			}
			else if(Notify.INSTANCE.type.getValue()== Notify.Notifys.type.Chat){
				switch (ChatSetting.INSTANCE.messageStyle.getValue()) {
					case Mio -> CommandManager.sendChatMessageWidthId("§2[+] §f" + getName(), -1);
					case Basic -> CommandManager.sendChatMessageWidthId("§f" + getName() + " §aEnabled", -1);
					case Future -> CommandManager.sendChatMessageWidthId("§7" + getName() + " toggled §2on §f", -1);
					case Earth -> CommandManager.sendChatMessageWidthIdNoSync("§l" + getName() + " §aEnabled", -1);
				}
			}
		}
		this.state = true;
		Nullpoint.EVENT_BUS.subscribe(this);
		this.onToggle();
		try {
			this.onEnable();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void disable() {
		if (!this.state) return;
		if (!nullCheck() && drawnSetting.getValue()) {
			if (Notify.INSTANCE.isOn() && Notify.INSTANCE.type.getValue() == Notify.Notifys.type.Both) {
				switch (ChatSetting.INSTANCE.messageStyle.getValue()) {
					case Mio -> CommandManager.sendChatMessageWidthId("§4[-] §f" + getName(), -1);
					case Basic -> CommandManager.sendChatMessageWidthId("§f" + getName() + " §cDisabled", -1);
					case Future -> CommandManager.sendChatMessageWidthId("§7" + getName() + " toggled §4off §f", -1);
					case Earth -> CommandManager.sendChatMessageWidthIdNoSync("§l" + getName() + " §cDisabled", -1);
				}
				sendNotify("§f" + "[" + "§b" + this.getName() + "§f" + "]" + "§7" + " toggled " + "§4" + "off");
			}
			else if (Notify.INSTANCE.isOn() && Notify.INSTANCE.type.getValue() == Notify.Notifys.type.Notify) {
				sendNotify("§f" + "[" + "§b" + this.getName() + "§f" + "]" + "§7" + " toggled " + "§4" + "off");
			}
			else if(Notify.INSTANCE.isOn() && Notify.INSTANCE.type.getValue()== Notify.Notifys.type.Chat){
				switch (ChatSetting.INSTANCE.messageStyle.getValue()) {
					case Mio -> CommandManager.sendChatMessageWidthId("§4[-] §f" + getName(), -1);
					case Basic -> CommandManager.sendChatMessageWidthId("§f" + getName() + " §cDisabled", -1);
					case Future -> CommandManager.sendChatMessageWidthId("§7" + getName() + " toggled §4off §f", -1);
					case Earth -> CommandManager.sendChatMessageWidthIdNoSync("§l" + getName() + " §cDisabled", -1);
				}
			}
		}
		this.state = false;
		Nullpoint.EVENT_BUS.unsubscribe(this);
		this.onToggle();
		this.onDisable();
	}
	public void setState(boolean state) {
		if (this.state == state) return;
		if (state) {
			enable();
		} else {
			disable();
		}
	}

	public boolean setBind(String rkey) {
		if (rkey.equalsIgnoreCase("none")) {
			this.bindSetting.setKey(-1);
			return true;
		}
		int key;
		try {
			key = InputUtil.fromTranslationKey("key.keyboard." + rkey.toLowerCase()).getCode();
		} catch (NumberFormatException e) {
			if (!nullCheck()) CommandManager.sendChatMessage("§c[!] §fBad key!");
			return false;
		}
		if (rkey.equalsIgnoreCase("none")) {
			key = -1;
		}
		if (key == 0) {
			return false;
		}
		this.bindSetting.setKey(key);
		return true;
	}

	public void addSetting(Setting setting) {
		this.settings.add(setting);
	}

	public StringSetting add(StringSetting setting) {
		addSetting(setting);
		return setting;
	}

	public ColorSetting add(ColorSetting setting) {
		addSetting(setting);
		return setting;
	}

	public SliderSetting add(SliderSetting setting) {
		addSetting(setting);
		return setting;
	}

	public BooleanSetting add(BooleanSetting setting) {
		addSetting(setting);
		return setting;
	}

	public <T extends Enum<T>> EnumSetting<T> add(EnumSetting<T> setting) {
		addSetting(setting);
		return setting;
	}

	public BindSetting add(BindSetting setting) {
		addSetting(setting);
		return setting;
	}

	public List<Setting> getSettings() {
		return this.settings;
	}

	public boolean hasSettings() {
		return !this.settings.isEmpty();
	}

	public static boolean nullCheck() {
		return mc.player == null || mc.world == null;
	}

	public void onDisable() {

	}

	public void onEnable() throws IOException {

	}

	public void onToggle() {

	}

	public void onUpdate() {

	}

	public void onLogin() {

	}

	public void onLogout() {

	}
	public void onRender2D(DrawContext drawContext, float tickDelta) {

	}

	public void onRender3D(MatrixStack matrixStack, float partialTicks) {

	}

	public final boolean isCategory(Module.Category category) {
		return category == this.category;
	}

	public String getArrayName() {
		return getName() + getArrayInfo();
	}
	public String getArrayInfo() {
		return (getInfo() == null ? "" : " §7[" + getInfo() + "§7]");
	}
	public String getInfo() {
		return null;
	}

	public enum Category {
		Combat, Misc, Render, Movement, Player, Exploit, Client
	}
}