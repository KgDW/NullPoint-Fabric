package me.nullpoint.mod.modules.impl.movement;


import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.eventbus.EventPriority;
import me.nullpoint.api.events.impl.TimerEvent;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.combat.HoleKick;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class FastWeb extends Module {
    public static FastWeb INSTANCE;
    public final EnumSetting<Mode> mode =
            add(new EnumSetting<>("Mode", Mode.Vanilla));
    private final SliderSetting fastSpeed =
            add(new SliderSetting("Speed", 3.0, 0.0, 8.0, v -> mode.getValue() == Mode.Vanilla || mode.getValue() == Mode.Strict));
    public final SliderSetting xZSlow =
            add(new SliderSetting("XZSpeed", 25, 0.0, 100, 0.1, v -> mode.getValue() == Mode.Custom).setSuffix("%"));
    public final SliderSetting ySlow =
            add(new SliderSetting("YSpeed", 100, 0.0, 100, 0.1, v -> mode.getValue() == Mode.Custom).setSuffix("%"));
    public final BooleanSetting onlySneak = add(new BooleanSetting("OnlySneak", true));

    public FastWeb() {
        super("FastWeb", "So you don't need to keep timer on keybind", Category.Movement);
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return mode.getValue().name();
    }

    public boolean isWorking() {
        return work;
    }
    private boolean work = false;
    @Override
    public void onUpdate() {
        work = (!mc.player.isOnGround()) && (mc.options.sneakKey.isPressed() || !onlySneak.getValue()) && HoleKick.isInWeb(mc.player);
        if (work && mode.getValue() == Mode.Vanilla) {
            MovementUtil.setMotionY(MovementUtil.getMotionY() - fastSpeed.getValue());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onTimer(TimerEvent event) {
        if (work && mode.getValue() == Mode.Strict) {
            event.set(fastSpeed.getValueFloat());
        }
    }

    public enum Mode {
        Vanilla,
        Strict,
        Custom,
        Ignore
    }
}
