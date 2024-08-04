package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.mod.modules.Module;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;

public class NoServerRP extends Module {
    public NoServerRP() {
        super("NoServerRP", Category.Misc);
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ResourcePackSendS2CPacket) {
            mc.player.networkHandler.sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), ResourcePackStatusC2SPacket.Status.ACCEPTED));
            event.cancel();
        }
    }
}
