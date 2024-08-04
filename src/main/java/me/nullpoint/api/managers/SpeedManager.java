package me.nullpoint.api.managers;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.utils.Wrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;

public class SpeedManager implements Wrapper {
    public double speedometerCurrentSpeed;

    public final HashMap<PlayerEntity, Double> playerSpeeds = new HashMap();

    public SpeedManager(){
        Nullpoint.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    public void updateWalking(UpdateWalkingEvent event) {
        updateValues();
    }

    public void updateValues() {
        double distTraveledLastTickX = mc.player.getX() - mc.player.prevX;
        double distTraveledLastTickZ = mc.player.getZ() - mc.player.prevZ;
        speedometerCurrentSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
        updatePlayers();
    }

    public void updatePlayers() {
        for (PlayerEntity player : SpeedManager.mc.world.getPlayers()) {
            if (!(SpeedManager.mc.player.distanceTo(player) < 400.0)) continue;
            double distTraveledLastTickX = player.getX() - player.prevX;
            double distTraveledLastTickZ = player.getZ() - player.prevZ;
            double playerSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
            this.playerSpeeds.put(player, playerSpeed);
        }
    }

    public double getPlayerSpeed(PlayerEntity player) {
        if (this.playerSpeeds.get(player) == null) {
            return 0.0;
        }
        return this.turnIntoKpH(this.playerSpeeds.get(player));
    }

    public double getSpeedKpH() {
        double speedometerkphdouble = turnIntoKpH(speedometerCurrentSpeed);
        speedometerkphdouble = (double) Math.round(10.0 * speedometerkphdouble) / 10.0;
        return speedometerkphdouble;
    }

    public double turnIntoKpH(double input) {
        return MathHelper.sqrt((float) input) * 71.2729367892;
    }
}
