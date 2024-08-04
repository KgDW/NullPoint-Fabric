/**
 * A class to represent a system to manage Commands.
 */
package me.nullpoint.api.managers;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.interfaces.IChatHud;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.mod.commands.Command;
import me.nullpoint.mod.commands.impl.*;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.ChatSetting;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.HashMap;

public class CommandManager implements Wrapper {
	public static final String syncCode = "§(";
	private final HashMap<String, Command> commands = new HashMap<>();
	public final AimCommand aim = new AimCommand();
	public final BindCommand bind = new BindCommand();
	public final ClipCommand clip = new ClipCommand();
	public final FriendCommand friend = new FriendCommand();
	public final GamemodeCommand gamemode = new GamemodeCommand();
	public final HelpCommand help = new HelpCommand();
	public final PrefixCommand prefix = new PrefixCommand();
	public final LoadCommand load = new LoadCommand();
	public final RejoinCommand rejoin = new RejoinCommand();
	public final ReloadCommand reload = new ReloadCommand();
	public final ReloadAllCommand reloadHack = new ReloadAllCommand();
	public final SaveCommand save = new SaveCommand();
	public final TeleportCommand tp = new TeleportCommand();
	public final Toggle2Command t = new Toggle2Command();
	public final ToggleCommand toggle = new ToggleCommand();
	public final WatermarkCommand watermark = new WatermarkCommand();
	public final IRCCommand irc = new IRCCommand();
	public final NPIRCCommand np = new NPIRCCommand();

	public CommandManager() {
		try
		{
			for(Field field : CommandManager.class.getDeclaredFields())
			{
				if (!Command.class.isAssignableFrom(field.getType())) 
					continue;
				Command cmd = (Command)field.get(this);
				commands.put(cmd.getName(), cmd);
			}
		}catch(Exception e)
		{
			System.out.println("Error initializing " + Nullpoint.LOG_NAME + " commands.");
			System.out.println(e.getStackTrace().toString());
		}
	}

	public Command getCommandBySyntax(String string) {
		return this.commands.get(string);
	}

	public HashMap<String, Command> getCommands() {
		return this.commands;
	}

	public int getNumOfCommands() {
		return this.commands.size();
	}

	public void command(String[] commandIn) {

		// Get the command from the user's message. (Index 0 is Username)
		Command command = commands.get(commandIn[0].substring(Nullpoint.PREFIX.length()).toLowerCase());

		// If the command does not exist, throw an error.
		if (command == null)
			sendChatMessage("§c[!] §fInvalid Command! Type §e" + "help §ffor a list of commands.");
		else {
			// Otherwise, create a new parameter list.
			String[] parameterList = new String[commandIn.length - 1];
			for (int i = 1; i < commandIn.length; i++) {
				parameterList[i - 1] = commandIn[i];
			}
			if (parameterList.length == 1 && parameterList[0].equals("help")) {
				command.sendUsage();
				return;
			}
			// Runs the command.
			command.runCommand(parameterList);
		}
	}

	public static void sendChatMessage(String message) {
		if (Module.nullCheck()) return;
		String startCode = "";
		String endCode = "";
		if(ChatSetting.INSTANCE.messageCode.getValue() == ChatSetting.code.Mio){
			startCode = "[";
			endCode = "]";
		}
		if(ChatSetting.INSTANCE.messageCode.getValue() == ChatSetting.code.Earth){
			startCode = "<";
			endCode = ">";
		}
		if(ChatSetting.INSTANCE.messageCode.getValue() == ChatSetting.code.Custom){
			startCode = ChatSetting.INSTANCE.start.getValue();
			endCode = ChatSetting.INSTANCE.end.getValue();
		}
		mc.inGameHud.getChatHud().addMessage(Text.of(syncCode + "§r" + startCode + ChatSetting.INSTANCE.hackName.getValue() + "§r" + endCode + "§f " + message));
	}

	public static void sendChatMessageWidthId(String message, int id) {
		if (Module.nullCheck()) return;
		String startCode = "";
		String endCode = "";
		if(ChatSetting.INSTANCE.messageCode.getValue() == ChatSetting.code.Mio){
			startCode = "[";
			endCode = "]";
		}
		if(ChatSetting.INSTANCE.messageCode.getValue() == ChatSetting.code.Earth){
			startCode = "<";
			endCode = ">";
		}
		if(ChatSetting.INSTANCE.messageCode.getValue() == ChatSetting.code.Custom){
			startCode = ChatSetting.INSTANCE.start.getValue();
			endCode = ChatSetting.INSTANCE.end.getValue();
		}
		((IChatHud) mc.inGameHud.getChatHud()).nullpoint_nextgen_master$add(Text.of(syncCode + "§r" + startCode + ChatSetting.INSTANCE.hackName.getValue() + "§r" + endCode + "§f "  + message), id);
	}

	public static void sendChatMessageWidthIdNoSync(String message, int id) {
		if (Module.nullCheck()) return;
		((IChatHud) mc.inGameHud.getChatHud()).nullpoint_nextgen_master$add(Text.of("§f" + message), id);
	}
}
