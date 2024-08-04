package me.nullpoint.mod.modules.deprecated;

import me.nullpoint.api.events.Event;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.mod.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;

@Deprecated
public class Godmode
extends Module {
    public Godmode() {
        super("Godmode", Category.Exploit);
    }

     public Entity entity;

    @Override
    public void onEnable() {
        if (nullCheck()) {
            disable();
            return;
        }
        if (mc.world != null && mc.player.getVehicle() != null) {
            entity = mc.player.getVehicle();
            mc.worldRenderer.reload();
            mc.player.dismountVehicle();
            mc.world.removeEntity(entity.getId(), Entity.RemovalReason.KILLED);
            mc.player.setPosition(mc.player.getPos().getX(), mc.player.getPos().getY() - 1, mc.player.getPos().getZ());
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround || event.getPacket() instanceof PlayerMoveC2SPacket.Full) {
            event.cancel();
        }
    }

    @EventHandler
    public void onPlayerWalkingUpdate(UpdateWalkingEvent event) {
        if (event.getStage() == Event.Stage.Pre) {
            if (entity == null) return;
            entity.copyPositionAndRotation(mc.player);
            entity.setYaw(mc.player.getYaw());
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), true));
            mc.player.networkHandler.sendPacket(new PlayerInputC2SPacket(MovementUtil.getMoveForward(), MovementUtil.getMoveStrafe(), false, false));
            mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(entity));
        }
    }
}
