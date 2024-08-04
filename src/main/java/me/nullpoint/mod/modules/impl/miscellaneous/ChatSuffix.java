package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.SendMessageEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.StringSetting;

public class ChatSuffix extends Module {
	public static ChatSuffix INSTANCE;
	private final StringSetting message = add(new StringSetting("append", "\uD835\uDD2B\uD835\uDD32\uD835\uDD29\uD835\uDD29\uD835\uDD2D\uD835\uDD2C\uD835\uDD26\uD835\uDD2B\uD835\uDD31"));
	public final BooleanSetting green = add(new BooleanSetting("Green",false));
	public ChatSuffix() {
		super("ChatSuffix", Category.Misc);
		INSTANCE = this;
	}
	public static final String nullPointSuffix = "\uD835\uDD2B\uD835\uDD32\uD835\uDD29\uD835\uDD29\uD835\uDD2D\uD835\uDD2C\uD835\uDD26\uD835\uDD2B\uD835\uDD31";
	public static final String mioSuffix = "⋆ ᴍɪᴏ";
	public static final String AshSuffix = "\uD83D\uDD25ₐₛₕ";
	public static final String jeezSuffix = " | Jeezʜᴀck/1.4";
	public static final String scannerSuffix = " | Scanner ʜᴀᴄᴋ";
	public static final String m7thh4ckSuffix = " | \uD835\uDCC27\uD835\uDCC9\uD835\uDCBD\uD835\uDCBD4\uD835\uDCB8\uD835\uDCC0-$";
	public static final String moonSuffix = "☽\uD835\uDD10\uD835\uDD2C\uD835\uDD2C\uD835\uDD2B";
	public static final String melonSuffix = "\uD835\uDD10\uD835\uDD22\uD835\uDD29\uD835\uDD2C\uD835\uDD2B\uD835\uDD05\uD835\uDD22\uD835\uDD31\uD835\uDD1E";
	@EventHandler
	public void onSendMessage(SendMessageEvent event) {
		if (nullCheck() || event.isCancelled()) return;
		String message = event.message;

		if (message.startsWith("/") || message.startsWith("!") || message.endsWith(this.message.getValue())) {
			return;
		}
		String suffix = this.message.getValue();
		message = message + " " + suffix;
		event.message = message;
	}

	public String getSuffix() {
		return null;
	}
}