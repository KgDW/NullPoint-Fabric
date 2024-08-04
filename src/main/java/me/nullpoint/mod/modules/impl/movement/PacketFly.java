package me.nullpoint.mod.modules.impl.movement;

import io.netty.util.internal.ConcurrentSet;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.MoveEvent;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class PacketFly extends Module {
    public PacketFly() {
        super("PacketFly", "PacketFly", Category.Movement);
    }

    public final BooleanSetting flight =
            add(new BooleanSetting("Flight", true).setParent());
    public final SliderSetting flightMode =
            add(new SliderSetting("FMode", 0, 0, 1, v -> flight.isOpen()));
    public final SliderSetting antiFactor =
            add(new SliderSetting("AntiFactor", 1.0, 0.1, 3.0));
    public final SliderSetting extraFactor =
            add(new SliderSetting("ExtraFactor", 1.0, 0.1, 3.0));
    public final BooleanSetting strafeFactor =
            add(new BooleanSetting("StrafeFactor", true));
    public final SliderSetting loops =
            add(new SliderSetting("Loops", 1, 1, 10));
    public final BooleanSetting antiRotation =
            add(new BooleanSetting("AntiRotation", false));
    public final BooleanSetting setID =
            add(new BooleanSetting("SetID", true));
    public final BooleanSetting setMove =
            add(new BooleanSetting("SetMove", false));
    public final BooleanSetting nocliperino =
            add(new BooleanSetting("NoClip", false));
    public final BooleanSetting sendTeleport =
            add(new BooleanSetting("Teleport", true));
    public final BooleanSetting setPos =
            add(new BooleanSetting("SetPos", false));
    public final BooleanSetting invalidPacket =
            add(new BooleanSetting("InvalidPacket", true));
    private final Set<PlayerMoveC2SPacket> packets = new ConcurrentSet<>();
    private int flightCounter = 0;
    private int teleportID = 0;

    @EventHandler
    public void onUpdateWalkingPlayer(UpdateWalkingEvent event) {
        if (nullCheck()) return;
        if (event.isPost()) {
            return;
        }
        mc.player.setVelocity(0.0, 0.0, 0.0);
        double speed;
        boolean checkCollisionBoxes = this.checkHitBoxes();
        speed = mc.player.input.jumping && (checkCollisionBoxes || !MovementUtil.isMoving()) ? (this.flight.getValue() && !checkCollisionBoxes ? (this.flightMode.getValue() == 0 ? (this.resetCounter(10) ? -0.032 : 0.062) : (this.resetCounter(20) ? -0.032 : 0.062)) : 0.062) : (mc.player.input.sneaking ? -0.062 : (!checkCollisionBoxes ? (this.resetCounter(4) ? (this.flight.getValue() ? -0.04 : 0.0) : 0.0) : 0.0));
        if (checkCollisionBoxes && MovementUtil.isMoving() && speed != 0.0) {
            speed /= this.antiFactor.getValue();
        }
        double[] strafing = this.getMotion(this.strafeFactor.getValue() && checkCollisionBoxes ? 0.031 : 0.26);
        for (int i = 1; i < this.loops.getValue() + 1; ++i) {
            MovementUtil.setMotionX(strafing[0] * (double) i * this.extraFactor.getValue());
            MovementUtil.setMotionY(speed * (double) i);
            MovementUtil.setMotionZ(strafing[1] * (double) i * this.extraFactor.getValue());
            this.sendPackets(MovementUtil.getMotionX(), MovementUtil.getMotionY(), MovementUtil.getMotionZ(), this.sendTeleport.getValue());
        }
    }

    @EventHandler
    public void onMove(MoveEvent event) {
        if (nullCheck()) return;
        if (this.setMove.getValue() && this.flightCounter != 0) {
            event.setX(MovementUtil.getMotionX());
            event.setY(MovementUtil.getMotionY());
            event.setZ(MovementUtil.getMotionZ());
            if (this.nocliperino.getValue() && this.checkHitBoxes()) {
                mc.player.noClip = true;
            }
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (nullCheck()) return;
        PlayerMoveC2SPacket packet;
        if (event.getPacket() instanceof PlayerMoveC2SPacket && !this.packets.remove(packet = event.getPacket())) {
            if (event.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround && !antiRotation.getValue()) return;
            event.cancel();
        }
    }

    @EventHandler
    public void onReceivePacket(PacketEvent.Receive event) {
        if (nullCheck()) return;
        if (event.isCancelled()) return;
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            PlayerPositionLookS2CPacket packet = event.getPacket();
            if (this.setID.getValue()) {
                this.teleportID = packet.getTeleportId();
            }
        }
    }

    private boolean checkHitBoxes() {
        return mc.world.canCollide(mc.player, mc.player.getBoundingBox().expand(-0.0625, -0.0625, -0.0625));
    }

    private boolean resetCounter(int counter) {
        if (++this.flightCounter >= counter) {
            this.flightCounter = 0;
            return true;
        }
        return false;
    }

    private double[] getMotion(double speed) {
        float moveForward = MovementUtil.getMoveForward();
        float moveStrafe = MovementUtil.getMoveStrafe();
        float rotationYaw = mc.player.prevYaw + (mc.player.getYaw() - mc.player.prevYaw) * mc.getTickDelta();
        if (moveForward != 0.0f) {
            if (moveStrafe > 0.0f) {
                rotationYaw += (float) (moveForward > 0.0f ? -45 : 45);
            } else if (moveStrafe < 0.0f) {
                rotationYaw += (float) (moveForward > 0.0f ? 45 : -45);
            }
            moveStrafe = 0.0f;
            if (moveForward > 0.0f) {
                moveForward = 1.0f;
            } else if (moveForward < 0.0f) {
                moveForward = -1.0f;
            }
        }
        double posX = (double) moveForward * speed * -Math.sin(Math.toRadians(rotationYaw)) + (double) moveStrafe * speed * Math.cos(Math.toRadians(rotationYaw));
        double posZ = (double) moveForward * speed * Math.cos(Math.toRadians(rotationYaw)) - (double) moveStrafe * speed * -Math.sin(Math.toRadians(rotationYaw));
        return new double[]{posX, posZ};
    }

    private void sendPackets(double x, double y, double z, boolean teleport) {
        Vec3d vec = new Vec3d(x, y, z);
        Vec3d position = mc.player.getPos().add(vec);
        Vec3d outOfBoundsVec = this.outOfBoundsVec(position);
        this.packetSender(new PlayerMoveC2SPacket.PositionAndOnGround(position.x, position.y, position.z, mc.player.isOnGround()));
        if (this.invalidPacket.getValue()) {
            this.packetSender(new PlayerMoveC2SPacket.PositionAndOnGround(outOfBoundsVec.x, outOfBoundsVec.y, outOfBoundsVec.z, mc.player.isOnGround()));
        }
        if (this.setPos.getValue()) {
            mc.player.setPosition(position.x, position.y, position.z);
        }
        this.teleportPacket(teleport);
    }

    private void teleportPacket(boolean shouldTeleport) {
        if (shouldTeleport) {
            mc.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(++this.teleportID));
        }
    }

    private Vec3d outOfBoundsVec(Vec3d position) {
        return position.add(0.0, 1337.0, 0.0);
    }

    private void packetSender(PlayerMoveC2SPacket packet) {
        this.packets.add(packet);
        mc.player.networkHandler.sendPacket(packet);
    }
}

