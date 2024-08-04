package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Identifier;

public class HitMarker extends Module {
    public HitMarker() {
        super("HitMarker", Category.Render);
    }
    public SliderSetting time=add(new SliderSetting("Show Time",3,0,60));
    private final Identifier marker = new Identifier("nullpoint", "hitmarker.png");
    public Timer timer=new Timer();
    public int ticks=114514;
    @Override
    public void onEnable() {
        ticks=114514;
        timer.reset();
    }
    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if(timer.passedMs(1/20)) {
            timer.reset();
            if (ticks <= time.getValueFloat()) {
                ++ticks;
               drawContext.drawTexture(marker,mc.getWindow().getScaledWidth()/2-8,mc.getWindow().getScaledHeight()/2-8,0,0,0,16,16,16,16);
            }
        }
    }

    @EventHandler
    public void onpacket(PacketEvent.Send event){
        if(event.getPacket() instanceof PlayerInteractEntityC2SPacket){
            ticks=0;

        }
    }
}
