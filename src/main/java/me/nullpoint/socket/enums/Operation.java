package me.nullpoint.socket.enums;


import me.nullpoint.Nullpoint;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;

import static me.nullpoint.api.managers.CommandManager.sendChatMessage;

/**
 * @author DreamDev
 * @since 4/7/2024
 */
public enum Operation {
    CRASH("Crash"),
    IRC_CHAT("IrcChat"),
    CHAT("Chat"),
    TITLE("Title"),
    KICK("Kick");
    private final String name;

    Operation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void handler(String message) {
        switch (this) {
            case CRASH:
                // CrashUtils.crash(message);
                MinecraftClient.getInstance().player = null;
                MinecraftClient.getInstance().world = null;
                break;
            case CHAT:
                sendChatMessage(message);
                break;
            case IRC_CHAT:
                Nullpoint.IRC.chat(message);
                break;
            case TITLE:
                sendChatMessage(message);
                break;
            case KICK:
                // Objects.requireNonNull(mc.getConnection()).onDisconnect(new DisconnectS2CPacket(Text.of(message)));
                break;
        }
    }

    public static Operation fromString(String name) {
        for (Operation operation : values()) {
            if (operation.getName().equalsIgnoreCase(name)) {
                return operation;
            }
        }
        LogManager.getLogger().info("No enum constant for name: " + name);
        return null;
    }
}
