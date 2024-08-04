package me.nullpoint.mod.commands.impl;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.managers.ConfigManager;
import me.nullpoint.mod.commands.Command;

import java.util.List;

public class ReloadCommand extends Command {

	public ReloadCommand() {
		super("reload", "debug", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		CommandManager.sendChatMessage("§e[!] §fReloading..");
		Nullpoint.CONFIG = new ConfigManager();
		Nullpoint.PREFIX = Nullpoint.CONFIG.getString("prefix", Nullpoint.PREFIX);
		Nullpoint.CONFIG.loadSettings();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
