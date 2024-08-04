package me.nullpoint.api.managers;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.mod.modules.impl.render.BreakESP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;

public class BreakManager implements Wrapper {
    public BreakManager() {Nullpoint.EVENT_BUS.subscribe(this);
    }
    public final HashMap<Integer, BreakData> breakMap = new HashMap<>();

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof BlockBreakingProgressS2CPacket packet) {
            if (packet.getPos() == null) return;
            BreakData breakData = new BreakData(packet.getPos(), packet.getEntityId());
            if (breakMap.containsKey(packet.getEntityId()) && breakMap.get(packet.getEntityId()).pos.equals(packet.getPos())) {
                return;
            }
            if (breakData.getEntity() == null) {
                return;
            }
            if (MathHelper.sqrt((float) breakData.getEntity().getEyePos().squaredDistanceTo(packet.getPos().toCenterPos())) > 8) {
                return;
            }
            breakMap.put(packet.getEntityId(), breakData);
        }
    }

    public boolean isMining(BlockPos pos) {
        boolean mining = false;

        for (BreakData breakData : new HashMap<>(breakMap).values()) {
            if (breakData.getEntity() == null) {
                continue;
            }
            if (breakData.getEntity().getEyePos().distanceTo(pos.toCenterPos()) > 8) {
                continue;
            }
            if (breakData.pos.equals(pos)) {
                mining = true;
                break;
            }
        }

        return mining;
    }
    public static class BreakData {
        public final BlockPos pos;
        public final int entityId;
        public final FadeUtils fade;
        public BreakData(BlockPos pos, int entityId) {
            this.pos = pos;
            this.entityId = entityId;
            this.fade = new FadeUtils((long) BreakESP.INSTANCE.animationTime.getValue());
        }

        public Entity getEntity() {
            Entity entity = mc.world.getEntityById(entityId);
            if (entity instanceof PlayerEntity) {
                return entity;
            }
            return null;
        }
    }
}
