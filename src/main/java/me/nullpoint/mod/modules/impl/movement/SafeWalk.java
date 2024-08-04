package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.eventbus.EventPriority;
import me.nullpoint.api.events.impl.MoveEvent;
import me.nullpoint.mod.modules.Module;

public class SafeWalk extends Module {
    public SafeWalk() {
        super("SafeWalk", "stop at the edge", Category.Movement);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMove(MoveEvent event) {
        double x = event.getX();
        double y = event.getY();
        double z = event.getZ();
        if (mc.player.isOnGround()) {
            double increment = 0.05;
            while (x != 0.0 && this.isOffsetBBEmpty(x, -1.0, 0.0)) {
                if (x < increment && x >= -increment) {
                    x = 0.0;
                    continue;
                }
                if (x > 0.0) {
                    x -= increment;
                    continue;
                }
                x += increment;
            }
            while (z != 0.0 && this.isOffsetBBEmpty(0.0, -1.0, z)) {
                if (z < increment && z >= -increment) {
                    z = 0.0;
                    continue;
                }
                if (z > 0.0) {
                    z -= increment;
                    continue;
                }
                z += increment;
            }
            while (x != 0.0 && z != 0.0 && this.isOffsetBBEmpty(x, -1.0, z)) {
                x = x < increment && x >= -increment ? 0.0 : (x > 0.0 ? x - increment : x + increment);
                if (z < increment && z >= -increment) {
                    z = 0.0;
                    continue;
                }
                if (z > 0.0) {
                    z -= increment;
                    continue;
                }
                z += increment;
            }
        }
        event.setX(x);
        event.setY(y);
        event.setZ(z);
    }

    public boolean isOffsetBBEmpty(double offsetX, double offsetY, double offsetZ) {
        return !mc.world.canCollide(mc.player, mc.player.getBoundingBox().offset(offsetX, offsetY, offsetZ));
    }
}
