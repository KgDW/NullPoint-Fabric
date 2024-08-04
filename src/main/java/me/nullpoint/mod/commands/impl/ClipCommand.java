package me.nullpoint.mod.commands.impl;

import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.mod.commands.Command;

import java.text.DecimalFormat;
import java.util.List;

public class ClipCommand extends Command {

	public ClipCommand() {
		super("clip", "Teleports the player certain blocks away (Vanilla only)", "[x] [y] [z]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length != 3)
		{
			sendUsage();
			return;
		}
		double x;
		double y;
		double z;
		if (isNumeric(parameters[0])) {
			x = mc.player.getX() + Double.parseDouble(parameters[0]);
		} else {
			sendUsage();
			return;
		}

		if (isNumeric(parameters[1])) {
			y = mc.player.getY() + Double.parseDouble(parameters[1]);
		} else {
			sendUsage();
			return;
		}

		if (isNumeric(parameters[2])) {
			z = mc.player.getZ() + Double.parseDouble(parameters[2]);
		} else {
			sendUsage();
			return;
		}
		mc.player.setPosition(x, y, z);
		DecimalFormat df = new DecimalFormat("0.0");
		CommandManager.sendChatMessage("§a[√] §fTeleported to §eX:" + df.format(x) + " Y:" +  df.format(y) + " Z:" + df.format(z));
	}

	private boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?");
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return new String[] {"0 "};
	}
}
