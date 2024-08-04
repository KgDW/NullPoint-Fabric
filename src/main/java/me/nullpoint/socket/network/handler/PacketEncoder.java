package me.nullpoint.socket.network.handler;

import me.nullpoint.socket.network.buffer.PacketBuffer;
import me.nullpoint.socket.network.packet.Packet;
import me.nullpoint.socket.network.packet.PacketFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author DreamDev
 * @since 4/7/2024
 */
public class PacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
        out.writeInt(PacketFactory.getPacketId(packet.getClass()));

        PacketBuffer buffer = new PacketBuffer(out);
        packet.encode(buffer);
    }
}