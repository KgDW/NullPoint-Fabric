package me.nullpoint.mod.modules.deprecated;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.BoatMoveEvent;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.asm.accessors.IVec3d;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

@Deprecated
public class BoatFly extends Module {
    private final ArrayList<VehicleMoveC2SPacket> Field2263 = new ArrayList<>();
    public BooleanSetting strict = add(new BooleanSetting("Strict", false));
    public BooleanSetting limit = add(new BooleanSetting("Limit", true));
    public BooleanSetting phase = add(new BooleanSetting("Phase", true));
    public BooleanSetting gravity = add(new BooleanSetting("Gravity", true));
    public BooleanSetting ongroundpacket = add(new BooleanSetting("OnGroundPacket", false));
    public BooleanSetting spoofpackets = add(new BooleanSetting("SpoofPackets", false));
    public BooleanSetting cancelrotations = add(new BooleanSetting("CancelRotations", true));
    public BooleanSetting cancel = add(new BooleanSetting("Cancel", true));
    public BooleanSetting remount = add(new BooleanSetting("Remount", true));
    public BooleanSetting stop = add(new BooleanSetting("Stop", false));
    public BooleanSetting ylimit = add(new BooleanSetting("yLimit", false));
    public BooleanSetting stopunloaded = add(new BooleanSetting("StopUnloaded", true));
    private final EnumSetting<Mode> mode  =add(new EnumSetting<>("Mode", Mode.Packet));
    private final SliderSetting speed = add(new SliderSetting("Speed", 2f, 0.0f, 45f));
    private final SliderSetting yspeed = add(new SliderSetting("YSpeed", 1f, 0.0f, 10f));
    private final SliderSetting glidespeed = add(new SliderSetting("GlideSpeed", 1f, 0.0f, 10f));
    private final SliderSetting timer = add(new SliderSetting("Timer", 1f, 0.0f, 5f));
    private final SliderSetting height = add(new SliderSetting("Height", 127f, 0.0f, 256f));
    private final SliderSetting offset = add(new SliderSetting("Offset", 0.1f, 0.0f, 10f));
    private final SliderSetting enableticks = add(new SliderSetting("EnableTicks", 10, 1, 100));
    private final SliderSetting waitticks = add(new SliderSetting("WaitTicks", 10, 1, 100));
    private int Field2264 = 0;
    private int Field2265 = 0;
    private boolean Field2266 = false;
    private boolean Field2267 = false;
    private boolean Field2268 = false;

    public static BoatFly INSTANCE;
    public BoatFly() {
        super("BoatFly", Category.Movement);
        INSTANCE = this;
    }

