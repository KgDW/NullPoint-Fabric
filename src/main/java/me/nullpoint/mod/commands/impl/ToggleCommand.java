package me.nullpoint.mod.commands.impl;

import me.nullpoint.Nullpoint;
import me.nullpoint.mod.commands.Command;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.api.managers.ModuleManager;

import java.util.ArrayList;
import java.util.List;

public class ToggleCommand extends Command {

	public ToggleCommand() {
		super("toggle", "Toggle module", "[module]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 0) {
			sendUsage();
			return;
		}
		String moduleName = parameters[0];
		Module module = Nullpoint.MODULE.getModuleByName(moduleName);
		if (module == null) {
			CommandManager.sendChatMessage("§4[!] §fUnknown §bmodule!");
			return;
		}
		module.toggle();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		if (count == 1) {
			String input = seperated.get(seperated.size() - 1).toLowerCase();
			ModuleManager cm = Nullpoint.MODULE;
			List<String> correct = new ArrayList<>();
			for (Module x : cm.modules) {
				if (input.equalsIgnoreCase(Nullpoint.PREFIX + "toggle") || x.getName().toLowerCase().startsWith(input)) {
					correct.add(x.getName());
				}
			}
			int numCmds = correct.size();
			String[] commands = new String[numCmds];

			int i = 0;
			for (String x : correct) {
				commands[i++] = x;
			}

			return commands;
		}
		return null;
	}
}
