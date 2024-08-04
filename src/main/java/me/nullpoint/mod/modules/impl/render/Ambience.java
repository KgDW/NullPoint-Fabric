package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import java.awt.*;


public class Ambience extends Module {

    public static Ambience INSTANCE;
    public final ColorSetting worldColor = add(new ColorSetting("WorldColor", new Color(0xFFFFFFFF, true)).injectBoolean(true));
    public final BooleanSetting customTime =
            add(new BooleanSetting("CustomTime", false).setParent());
    private final SliderSetting time =
            add(new SliderSetting("Time", 0, 0, 24000, v -> customTime.isOpen()));
    public final ColorSetting fog =
            add(new ColorSetting("FogColor", new Color(0xCC7DD5)).injectBoolean(false));
    public final ColorSetting sky =
            add(new ColorSetting("SkyColor", new Color(0x000000)).injectBoolean(false));
    public final BooleanSetting fogDistance =
            add(new BooleanSetting("FogDistance", false).setParent());
    public final SliderSetting fogStart =
            add(new SliderSetting("FogStart", 50, 0, 1000, v -> fogDistance.isOpen()));
    public final SliderSetting fogEnd =
            add(new SliderSetting("FogEnd", 100, 0, 1000, v -> fogDistance.isOpen()));
    public Ambience() {
        super("Ambience", "Custom ambience", Category.Render);
        INSTANCE = this;
    }

    long oldTime;

    @Override
    public void onUpdate() {
        if (customTime.getValue()) {
            mc.world.setTimeOfDay((long) this.time.getValue());
        }
    }

    @Override
    public void onEnable() {
        if (nullCheck()) return;
        oldTime = mc.world.getTimeOfDay();
    }

    @Override
    public void onDisable() {
        mc.world.setTimeOfDay(oldTime);
    }

    @EventHandler
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            oldTime = ((WorldTimeUpdateS2CPacket) event.getPacket()).getTime();
            event.cancel();
        }
    }
}