package me.nullpoint.socket.network.packet;

import me.nullpoint.socket.network.packet.impl.info.GameInfoPacket;
import me.nullpoint.socket.network.packet.impl.info.OnlineUsersPacket;
import me.nullpoint.socket.network.packet.impl.info.TokenPacket;
import me.nullpoint.socket.network.packet.impl.info.UserInfoPacket;
import me.nullpoint.socket.network.packet.impl.message.ChatMessagePacket;
import me.nullpoint.socket.network.packet.impl.message.MessagePacket;
import me.nullpoint.socket.network.packet.impl.operation.OperationPacket;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author DreamDev
 * @since 4/7/2024
 */

public class PacketFactory {
    private static final Map<Integer, Class<? extends Packet>> packetMap = new HashMap<>();

    static {
        packetMap.put(0, TokenPacket.class);
        packetMap.put(1, UserInfoPacket.class);
        packetMap.put(2, GameInfoPacket.class);
        packetMap.put(3, OnlineUsersPacket.class);
        packetMap.put(4, MessagePacket.class);
        packetMap.put(5, ChatMessagePacket.class);
        packetMap.put(6, OperationPacket.class);
    }

    public static Class<? extends Packet> getPacketClass(int packetId) {
        return packetMap.get(packetId);
    }

    public static Packet createPacket(int packetId) {
        Class<? extends Packet> packetClass = packetMap.get(packetId);
        if (packetClass != null) {
            try {
                return packetClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                LogManager.getLogger().info("Error instantiating packet class for ID " + packetId + ": " + e.getMessage());
            }
        }
        return null;
    }

    public static Integer getPacketId(Class<? extends Packet> packetClass) {
        return packetMap.entrySet()
                .stream()
                .filter(entry -> packetClass.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
