package me.nullpoint.socket.network.packet.impl.info;

import me.nullpoint.socket.network.buffer.PacketBuffer;
import me.nullpoint.socket.network.handler.ClientHandler;
import me.nullpoint.socket.network.info.record.OnlineUserInfo;
import me.nullpoint.socket.network.packet.Packet;
import me.nullpoint.socket.network.user.UserManager;
import me.nullpoint.socket.enums.ClientType;
import me.nullpoint.socket.enums.Rank;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * @author DreamDev
 * @since 4/8/2024
 */

public class OnlineUsersPacket extends Packet {
    private List<OnlineUserInfo> onlineUsers;

    public OnlineUsersPacket() {
    }

    public OnlineUsersPacket(List<OnlineUserInfo> onlineUsers) {
        this.onlineUsers = onlineUsers;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeList(onlineUsers, (buffer, user) -> {
            buffer.writeEnum(user.getClient());
            buffer.writeString(user.getUsername());
            buffer.writeString(user.getInGameName());
            buffer.writeEnum(user.getRank());
        });
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.onlineUsers = buf.readList(buffer -> new OnlineUserInfo(
                buffer.readEnum(ClientType.class),
                buffer.readString(),
                buffer.readString(),
                buffer.readEnum(Rank.class)
        ));
    }

    @Override
    public void handler(ChannelHandlerContext ctx, ClientHandler handler) {
        UserManager.setOnlineUsers(getOnlineUsers());
    }

    public List<OnlineUserInfo> getOnlineUsers() {
        return onlineUsers;
    }

    public void setOnlineUsers(List<OnlineUserInfo> onlineUsers) {
        this.onlineUsers = onlineUsers;
    }
}