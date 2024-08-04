package me.nullpoint.socket.network.packet.impl.operation;

import me.nullpoint.socket.network.buffer.PacketBuffer;
import me.nullpoint.socket.network.handler.ClientHandler;
import me.nullpoint.socket.network.packet.Packet;
import me.nullpoint.socket.enums.Operation;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author DreamDev
 * @since 4/10/2024
 */
public class OperationPacket extends Packet {
    private String initiatorUsername;
    private String targetUsername;
    private String message;
    private Operation operation;

    public OperationPacket() {
    }

    public OperationPacket(String initiatorUsername, String targetUsername, String message, Operation operation) {
        this.initiatorUsername = initiatorUsername;
        this.targetUsername = targetUsername;
        this.message = message;
        this.operation = operation;
    }

    public String getInitiatorUsername() {
        return initiatorUsername;
    }

    public void setInitiatorUsername(String initiatorUsername) {
        this.initiatorUsername = initiatorUsername;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeString(initiatorUsername);
        buf.writeString(targetUsername);
        buf.writeString(message);
        buf.writeEnum(operation);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.initiatorUsername = buf.readString();
        this.targetUsername = buf.readString();
        this.message = buf.readString();
        this.operation = buf.readEnum(Operation.class);
    }

    @Override
    public void handler(ChannelHandlerContext ctx, ClientHandler handler) {
        getOperation().handler(getMessage());
    }
}