    public static double[] Method1330(double d) {
        double f = MovementUtil.getMoveForward();
        double f2 = MovementUtil.getMoveStrafe();
        float f3 = mc.player.prevYaw + (mc.player.getYaw() - mc.player.prevYaw) * mc.getTickDelta();
        if (f != 0.0f) {
            if (f2 > 0.0f) {
                f3 += (f > 0.0f ? -45 : 45);
            } else if (f2 < 0.0f) {
                f3 += (f > 0.0f ? 45 : -45);
            }
            f2 = 0.0f;
            if (f > 0.0f) {
                f = 1.0f;
            } else if (f < 0.0f) {
                f = -1.0f;
            }
        }
        double d2 = Math.sin(Math.toRadians(f3 + 90.0f));
        double d3 = Math.cos(Math.toRadians(f3 + 90.0f));
        double d4 = f * d * d3 + f2 * d * d2;
        double d5 = f * d * d2 - f2 * d * d3;
        return new double[]{d4, d5};
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            disable();
        }
    }

    @Override
    public void onDisable() {
        Nullpoint.TIMER.timer = (1.0f);
        this.Field2263.clear();
        this.Field2266 = false;
        if (mc.player == null) {
            return;
        }
        if ((this.phase.getValue()) && this.mode.getValue() == Mode.Motion) {
            if (mc.player.getVehicle() != null) {
                mc.player.getVehicle().noClip = false;
            }
            mc.player.noClip = false;
        }
        if (mc.player.getVehicle() != null) {
            mc.player.getVehicle().setNoGravity(false);
        }
        mc.player.setNoGravity(false);
    }


    private float Method2874() {
        this.Field2268 = !this.Field2268;
        return this.Field2268 ? (this.offset.getValueFloat()) : -(this.offset.getValueFloat());
    }

    private void Method2875(VehicleMoveC2SPacket VehicleMoveC2SPacket) {
        this.Field2263.add(VehicleMoveC2SPacket);
        mc.player.networkHandler.sendPacket(VehicleMoveC2SPacket);
    }

    private void Method2876(Entity entity) {
        double d = entity.getY();
        BlockPos blockPos = new BlockPosX(entity.getX(), (int) entity.getY(), entity.getZ());
        for (int i = 0; i < 255; ++i) {
            if (!mc.world.getBlockState(blockPos).isReplaceable() || mc.world.getBlockState(blockPos).getBlock() == Blocks.WATER) {
                ((IVec3d) entity.getPos()).setY(blockPos.getY() + 1);
                this.Method2875(new VehicleMoveC2SPacket(entity));
                ((IVec3d) entity.getPos()).setY(d);
                break;
            }
            blockPos = blockPos.add(0, -1, 0);
        }
    }

    @EventHandler
    public void onBoatMove(BoatMoveEvent event) {
        if (nullCheck()) {
            return;
        }
        BoatEntity boat = event.getBoat();
        if (boat.getControllingPassenger() != mc.player) return;

        if (this.phase.getValue() && this.mode.getValue() == Mode.Motion) {
            boat.noClip = true;
            boat.setNoGravity(true);
            mc.player.noClip = true;
        }
        if (!this.Field2267) {
            boat.setNoGravity(!(this.gravity.getValue()));
            mc.player.setNoGravity(!(this.gravity.getValue()));
        }
        if (this.stop.getValue()) {
            if (this.Field2264 > this.enableticks.getValue() && !this.Field2266) {
                this.Field2264 = 0;
                this.Field2266 = true;
                this.Field2265 = this.waitticks.getValueInt();
            }
            if (this.Field2265 > 0 && this.Field2266) {
                --this.Field2265;
                return;
            }
            if (this.Field2265 <= 0) {
                this.Field2266 = false;
            }
        }
        if ((!mc.world.isChunkLoaded(boat.getBlockPos().getX() >> 4, boat.getBlockPos().getZ() >> 4)) && this.stopunloaded.getValue()) {
            this.Field2267 = true;
            return;
        }
        if (this.timer.getValue() != 1.0f) {
            Nullpoint.TIMER.timer = (this.timer.getValueFloat());
        }
        boat.setYaw(mc.player.getYaw());
        double[] dArray = Method1330(this.speed.getValue());
        double d = boat.getX() + dArray[0];
        double d2 = boat.getZ() + dArray[1];
        double d3 = boat.getY();
        if ((!mc.world.isChunkLoaded((int) d >> 4, (int) d2 >> 4)) && (this.stopunloaded.getValue())) {
            this.Field2267 = true;
            return;
        }
        this.Field2267 = false;
        ((IVec3d) boat.getVelocity()).setY(-((this.glidespeed.getValue()) / 100.0f));
        if (this.mode.getValue() == Mode.Motion) {
            ((IVec3d) boat.getVelocity()).setX(dArray[0]);
            ((IVec3d) boat.getVelocity()).setZ(dArray[1]);
        }
        if (mc.options.jumpKey.isPressed()) {
            if (!(this.ylimit.getValue()) || boat.getY() <= (this.height.getValue())) {
                if (this.mode.getValue() == Mode.Motion) {
                    ((IVec3d) boat.getVelocity()).setY(boat.getVelocity().getY() + (this.yspeed.getValue()));
                } else {
                    d3 += (this.yspeed.getValue());
                }
            }
        } else if (mc.options.sneakKey.isPressed()) {
            if (this.mode.getValue() == Mode.Motion) {
                ((IVec3d) boat.getVelocity()).setY(boat.getVelocity().getY() + (-(this.yspeed.getValue())));
            } else {
                d3 += (-(this.yspeed.getValue()));
            }
        }
        if (!MovementUtil.isMoving()) {
            ((IVec3d) boat.getVelocity()).setX(0);
            ((IVec3d) boat.getVelocity()).setZ(0);
        }
        if ((this.ongroundpacket.getValue())) {
            this.Method2876(boat);
        }
        if (this.mode.getValue() != Mode.Motion) {
            boat.setPosition(d, d3, d2);
        }
        if (this.mode.getValue() == Mode.Packet) {
            this.Method2875(new VehicleMoveC2SPacket(boat));
        }
        if (this.strict.getValue()) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 0, 0, SlotActionType.CLONE, mc.player);
        }
        if ((this.spoofpackets.getValue())) {
            Vec3d vec3d = boat.getPos().add(0.0, this.Method2874(), 0.0);
            BoatEntity BoatEntity = new BoatEntity(mc.world, vec3d.x, vec3d.y, vec3d.z);
            BoatEntity.setYaw(boat.getYaw());
            BoatEntity.setPitch(boat.getPitch());
            this.Method2875(new VehicleMoveC2SPacket(BoatEntity));
        }
        if ((this.remount.getValue())) {
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(boat, false, Hand.MAIN_HAND));
        }
        ++this.Field2264;
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive eventNetworkPrePacketEvent) {
        if (nullCheck()) {
            return;
        }
        if (eventNetworkPrePacketEvent.getPacket() instanceof DisconnectS2CPacket) {
            this.toggle();
        }
        if (!mc.player.isRiding() || this.Field2267 || this.Field2266) {
            return;
        }
        if (eventNetworkPrePacketEvent.getPacket() instanceof VehicleMoveS2CPacket && mc.player.isRiding() && (this.cancel.getValue())) {
            eventNetworkPrePacketEvent.cancel();
        }
        if (eventNetworkPrePacketEvent.getPacket() instanceof PlayerPositionLookS2CPacket && mc.player.isRiding() && (this.cancel.getValue())) {
            eventNetworkPrePacketEvent.cancel();
        }
        if (eventNetworkPrePacketEvent.getPacket() instanceof EntityS2CPacket && (this.cancel.getValue())) {
            eventNetworkPrePacketEvent.cancel();
        }
        if (eventNetworkPrePacketEvent.getPacket() instanceof EntityAttachS2CPacket && (this.cancel.getValue())) {
            eventNetworkPrePacketEvent.cancel();
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send eventNetworkPostPacketEvent) {
        if (nullCheck()) {
            return;
        }
        if ((eventNetworkPostPacketEvent.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround && (this.cancelrotations.getValue()) || eventNetworkPostPacketEvent.getPacket() instanceof PlayerInputC2SPacket) && mc.player.isRiding()) {
            eventNetworkPostPacketEvent.cancel();
        }
        if (this.Field2267 && eventNetworkPostPacketEvent.getPacket() instanceof VehicleMoveC2SPacket) {
            eventNetworkPostPacketEvent.cancel();
        }
        if (!mc.player.isRiding() || this.Field2267 || this.Field2266) {
            return;
        }
        Entity entity = mc.player.getVehicle();
        if ((!mc.world.isChunkLoaded(entity.getBlockPos().getX() >> 4, entity.getBlockPos().getZ() >> 4)) && (this.stopunloaded.getValue())) {
            return;
        }
        if (eventNetworkPostPacketEvent.getPacket() instanceof VehicleMoveC2SPacket && (this.limit.getValue()) && this.mode.getValue() == Mode.Packet) {
            VehicleMoveC2SPacket VehicleMoveC2SPacket = eventNetworkPostPacketEvent.getPacket();
            if (this.Field2263.contains(VehicleMoveC2SPacket)) {
                this.Field2263.remove(VehicleMoveC2SPacket);
            } else {
                eventNetworkPostPacketEvent.cancel();
            }
        }
    }

    public enum Mode {
        Packet, Motion
    }
}