package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.eventbus.EventPriority;
import me.nullpoint.api.events.impl.MoveEvent;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class Strafe extends Module {
    private final BooleanSetting inWater =
            add(new BooleanSetting("InWater", false));
    private final BooleanSetting inBlock =
            add(new BooleanSetting("InBlock", false));
    private final SliderSetting strafeSpeed =
            add(new SliderSetting("Speed", 287.3, 100.0, 1000.0, 0.1));
    private final BooleanSetting explosions =
            add(new BooleanSetting("Explosions", false));
    private final BooleanSetting velocity =
            add(new BooleanSetting("Velocity", true));
    private final SliderSetting multiplier =
            add(new SliderSetting("H-Factor", 1.0f, 0.0f, 5.0f, 0.01));
    private final SliderSetting vertical =
            add(new SliderSetting("V-Factor", 1.0f, 0.0f, 5.0f, 0.01));
    private final SliderSetting coolDown =
            add(new SliderSetting("CoolDown", 1000, 0, 5000, 1));
    private final SliderSetting lagTime =
            add(new SliderSetting("LagTime", 500, 0, 1000, 1));
    private final BooleanSetting slow =
            add(new BooleanSetting("Slowness", false));
    private final Timer expTimer = new Timer();
    private final Timer lagTimer = new Timer();
    private boolean stop;
    private double speed;
    private double distance;

    private int stage;
    private double lastExp;
    private boolean boost;

    public Strafe() {
        super("Strafe", Category.Movement);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            speed = MovementUtil.getSpeed(false);
            distance = MovementUtil.getDistance2D();
        }

        stage = 4;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void invoke(PacketEvent.Receive event) {
        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet) {
            if (mc.player != null
                    && packet.getId() == mc.player.getId()
                    && this.velocity.getValue()) {
                double speed = Math.sqrt(
                        packet.getVelocityX() * packet.getVelocityX()
                                + packet.getVelocityZ() * packet.getVelocityZ())
                        / 8000.0;

                this.lastExp = this.expTimer
                        .passedMs(this.coolDown.getValueInt())
                        ? speed
                        : (speed - this.lastExp);

                if (this.lastExp > 0) {
                    this.expTimer.reset();
                    mc.executeTask(() ->
                    {
                        this.speed +=
                                this.lastExp * this.multiplier.getValue();

                        this.distance +=
                                this.lastExp * this.multiplier.getValue();

                        if (MovementUtil.getMotionY() > 0
                                && this.vertical.getValue() != 0) {
                            MovementUtil.setMotionY(MovementUtil.getMotionY() * this.vertical.getValue());
                        }
                    });
                }
            }
        } else if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            lagTimer.reset();
            if (mc.player != null) {
                this.distance = 0.0;
            }

            this.speed = 0.0;
            this.stage = 4;
        } else if (event.getPacket() instanceof ExplosionS2CPacket packet) {

            if (this.explosions.getValue()
                    && MovementUtil.isMoving())
            {
                if (mc.player.squaredDistanceTo(packet.getX(), packet.getY(), packet.getZ()) < 200)
                {
                    double speed = Math.sqrt(
                            Math.abs(packet.getPlayerVelocityX() * packet.getPlayerVelocityX())
                                    + Math.abs(packet.getPlayerVelocityZ() * packet.getPlayerVelocityZ()));
                    this.lastExp = this.expTimer
                            .passedMs(this.coolDown.getValueInt())
                            ? speed
                            : (speed - this.lastExp);

                    if (this.lastExp > 0)
                    {
                        this.expTimer.reset();

                        this.speed +=
                                this.lastExp * this.multiplier.getValue();

                        this.distance +=
                                this.lastExp * this.multiplier.getValue();

                        if (MovementUtil.getMotionY() > 0) {
                            MovementUtil.setMotionY(MovementUtil.getMotionY() * this.vertical.getValue());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onUpdateWalking(UpdateWalkingEvent event) {
        if (!MovementUtil.isMoving()) {
            MovementUtil.setMotionX(0);
            MovementUtil.setMotionZ(0);
        }
        this.distance = MovementUtil.getDistance2D();
    }

    @EventHandler
    public void invoke(MoveEvent event) {
        if (!this.inWater.getValue()
                && (mc.player.isSubmergedInWater() || mc.player.isTouchingWater())
                || mc.player.isHoldingOntoLadder() || !inBlock.getValue() && EntityUtil.isInsideBlock()) {
            this.stop = true;
            return;
        }

        if (this.stop) {
            this.stop = false;
            return;
        }

        if (!MovementUtil.isMoving() || HoleSnap.INSTANCE.isOn()) {
            return;
        }

        if (mc.player.isFallFlying()) return;
        if (!lagTimer.passedMs(this.lagTime.getValueInt())) {
            return;
        }

        if (this.stage == 1 && MovementUtil.isMoving()) {
            double yMotion = 0.3999 + MovementUtil.getJumpSpeed();
            MovementUtil.setMotionY(yMotion);
            event.setY(yMotion);
            this.speed = this.speed * (this.boost ? 1.6835 : 1.395);
        } else if (this.stage == 3) {
            this.speed = this.distance - 0.66
                    * (this.distance - MovementUtil.getSpeed(this.slow.getValue(), this.strafeSpeed.getValue() / 1000));

            this.boost = !this.boost;
        } else {
            if ((mc.world.canCollide(null,
                    mc.player
                            .getBoundingBox()
                            .offset(0.0, MovementUtil.getMotionY(), 0.0))
                    || mc.player.collidedSoftly)
                    && this.stage > 0) {
                this.stage = MovementUtil.isMoving() ? 1 : 0;
            }

            this.speed = this.distance - this.distance / 159.0;
        }

        this.speed = Math.min(this.speed, 10);
        this.speed = Math.max(this.speed, MovementUtil.getSpeed(this.slow.getValue(), this.strafeSpeed.getValue() / 1000));
        double n = MovementUtil.getMoveForward();
        double n2 = MovementUtil.getMoveStrafe();
        double n3 = mc.player.getYaw();
        if (n == 0.0 && n2 == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
        } else if (n != 0.0 && n2 != 0.0) {
            n *= Math.sin(0.7853981633974483);
            n2 *= Math.cos(0.7853981633974483);
        }
        event.setX((n * this.speed * -Math.sin(Math.toRadians(n3)) + n2 * this.speed * Math.cos(Math.toRadians(n3))) * 0.99);
        event.setZ((n * this.speed * Math.cos(Math.toRadians(n3)) - n2 * this.speed * -Math.sin(Math.toRadians(n3))) * 0.99);

        if (MovementUtil.isMoving()) {
            this.stage++;
        }
    }
}