package me.nullpoint.mod.commands.impl;

import me.nullpoint.Nullpoint;
import me.nullpoint.mod.commands.Command;
import me.nullpoint.mod.modules.impl.client.HUD;

import java.util.Arrays;
import java.util.List;

public class IRCCommand extends Command {

	public IRCCommand() {
		super("irc", "Internet Relay Chat", "[text]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 0) {
			sendUsage();
			return;
		}
		StringBuilder text = new StringBuilder();
		for (String s : Arrays.stream(parameters).toList()) {
			text.append(" ").append(s);
		}
		Nullpoint.IRC.chat(text.toString());
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
