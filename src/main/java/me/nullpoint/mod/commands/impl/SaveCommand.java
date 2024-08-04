package me.nullpoint.mod.commands.impl;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.managers.ConfigManager;
import me.nullpoint.mod.commands.Command;

import java.io.File;
import java.util.List;

public class SaveCommand extends Command {

	public SaveCommand() {
		super("save", "save", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		CommandManager.sendChatMessage("§e[!] §fSaving..");
		if (parameters.length == 1) {
			ConfigManager.options = new File(mc.runDirectory, parameters[0] + ".cfg");
			Nullpoint.save();
			ConfigManager.options = new File(mc.runDirectory, "nullpoint_options.txt");
		}
		Nullpoint.save();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
