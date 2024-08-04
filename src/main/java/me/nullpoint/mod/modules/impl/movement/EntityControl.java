package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.BoatMoveEvent;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.asm.accessors.IVec3d;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.UIModule;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.util.math.Vec3d;

public class EntityControl extends Module {
    public static EntityControl INSTANCE;

    public EntityControl() {
        super("EntityControl", Category.Movement);
        INSTANCE = this;
    }
    public final BooleanSetting fly = add(new BooleanSetting("Fly", true));

    public final SliderSetting speed = add(new SliderSetting("Speed", 5.0, 0.1, 50.0));
    private final SliderSetting verticalSpeed = add(new SliderSetting("VerticalSpeed", 6.0, 0, 20.0));
    public final SliderSetting fallSpeed = add(new SliderSetting("FallSpeed", 0.1, 0, 50.0));
    private final BooleanSetting noSync = add(new BooleanSetting("NoSync", false));

    @EventHandler
    public void onBoat(BoatMoveEvent event) {
        if (nullCheck() || !fly.getValue()) return;
        Entity boat = event.getBoat();
        if (boat == null) return;
        if (boat.getControllingPassenger() != mc.player) return;

        boat.setYaw(mc.player.getYaw());

        // Horizontal movement
        Vec3d vel = MovementUtil.getHorizontalVelocity(speed.getValue());
        double velX = vel.getX();
        double velY;
        double velZ = vel.getZ();

        if (mc.currentScreen instanceof ChatScreen || mc.currentScreen != null && UIModule.INSTANCE.isOff()) {
            velY = -fallSpeed.getValue() / 20;
        } else {
            boolean sprint = InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.sprintKey.getBoundKeyTranslationKey()).getCode());
            boolean jump = InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.jumpKey.getBoundKeyTranslationKey()).getCode());
            // Vertical movement
            if (jump) {
                if (sprint) {
                    velY = -fallSpeed.getValue() / 20;
                } else {
                    velY = verticalSpeed.getValue() / 20;
                }
            } else if (sprint) {
                velY = -verticalSpeed.getValue() / 20;
            } else {
                velY = -fallSpeed.getValue() / 20;
            }
        }

        // Apply velocity
        ((IVec3d) boat.getVelocity()).setX(velX);
        ((IVec3d) boat.getVelocity()).setY(velY);
        ((IVec3d) boat.getVelocity()).setZ(velZ);
    }

    @EventHandler
    public void onUpdateWalking(UpdateWalkingEvent event) {
        if (nullCheck() || !fly.getValue()) return;
        Entity boat = mc.player.getVehicle();
        if (boat == null) return;
        //if (boat.getControllingPassenger() != mc.player) return;

        boat.setYaw(mc.player.getYaw());

        // Horizontal movement
        Vec3d vel = MovementUtil.getHorizontalVelocity(speed.getValue());
        double velX = vel.getX();
        double velY;
        double velZ = vel.getZ();

        if (mc.currentScreen instanceof ChatScreen || mc.currentScreen != null && UIModule.INSTANCE.isOff()) {
            velY = -fallSpeed.getValue() / 20;
        } else {
            boolean sprint = InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.sprintKey.getBoundKeyTranslationKey()).getCode());
            boolean jump = InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.jumpKey.getBoundKeyTranslationKey()).getCode());
            // Vertical movement
            if (jump) {
                if (sprint) {
                    velY = -fallSpeed.getValue() / 20;
                } else {
                    velY = verticalSpeed.getValue() / 20;
                }
            } else if (sprint) {
                velY = -verticalSpeed.getValue() / 20;
            } else {
                velY = -fallSpeed.getValue() / 20;
            }
        }

        // Apply velocity
        ((IVec3d) boat.getVelocity()).setX(velX);
        ((IVec3d) boat.getVelocity()).setY(velY);
        ((IVec3d) boat.getVelocity()).setZ(velZ);
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof VehicleMoveS2CPacket && noSync.getValue()) {
            event.cancel();
        }
    }
}
