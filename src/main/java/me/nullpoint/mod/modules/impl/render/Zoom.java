package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.Render3DEvent;
import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class Zoom extends Module {
    public static Zoom INSTANCE;
    public double currentFov;
    final SliderSetting animSpeed = add(new SliderSetting("AnimSpeed", 0.1, 0, 1, 0.01));
    final SliderSetting fov = add(new SliderSetting("Fov", 60, -130, 130, 1));
    public Zoom() {
        super("Zoom", Category.Render);
        INSTANCE = this;
        Nullpoint.EVENT_BUS.subscribe(new ZoomAnim());
    }

    @Override
    public void onEnable() {
        if (mc.options.getFov().getValue() == 70) {
            mc.options.getFov().setValue(71);
        }
    }

    public static boolean on = false;
    public class ZoomAnim {
        @EventHandler
        public void onRender3D(Render3DEvent event) {
            if (isOn()) {
                currentFov = AnimateUtil.animate(currentFov, fov.getValue(), animSpeed.getValue());
                on = true;
            } else if (on) {
                currentFov = AnimateUtil.animate(currentFov, 0, animSpeed.getValue());
                if ((int) currentFov == 0) {
                    on = false;
                }
            }
        }
    }
}
