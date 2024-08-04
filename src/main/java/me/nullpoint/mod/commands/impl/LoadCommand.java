package me.nullpoint.mod.commands.impl;


import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.managers.ConfigManager;
import me.nullpoint.mod.commands.Command;

import java.io.File;
import java.util.List;

public class LoadCommand extends Command {

    public LoadCommand() {
        super("load", "debug", "[config]");
    }

    @Override
    public void runCommand(String[] parameters) {
        if (parameters.length == 0) {
            sendUsage();
            return;
        }
        CommandManager.sendChatMessage("§e[!] §fLoading..");
        ConfigManager.options = new File(mc.runDirectory, parameters[0] + ".cfg");
        Nullpoint.unload();
        Nullpoint.load();
        ConfigManager.options = new File(mc.runDirectory, "nullpoint_options.txt");
        Nullpoint.save();
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }
}