package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;

public class Ping extends Module {
    public Ping() {
        super("Ping", Category.Misc);
    }
    private final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Command));
    public enum Mode {
        Request,
        Command
    }
    private long sendTime;
    @Override
    public void onEnable() {
        if (nullCheck()) {
            disable();
            return;
        }
        sendTime = System.currentTimeMillis();
        if (mode.getValue() == Mode.Command) {
            mc.player.networkHandler.sendCommand("chat ");
        } else if (mode.getValue() == Mode.Request) {
            mc.player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(1337, "tell "));
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (mode.getValue() == Mode.Request) {
            if (e.getPacket() instanceof CommandSuggestionsS2CPacket c && c.getCompletionId() == 1337) {
                CommandManager.sendChatMessage("ping: " + (System.currentTimeMillis() - sendTime) / 2);
                disable();
            }
        } else if (mode.getValue() == Mode.Command) {
            if (e.getPacket() instanceof GameMessageS2CPacket packet) {
                if (packet.content().getString().contains("chat.use") || packet.content().getString().contains("<--[HERE]") || packet.content().getString().contains("Unknown")) {
                    CommandManager.sendChatMessage("ping: " + (System.currentTimeMillis() - sendTime) / 2);
                    disable();
                }
            }
        }
    }
}
