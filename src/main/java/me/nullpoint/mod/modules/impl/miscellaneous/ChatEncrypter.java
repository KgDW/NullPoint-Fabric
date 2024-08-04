package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.SendMessageEvent;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.asm.accessors.IGameMessageS2CPacket;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BindSetting;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.StringSetting;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;

import java.util.Base64;
import java.util.Objects;

public class ChatEncrypter extends Module {
    public static ChatEncrypter INSTANCE;
    private boolean b = false;

    public ChatEncrypter() {
        super("ChatEncrypter", Category.Misc);
        INSTANCE=this;
    }
    private final StringSetting prefix = add(new StringSetting("Prefix","RebirthChat" ));
    private final StringSetting key = add(new StringSetting("KEY","114514" ));
    public final BooleanSetting encrypt = add(new BooleanSetting("Encrypt", true).setParent());
    private final BindSetting openbind = add(new BindSetting("OpenEncryptBind", -1));
    private final BooleanSetting decrypt = add(new BooleanSetting("Decrypt", true));

    @EventHandler
    public void onSendMessage(SendMessageEvent event) {
        if (nullCheck() || event.isCancel()) return;
        if(!encrypt.getValue()) return;
        if(ChatSuffix.INSTANCE.isOn() && ChatSuffix.INSTANCE.green.getValue()) {
            event.message = ">_" + prefix.getValue() + "_" + Base64.getEncoder().encodeToString(encrypt(event.message, key.getValue()).getBytes());
        }else{
            event.message = "_" + prefix.getValue() + "_" + Base64.getEncoder().encodeToString(encrypt(event.message, key.getValue()).getBytes());
        }

    }
    @Override
    public void onUpdate() {
        if(openbind.getKey() == -1) return;
        if(openbind.isPressed() && !b){
            encrypt.setValue(!encrypt.getValue());
            if(encrypt.getValue()) {
                CommandManager.sendChatMessage("\u00a7a[#]] \u00a7f\u00a7o" + "Open" + " Encrypt");
            }else{
                CommandManager.sendChatMessage("\u00a7e[#] \u00a7c\u00a7o" + "Disable" + " Encrypt");
            }
            b = true;
        }
        if(!openbind.isPressed()){
            b = false;
        }
    }

    @EventHandler
    private void PacketReceive(PacketEvent.Receive receive){
        if(nullCheck()){
            return;
        }
        if(!decrypt.getValue()) return;
        if(receive.getPacket() instanceof GameMessageS2CPacket e) {
            String[] m = e.content().getString().split("_");
            if(m.length < 2) return;
            if(Objects.equals(m[1], prefix.getValue())){
                ((IGameMessageS2CPacket)receive.getPacket()).setContent(Text.of( CommandManager.syncCode + m[0] + " " + decrypt(new String(Base64.getDecoder().decode(m[2])), key.getValue())));
            }

            // receive.cancel();

        }

    }
    public static String encrypt(String string, String key) {
        char[] chars = key.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            for (char ckey : chars) {
                c ^= ckey;

            }
            builder.append(c);
        }
        return builder.toString();
    }

    public static String decrypt(String string, String key) {
        char[] chars = key.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            for (char ckey : chars) {
                c ^= ckey;

            }
            builder.append(c);
        }
        return builder.toString();
    }
}

