package me.nullpoint.mod.commands.impl;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.mod.commands.Command;
import me.nullpoint.mod.modules.impl.client.ChatSetting;
import me.nullpoint.mod.modules.impl.client.Notify;
import net.minecraft.network.message.SentMessage;

import java.util.Arrays;
import java.util.List;

public class NPIRCCommand extends Command {

	public NPIRCCommand() {
		super("np", "Internet Relay Chat", "[text]");
	}
	public void sendNotify(String string){
		Notify.notifyList.add(new Notify.Notifys(string));
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
		if (Nullpoint.isdev) {
			Nullpoint.NPIRC.chat(text.toString());
		}else {
			if (Notify.INSTANCE.isOn() && Notify.INSTANCE.type.getValue()== Notify.Notifys.type.Notify){
				sendNotify("不是开发者，无法使用私有IRC功能");
			}
			if (Notify.INSTANCE.isOn() && Notify.INSTANCE.type.getValue()== Notify.Notifys.type.Chat){
				CommandManager.sendChatMessageWidthId("不是开发者，无法使用私有IRC功能", -1);
			}
		}
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
