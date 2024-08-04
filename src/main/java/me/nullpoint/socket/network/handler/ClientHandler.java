package me.nullpoint.socket.network.handler;

import me.nullpoint.api.utils.render.EnumChatFormatting;
import me.nullpoint.socket.network.packet.Packet;
import me.nullpoint.socket.network.packet.impl.info.UserInfoPacket;
import me.nullpoint.socket.enums.ClientType;
import me.nullpoint.socket.enums.Rank;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;

import java.net.SocketException;

/**
 * @author DreamDev
 * @since 4/7/2024
 */
public class ClientHandler extends SimpleChannelInboundHandler<Packet> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        LogManager.getLogger().info("Received packet: " + packet.getClass().getSimpleName());

        packet.handler(ctx, this);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LogManager.getLogger().info("Connected to server: " + ctx.channel().remoteAddress());
  {
            ctx.writeAndFlush(new UserInfoPacket(
                    ClientType.NullPoint,
                    0,
                     EnumChatFormatting.RESET + MinecraftClient.getInstance().getSession().getUsername(),
                    Rank.USER,
                    0,
                    114514
            ));
        }
        }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogManager.getLogger().warn("Disconnected from server.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof SocketException) {
            LogManager.getLogger().error("Connection reset by peer or server shutdown.");
        } else {
            cause.printStackTrace();
        }
        ctx.close();
    }
}