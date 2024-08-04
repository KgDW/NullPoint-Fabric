package me.nullpoint.mod.commands.impl;

import me.nullpoint.Nullpoint;
import me.nullpoint.mod.commands.Command;
import me.nullpoint.api.managers.CommandManager;

import java.util.ArrayList;
import java.util.List;

public class FriendCommand extends Command {

	public FriendCommand() {
		super("friend", "Set friend", "[name/reset/list] | [add/del] [name]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 0) {
			sendUsage();
			return;
		}
		if (parameters[0].equals("reset")) {
			Nullpoint.FRIEND.friendList.clear();
			CommandManager.sendChatMessage("§a[√] §bFriends list §egot reset");
			return;
		}
		if (parameters[0].equals("list")) {
			if (Nullpoint.FRIEND.friendList.isEmpty()) {
				CommandManager.sendChatMessage("§e[!] §bFriends list §eempty");
				return;
			}
			StringBuilder friends = new StringBuilder();
			boolean first = true;
			for (String name : Nullpoint.FRIEND.friendList) {
				if (!first) {
					friends.append(", ");
				}
				friends.append(name);
				first = false;
			}
			CommandManager.sendChatMessage("§e[~] §bFriends§e:§a" + friends);
			return;
		}

		if (parameters[0].equals("add")) {
			if (parameters.length == 2) {
				Nullpoint.FRIEND.addFriend(parameters[1]);
				CommandManager.sendChatMessage("§a[√] §b" + parameters[1] + (Nullpoint.FRIEND.isFriend(parameters[1]) ? " §ahas been friended" : " §chas been unfriended"));
				return;
			}
			sendUsage();
			return;
		} else if (parameters[0].equals("del")) {
			if (parameters.length == 2) {
				Nullpoint.FRIEND.removeFriend(parameters[1]);
				CommandManager.sendChatMessage("§a[√] §b" + parameters[1] + (Nullpoint.FRIEND.isFriend(parameters[1]) ? " §ahas been friended" : " §chas been unfriended"));
				return;
			}
			sendUsage();
			return;
		}

		if (parameters.length == 1) {
			CommandManager.sendChatMessage("§a[√] §b" + parameters[0] + (Nullpoint.FRIEND.isFriend(parameters[0]) ? " §ais friended" : " §cisn't friended"));
			return;
		}

		sendUsage();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		if (count == 1) {
			String input = seperated.get(seperated.size() - 1).toLowerCase();
			List<String> correct = new ArrayList<>();
			List<String> list = List.of("add", "del", "list", "reset");
			for (String x : list) {
				if (input.equalsIgnoreCase(Nullpoint.PREFIX + "friend") || x.toLowerCase().startsWith(input)) {
					correct.add(x);
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
