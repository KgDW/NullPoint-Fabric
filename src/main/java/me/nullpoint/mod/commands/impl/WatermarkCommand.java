package me.nullpoint.mod.commands.impl;

import me.nullpoint.mod.commands.Command;
import me.nullpoint.mod.modules.impl.client.HUD;

import java.util.Arrays;
import java.util.List;

public class WatermarkCommand extends Command {

	public WatermarkCommand() {
		super("watermark", "change watermark", "[text]");
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
		HUD.INSTANCE.watermarkString.setValue(text.toString());
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
