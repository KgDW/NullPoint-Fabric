package me.nullpoint.socket.network.handler;

import me.nullpoint.socket.network.buffer.PacketBuffer;
import me.nullpoint.socket.network.packet.Packet;
import me.nullpoint.socket.network.packet.PacketFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author DreamDev
 * @since 4/7/2024
 */
public class PacketDecoder extends ByteToMessageDecoder {

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        var packetId = in.readInt();

        Packet packet = PacketFactory.createPacket(packetId);
        PacketBuffer buffer = new PacketBuffer(in);

        if (packet != null) {
            packet.decode(buffer);

            out.add(packet);
        }
    }
}