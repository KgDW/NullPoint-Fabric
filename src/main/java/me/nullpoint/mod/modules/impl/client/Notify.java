package me.nullpoint.mod.modules.impl.client;

import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import me.nullpoint.mod.modules.Module;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.ArrayList;

public class Notify extends Module {
    public static final ArrayList<Notifys> notifyList = new ArrayList();
    public static Notify INSTANCE;
    public final EnumSetting<Notifys.type> type = add(new EnumSetting<>("Type", Notifys.type.Notify));
    public final EnumSetting<Notifys.mode> mode = add(new EnumSetting<>("Type", Notifys.mode.Fill));
    private final SliderSetting notifyY = add(new SliderSetting("Y", 18, -50, 500));
    private final ColorSetting fillcolor = add(new ColorSetting("FillColor",new Color(20, 20, 20, 100)));
    private final ColorSetting linecolor = add(new ColorSetting("LineColor",new Color(140,140,250,225)));
    public final SliderSetting notifyX =
            add(new SliderSetting("notifyX", 256, 18, 500));
    public Notify(){
        super("Notify","Notify Test",Category.Client);
        INSTANCE = this;
    }
    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        boolean bl = true;
        int n = (int) (379 - this.notifyY.getValue());
        int n2 = notifyX.getValueInt() + 500;
        for (Notifys notifys : notifyList) {
            if (notifys == null || notifys.first == null || notifys.firstFade == null || notifys.delayed < 1) continue;
            bl = false;
            if (notifys.delayed < 5 && !notifys.end) {
                notifys.end = true;
                notifys.endFade.reset();
            }
            n = (int)((double)n - 18.0 * notifys.yFade.easeOutQuad());
            String string = notifys.first;
            double d = notifys.delayed < 5 ? (double)n2 - (double)(mc.textRenderer.getWidth(string) + 10) * (1.0 - notifys.endFade.easeOutQuad()) : (double)n2 - (double)(mc.textRenderer.getWidth(string) + 10) * notifys.firstFade.easeOutQuad();
            Render2DUtil.drawRound(drawContext.getMatrices(),(int)d, n, 10 + mc.textRenderer.getWidth(string), 15,4f, fillcolor.getValue());
//            Render2DUtil.drawRect(drawContext.getMatrices(),(int)d, n, 10 + mc.textRenderer.getWidth(string), 15, fillcolor.getValue());
            drawContext.drawText(mc.textRenderer, string, 5 + (int)d, 4 + n , new Color(255,255,255,255).getRGB(), true);
            if (notifys.delayed < 5) {
                n = (int)((double)n + 18.0 * notifys.yFade.easeOutQuad() - 18.0 * (1.0 - notifys.endFade.easeOutQuad()));
                continue;
            }
            Render2DUtil.drawRect(drawContext.getMatrices(),(int)d+2f, n + 14, (float) ((10 + mc.textRenderer.getWidth(string)) * (notifys.delayed - 4)-2) / 62, 1, linecolor.getValue());
        }
        if (bl) {
            notifyList.clear();
        }
    }

    @Override
    public void onUpdate() {
        if (UIModule.INSTANCE.state){
            return;
        }
        for (Notifys notifys : notifyList) {
            if (notifys == null || notifys.first == null || notifys.firstFade == null) continue;
            --notifys.delayed;
        }
    }

    @Override
    public void onDisable() {
        notifyList.clear();
    }

    public static class Notifys {
        public final FadeUtils firstFade = new FadeUtils(500L);
        public final FadeUtils endFade;
        public final FadeUtils yFade = new FadeUtils(500L);
        public final String first;
        public int delayed = 55;
        public boolean end;

        public Notifys(String string) {
            this.endFade = new FadeUtils(350L);
            this.first = string;
            this.firstFade.reset();
            this.yFade.reset();
            this.endFade.reset();
            this.end = false;
        }
        public enum type {
            Notify,
            Chat,
            Both
        }
        private enum mode {
            Line,
            Fill
        }
    }
}
