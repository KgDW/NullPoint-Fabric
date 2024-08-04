package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.TravelEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.MovementType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static me.nullpoint.api.utils.entity.MovementUtil.*;

public class ElytraFly extends Module {
    public static ElytraFly INSTANCE;

    public ElytraFly() {
        super("ElytraFly", Category.Movement);
        INSTANCE = this;
    }


    private final BooleanSetting instantFly = add(new BooleanSetting("InstantFly", true));
    public final SliderSetting upPitch = add(new SliderSetting("UpPitch", 0.0f, 0.0f, 90.0f));
    public final SliderSetting upFactor = add(new SliderSetting("UpFactor", 1.0f, 0.0f, 10.0f));
    public final SliderSetting downFactor = add(new SliderSetting("DownFactor", 1.0f, 0.0f, 10.0f));
    public final SliderSetting speed = add(new SliderSetting("Speed", 1.0f, 0.1f, 10.0f));
    private final SliderSetting sneakDownSpeed = add(new SliderSetting("DownSpeed", 1.0F, 0.1F, 10.0F));
    private final BooleanSetting boostTimer = add(new BooleanSetting("Timer", true));
    public final BooleanSetting speedLimit = add(new BooleanSetting("SpeedLimit", true));
    public final SliderSetting maxSpeed = add(new SliderSetting("MaxSpeed", 2.5f, 0.1f, 10.0f, v -> speedLimit.getValue()));
    public final BooleanSetting noDrag = add(new BooleanSetting("NoDrag", false));
    private final SliderSetting timeout = add(new SliderSetting("Timeout", 0.5F, 0.1F, 1F));

    private boolean hasElytra = false;
    private final Timer instantFlyTimer = new Timer();
    private final Timer strictTimer = new Timer();
    private boolean hasTouchedGround = false;


    @Override
    public String getInfo() {
        return "Control";
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            if (!mc.player.isCreative()) mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().flying = false;
        }
        hasElytra = false;
    }

    @Override
    public void onDisable() {
        Nullpoint.TIMER.reset();
        hasElytra = false;
        if (mc.player != null) {
            if (!mc.player.isCreative()) mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().flying = false;
        }
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        if (mc.player.isOnGround()) {
            hasTouchedGround = true;
        }
        for (ItemStack is : mc.player.getArmorItems()) {
            if (is.getItem() instanceof ElytraItem) {
                hasElytra = true;
                break;
            } else {
                hasElytra = false;
            }
        }
        if (strictTimer.passedMs(1500) && !strictTimer.passedMs(2000) || EntityUtil.isElytraFlying() && Nullpoint.TIMER.get() == 0.3) {
            Nullpoint.TIMER.reset();
        }
        if (!mc.player.isFallFlying()) {
            if (hasTouchedGround && boostTimer.getValue() && !mc.player.isOnGround()) {
                Nullpoint.TIMER.set(0.3f);
            }
            if (!mc.player.isOnGround() && instantFly.getValue() && mc.player.getVelocity().getY() < 0D) {
                if (!instantFlyTimer.passedMs((long) (1000 * timeout.getValue())))
                    return;
                instantFlyTimer.reset();
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                hasTouchedGround = false;
                strictTimer.reset();
            }
        }
    }

    protected final Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    public final Vec3d getRotationVec(float tickDelta) {
        return this.getRotationVector(-upPitch.getValueFloat(), mc.player.getYaw(tickDelta));
    }

    @EventHandler
    public void onMove(TravelEvent event) {
        if (nullCheck() || !hasElytra || !mc.player.isFallFlying()) return;
        Vec3d lookVec = getRotationVec(mc.getTickDelta());
        double lookDist = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
        double motionDist = Math.sqrt(getX() * getX() + getZ() * getZ());
        if (mc.options.sneakKey.isPressed()) {
            setY(-sneakDownSpeed.getValue());
        } else if (!mc.player.input.jumping) {
            setY(-0.00000000000003D * downFactor.getValue());
        }
        if (mc.player.input.jumping) {
            if (motionDist > upFactor.getValue() / upFactor.getMaximum()) {
                double rawUpSpeed = motionDist * 0.01325D;
                setY(getY() + rawUpSpeed * 3.2D);
                setX(getX() - lookVec.x * rawUpSpeed / lookDist);
                setZ(getZ() - lookVec.z * rawUpSpeed / lookDist);
            } else {
                double[] dir = directionSpeed(speed.getValue());
                setX(dir[0]);
                setZ(dir[1]);
            }
        }
        if (lookDist > 0.0D) {
            setX(getX() + (lookVec.x / lookDist * motionDist - getX()) * 0.1D);
            setZ(getZ() + (lookVec.z / lookDist * motionDist - getZ()) * 0.1D);
        }
        if (!mc.player.input.jumping) {
            double[] dir = directionSpeed(speed.getValue());
            setX(dir[0]);
            setZ(dir[1]);
        }
        if (!noDrag.getValue()) {
            setY(getY() * 0.9900000095367432D);
            setX(getX() * 0.9800000190734863D);
            setZ(getZ() * 0.9900000095367432D);
        }
        double finalDist = Math.sqrt(getX() * getX() + getZ() * getZ());
        if (speedLimit.getValue() && finalDist > maxSpeed.getValue()) {
            setX(getX() * maxSpeed.getValue() / finalDist);
            setZ(getZ() * maxSpeed.getValue() / finalDist);
        }
        event.cancel();
        mc.player.move(MovementType.SELF, mc.player.getVelocity());
    }

    private void setX(double f) {
        setMotionX(f);
    }

    private void setY(double f) {
        setMotionY(f);
    }

    private void setZ(double f) {
        setMotionZ(f);
    }

    private double getX() {
        return getMotionX();
    }

    private double getY() {
        return getMotionY();
    }

    private double getZ() {
        return getMotionZ();
    }
}
