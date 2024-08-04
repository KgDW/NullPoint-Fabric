package me.nullpoint.socket.network.packet.impl.message;

import io.netty.channel.ChannelHandlerContext;
import me.nullpoint.api.utils.render.EnumChatFormatting;
import me.nullpoint.mod.modules.impl.client.IRC;
import me.nullpoint.mod.modules.impl.client.Notify;
import me.nullpoint.socket.enums.ChannelType;
import me.nullpoint.socket.enums.ChatType;
import me.nullpoint.socket.enums.ClientType;
import me.nullpoint.socket.enums.Rank;
import me.nullpoint.socket.network.buffer.PacketBuffer;
import me.nullpoint.socket.network.handler.ClientHandler;
import me.nullpoint.socket.network.packet.Packet;

import static me.nullpoint.api.managers.CommandManager.sendChatMessage;

/**
 * @author DreamDev
 * @since 4/8/2024
 */
public class MessagePacket extends Packet {
    private ClientType client;
    private Rank rank;
    private ChannelType channel;
    private ChatType chat;
    private String username;
    private String message;
    private long timestamp;

    public MessagePacket() {
    }

    public MessagePacket(ClientType client, Rank rank, ChannelType channel, ChatType chat, String username, String message, long timestamp) {
        this.client = client;
        this.rank = rank;
        this.channel = channel;
        this.chat = chat;
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
    }

    public ClientType getClient() {
        return client;
    }

    public Rank getRank() {
        return rank;
    }

    public ChannelType getChannel() {
        return channel;
    }

    public ChatType getChat() {
        return chat;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeEnum(client);
        buf.writeEnum(rank);
        buf.writeEnum(channel);
        buf.writeEnum(chat);
        buf.writeString(username);
        buf.writeString(message);
        buf.writeLong(timestamp);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.client = buf.readEnum(ClientType.class);
        this.rank = buf.readEnum(Rank.class);
        this.channel = buf.readEnum(ChannelType.class);
        this.chat = buf.readEnum(ChatType.class);
        this.username = buf.readString();
        this.message = buf.readString();
        this.timestamp = buf.readLong();
    }

    @Override
    public void handler(ChannelHandlerContext ctx, ClientHandler handler) {
//        if (!getClient().getName().equalsIgnoreCase("NullPoint")) return;
            if (IRC.INSTANCE.isOn() && IRC.INSTANCE.tp.getValue() == IRC.type.Chat) {
            sendChatMessage(EnumChatFormatting.GOLD + "[IRC] " + EnumChatFormatting.RESET + build("[%rank_colored%] " + EnumChatFormatting.AQUA + "(%channel%)" + EnumChatFormatting.RESET + " %username%" + EnumChatFormatting.DARK_GRAY + " <%client%>" + EnumChatFormatting.GRAY + " %message%"));
        }else if (IRC.INSTANCE.tp.getValue() == IRC.type.Notify) {
            sendNotify(EnumChatFormatting.GOLD + "[IRC] " + EnumChatFormatting.RESET + build("[%rank_colored%] " + EnumChatFormatting.AQUA + "(%channel%)" + EnumChatFormatting.RESET + " %username%" + EnumChatFormatting.DARK_GRAY + " <%client%>" + EnumChatFormatting.GRAY + " %message%"));
        }
    }

    public String build(String template) {
        String formattedMessage = template;

        formattedMessage = formattedMessage.replace("%rank%", getRank().getName());
        formattedMessage = formattedMessage.replace("%rank_colored%", getRank().color() + getRank().getName() + EnumChatFormatting.RESET);
        formattedMessage = formattedMessage.replace("%channel%", getChannel().getName());
        formattedMessage = formattedMessage.replace("%chat%", getChat().getName());
        formattedMessage = formattedMessage.replace("%username%", getUsername());
        formattedMessage = formattedMessage.replace("%client%", getClient().getName());
        formattedMessage = formattedMessage.replace("%message%", getMessage());

        return formattedMessage;
    }
    public void sendNotify(String string){
        Notify.notifyList.add(new Notify.Notifys(string));
    }
}
