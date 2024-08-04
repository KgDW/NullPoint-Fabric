package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.TimerEvent;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.combat.HoleKick;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.HashMap;
import java.util.Map;


public class FastFall extends Module {

    private final EnumSetting<Mode> mode =
            add(new EnumSetting<>("Mode", Mode.Fast));
    private final BooleanSetting noLag =
            add(new BooleanSetting("NoLag", true, v -> mode.getValue() == Mode.Fast));
    private final SliderSetting height =
            add(new SliderSetting("Height", 10, 1, 20, 0.5));

    private final Timer lagTimer = new Timer();

    private boolean useTimer;

    public FastFall() {
        super("FastFall", "Miyagi son simulator", Category.Movement);
    }

    @Override
    public void onDisable() {
        useTimer = false;
    }

    @Override
    public String getInfo() {
        return mode.getValue().name();
    }

    boolean onGround = false;
    @Override
    public void onUpdate() {
        if ((height.getValue() > 0 && (traceDown() > height.getValue()))
                || mc.player.isInsideWall()
                || mc.player.isSubmergedInWater()
                || mc.player.isInLava()
                || mc.player.isHoldingOntoLadder()
                || !lagTimer.passedMs(1000)
                || mc.player.isFallFlying()
                || Flight.INSTANCE.isOn()
                || nullCheck()) {
            return;
        }

        if (HoleKick.isInWeb(mc.player)) return;

        if (mc.player.isOnGround()) {

            if (mode.getValue() == Mode.Fast) {
                MovementUtil.setMotionY(MovementUtil.getMotionY() - (noLag.getValue() ? 0.62f : 1));
            }

            if (traceDown() != 0 && traceDown() <= height.getValue() && trace()) {
                MovementUtil.setMotionX(MovementUtil.getMotionX() * 0.05);
                MovementUtil.setMotionZ(MovementUtil.getMotionZ() * 0.05);
            }
        }

        if (mode.getValue() == Mode.Strict) {
            if (!mc.player.isOnGround()) {
                if (onGround) {
                    useTimer = true;
                }
                if (MovementUtil.getMotionY() >= 0) {
                    useTimer = false;
                }
                onGround = false;
            } else {
                useTimer = false;
                MovementUtil.setMotionY(-0.08);
                onGround = true;
            }
        } else {
            useTimer = false;
        }
    }

    @EventHandler
    public void onTimer(TimerEvent event) {
        if (nullCheck()) return;
        if (!mc.player.isOnGround() && useTimer) {
            event.set(2.5f);
        }
    }
    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (!nullCheck()) {
            if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
                lagTimer.reset();
            }
        }
    }

    private int traceDown() {
        int retval = 0;

        int y = (int) Math.round(mc.player.getY()) - 1;

        for (int tracey = y; tracey >= 0; tracey--) {

            HitResult trace = mc.world.raycast(new RaycastContext(
                    mc.player.getPos(),
                    new Vec3d(mc.player.getX(), tracey, mc.player.getZ()),
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    mc.player
            ));


            if (trace != null && trace.getType() == HitResult.Type.BLOCK) return retval;

            retval++;
        }
        return retval;
    }

    private boolean trace() {
        Box bbox = mc.player.getBoundingBox();
        Vec3d basepos = bbox.getCenter();

        double minX = bbox.minX;
        double minZ = bbox.minZ;
        double maxX = bbox.maxX;
        double maxZ = bbox.maxZ;

        Map<Vec3d, Vec3d> positions = new HashMap<>();

        positions.put(
                basepos,
                new Vec3d(basepos.x, basepos.y - 1, basepos.z));

        positions.put(
                new Vec3d(minX, basepos.y, minZ),
                new Vec3d(minX, basepos.y - 1, minZ));

        positions.put(
                new Vec3d(maxX, basepos.y, minZ),
                new Vec3d(maxX, basepos.y - 1, minZ));

        positions.put(
                new Vec3d(minX, basepos.y, maxZ),
                new Vec3d(minX, basepos.y - 1, maxZ));

        positions.put(
                new Vec3d(maxX, basepos.y, maxZ),
                new Vec3d(maxX, basepos.y - 1, maxZ));

        for (Vec3d key : positions.keySet()) {
            RaycastContext context = new RaycastContext(
                    key,
                    positions.get(key),
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    mc.player
            );

            BlockHitResult result = mc.world.raycast(context);

            if (result != null && result.getType() == HitResult.Type.BLOCK) {
                return false;
            }
        }

        BlockState state = mc.world.getBlockState(new BlockPosX(mc.player.getX(), mc.player.getY() - 1, mc.player.getZ()));

        return state.isAir();
    }

    private enum Mode {
        Fast,
        Strict
    }
}