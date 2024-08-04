package me.nullpoint.socket.network.packet;

import me.nullpoint.socket.network.buffer.PacketBuffer;
import me.nullpoint.socket.network.handler.ClientHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author DreamDev
 * @since 4/7/2024
 */

public abstract class Packet {
    public abstract void encode(PacketBuffer buf);

    public abstract void decode(PacketBuffer buf);

    public abstract void handler(ChannelHandlerContext ctx, ClientHandler handler);
}