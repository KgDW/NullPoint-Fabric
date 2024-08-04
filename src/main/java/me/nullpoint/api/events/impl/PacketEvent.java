package me.nullpoint.api.events.impl;

import me.nullpoint.api.events.Event;
import net.minecraft.network.packet.Packet;

public class PacketEvent extends Event {

    private final Packet<?> packet;
    public PacketEvent(Packet<?> packet) {
        super(Stage.Pre);
        this.packet = packet;
    }
    public <T extends Packet<?>> T getPacket() {
        return (T) packet;
    }
    public static class Send extends PacketEvent {
        public Send(Packet<?> packet) {
            super(packet);
        }
    }

    public static class Receive extends PacketEvent {
        public Receive(Packet<?> packet) {
            super(packet);
        }

    }
}
