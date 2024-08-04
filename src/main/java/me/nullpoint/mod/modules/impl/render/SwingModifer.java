package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.player.PacketEat;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;


public class SwingModifer extends Module {
    public static SwingModifer instance;
    public SwingModifer() {
        super("SwingModifer", Category.Render);
        instance=this;
    }

    public EnumSetting mode=add(new EnumSetting<Mode>("Mode", Mode.Main));

    public enum Mode{
        Main,
        OFF,
        None
    }
}
