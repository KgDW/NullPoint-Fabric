package me.nullpoint.mod.commands.impl;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.mod.commands.Command;

import java.util.List;

public class ReloadAllCommand extends Command {

	public ReloadAllCommand() {
		super("reloadall", "debug", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		CommandManager.sendChatMessage("§e[!] §fReloading..");
		Nullpoint.unload();
		Nullpoint.load();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
