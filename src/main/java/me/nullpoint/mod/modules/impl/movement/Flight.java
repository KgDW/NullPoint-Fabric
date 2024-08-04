package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.MoveEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

import static me.nullpoint.api.utils.entity.MovementUtil.*;

public class Flight extends Module {
    public static Flight INSTANCE;

    public Flight() {
        super("Flight", "me", Category.Movement);
        INSTANCE = this;
    }


    public final SliderSetting speed = add(new SliderSetting("Speed", 1.0f, 0.1f, 10.0f));
    private final SliderSetting sneakDownSpeed = add(new SliderSetting("DownSpeed", 1.0F, 0.1F, 10.0F));
    private final SliderSetting upSpeed = add(new SliderSetting("UpSpeed", 1.0F, 0.1F, 10.0F));
    public final SliderSetting downFactor = add(new SliderSetting("DownFactor", 0f, 0.0f, 1f, 0.000001f));
    private MoveEvent event;
    @EventHandler
    public void onMove(MoveEvent event) {
        if (nullCheck()) return;
        this.event = event;
        if (!(mc.options.sneakKey.isPressed() && mc.player.input.jumping)) {
            if (mc.options.sneakKey.isPressed()) {
                setY(-sneakDownSpeed.getValue());
            }
            else if (mc.player.input.jumping) {
                setY(upSpeed.getValue());
            } else {
                setY(-downFactor.getValue());
            }
        } else {
            setY(0);
        }
        double[] dir = directionSpeed(speed.getValue());
        setX(dir[0]);
        setZ(dir[1]);
    }

    private void setX(double f) {
        event.setX(f);
        setMotionX(f);
    }

    private void setY(double f) {
        event.setY(f);
        setMotionY(f);
    }

    private void setZ(double f) {
        event.setZ(f);
        setMotionZ(f);
    }
}
