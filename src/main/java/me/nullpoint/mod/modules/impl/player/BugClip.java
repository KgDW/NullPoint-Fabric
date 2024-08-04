package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class BugClip extends Module {
    public static BugClip INSTANCE;
    final SliderSetting delay =
            add(new SliderSetting("Delay", 100, 0, 500));
    private final BooleanSetting clipIn = add(new BooleanSetting("ClipIn", true));
    final Timer timer = new Timer();
    boolean cancelPacket = true;
    public BugClip() {
        super("BugClip", Category.Player);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        cancelPacket = false;
        if (clipIn.getValue()) {
            Direction f = mc.player.getHorizontalFacing();
            mc.player.setPosition(mc.player.getX() + f.getOffsetX() * 0.5, mc.player.getY(), mc.player.getZ() + f.getOffsetZ() * 0.5);
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true));
        } else {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true));
            mc.player.setPosition(roundToClosest(mc.player.getX(), Math.floor(mc.player.getX()) + 0.23, Math.floor(mc.player.getX()) + 0.77), mc.player.getY(), roundToClosest(mc.player.getZ(), Math.floor(mc.player.getZ()) + 0.23, Math.floor(mc.player.getZ()) + 0.77));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(roundToClosest(mc.player.getX(), Math.floor(mc.player.getX()) + 0.23, Math.floor(mc.player.getX()) + 0.77), mc.player.getY(), roundToClosest(mc.player.getZ(), Math.floor(mc.player.getZ()) + 0.23, Math.floor(mc.player.getZ()) + 0.77), true));
        }
        cancelPacket = true;
    }

    private double roundToClosest(double num, double low, double high) {
        double d1 = num - low;
        double d2 = high - num;

        if (d2 > d1) {
            return low;

        } else {
            return high;
        }
    }

    @Override
    public void onUpdate() {
        if (!insideBurrow()) {
            disable();
        }
    }

    @EventHandler
    public void onPacket(PacketEvent.Send event) {
        if (nullCheck()) return;
        if (cancelPacket && event.getPacket() instanceof PlayerMoveC2SPacket packet) {
            if (!insideBurrow()) {
                disable();
                return;
            }
            if (packet.changesLook()) {
                float packetYaw = packet.getYaw(0);
                float packetPitch = packet.getPitch(0);
                if (timer.passedMs(delay.getValue())) {
                    cancelPacket = false;
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() + 1337, mc.player.getZ(), packetYaw, packetPitch, false));
                    cancelPacket = true;
                    timer.reset();
                }
            }
            event.cancel();
        }
    }

    public boolean insideBurrow() {
        BlockPos playerBlockPos = EntityUtil.getPlayerPos(true);
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int yOffset = -1; yOffset <= 1; yOffset++) {
                for (int zOffset = -1; zOffset <= 1; zOffset++) {
                    BlockPos offsetPos = playerBlockPos.add(xOffset, yOffset, zOffset);
                    if (mc.world.getBlockState(offsetPos).getBlock() == Blocks.BEDROCK) {
                        if (mc.player.getBoundingBox().intersects(new Box(offsetPos))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}