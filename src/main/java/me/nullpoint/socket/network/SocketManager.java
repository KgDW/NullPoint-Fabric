package me.nullpoint.socket.network;

import me.nullpoint.socket.network.client.SocketClient;
import me.nullpoint.socket.network.packet.Packet;
import me.nullpoint.socket.network.packet.impl.message.ChatMessagePacket;
import me.nullpoint.socket.network.packet.impl.operation.OperationPacket;
import me.nullpoint.socket.network.user.UserManager;
import me.nullpoint.socket.enums.ChannelType;
import me.nullpoint.socket.enums.Operation;

/**
 * @author DreamDev
 * @since 4/8/2024
 */
public class SocketManager {
    private final SocketClient client = new SocketClient();

    private static String prefix = "!";

    public SocketClient getClient() {
        return client;
    }

    public String getPrefix() {
        return prefix;
    }

    public void send(Packet packet) {
        client.send(packet);
    }

    // 我去发你
    public void chat(String message) {
        this.send(new ChatMessagePacket(
                ChannelType.GLOBAL,
                message,
                System.currentTimeMillis()));
    }

    public void operation(Operation operation, String targetUsername, String message) {
        this.send(new OperationPacket(
                UserManager.getUser().getUsername(),
                targetUsername,
                message,
                operation
        ));
    }
}
