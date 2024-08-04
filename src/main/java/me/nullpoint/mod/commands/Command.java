/**
 * A class to represent a Command
 */
package me.nullpoint.mod.commands;

import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.Wrapper;

import java.util.List;
import java.util.Objects;

public abstract class Command implements Wrapper {
	protected final String name;
	protected final String description;
	protected final String syntax;
	
	public Command(String name, String description, String syntax) {
		this.name = Objects.requireNonNull(name);
		this.description = Objects.requireNonNull(description);
		this.syntax = Objects.requireNonNull(syntax);
	}

	public String getName() {
		return this.name;
	}
	public String getDescription() {
		return this.description;
	}

	public String getSyntax() {
		return this.syntax;
	}

	public abstract void runCommand(String[] parameters);

	public abstract String[] getAutocorrect(int count, List<String> seperated);

	public void sendUsage() {
		CommandManager.sendChatMessage("§b[!] §fUsage: §e" + getName() + " " + getSyntax());
	}
}
