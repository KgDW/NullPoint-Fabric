package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.eventbus.EventPriority;
import me.nullpoint.api.events.impl.MoveEvent;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.TravelEvent;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class PacketElytra extends Module {
    public PacketElytra() {
        super("PacketElytra", Category.Movement);
    }

    private final BooleanSetting stopOnGround = add(new BooleanSetting("StopOnGround", false));
    private final BooleanSetting infDurability = add(new BooleanSetting("InfDurability", true));
    private final SliderSetting speed = add(new SliderSetting("Speed", 1.55f, 0.1f, 10f));
    private final BooleanSetting accelerate = add(new BooleanSetting("Acceleration", false));
    private final SliderSetting factor = add(new SliderSetting("Factor", 9f, 0f, 100f));

    private final Timer pingTimer = new Timer();

    private float acceleration;

    @Override
    public void onEnable() {
        if (nullCheck()) return;

        acceleration = 0;

        pingTimer.reset();
    }

    @EventHandler
    public void modifyVelocity(TravelEvent e) {
    }

    @EventHandler
    public void onSync(UpdateWalkingEvent e) {
        if (e.isPost()) return;
        if ((!isBoxCollidingGround() || !stopOnGround.getValue()) && mc.player.getInventory().getStack(38).getItem() == Items.ELYTRA) {
            if (infDurability.getValue() || !mc.player.isFallFlying())
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

    @Override
    public void onDisable() {
        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().setFlySpeed(0.05F);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(MoveEvent e) {
        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().setFlySpeed(0.05F);

        if ((isBoxCollidingGround() && stopOnGround.getValue()) || mc.player.getInventory().getStack(38).getItem() != Items.ELYTRA)
            return;

        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlySpeed((speed.getValueFloat() / 15f) * (accelerate.getValue() ? Math.min((acceleration += (float) factor.getValue()) / 100.0f, 1.0f) : 1f));

        if (mc.player.age % 3 == 0) {
            e.setY(0);
            e.setX(0);
            e.setZ(0);
            return;
        }

        if (Math.abs(e.getX()) < 0.05)
            e.setX(0);

        if (Math.abs(e.getZ()) < 0.05)
            e.setZ(0);

        e.setY(-4.000355602329364E-12);


        if (mc.player.horizontalCollision && mc.player.age % 2 == 0)
            e.setY(-0.07840000152587923);

        if (!MovementUtil.isMoving() && Math.abs(e.getX()) < 0.121 && Math.abs(e.getX()) < 0.121) {
            float angleToRad = (float) Math.toRadians(4.5 * (mc.player.age % 80));
            e.setX(Math.sin(angleToRad) * 0.12);
            e.setZ(Math.cos(angleToRad) * 0.12);
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof EntityTrackerUpdateS2CPacket pac && pac.id() == mc.player.getId())
            e.cancel();

        if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
            acceleration = 0;
            pingTimer.reset();
        }
    }

    private boolean isBoxCollidingGround() {
        return mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.25, 0.0, -0.25).offset(0.0, -0.3, 0.0)).iterator().hasNext();
    }
}