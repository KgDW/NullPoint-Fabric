package me.nullpoint.mod.modules.impl.client;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.movement.Step;
import me.nullpoint.mod.modules.settings.impl.*;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;

import java.awt.*;
import java.util.HashMap;

public class ChatSetting extends Module {
    public static ChatSetting INSTANCE;
    public final StringSetting hackName = add(new StringSetting("Name", Nullpoint.LOG_NAME));
    public final ColorSetting color = add(new ColorSetting("Color", new Color(140, 146, 255)));
    public final ColorSetting pulse = add(new ColorSetting("Pulse", new Color(0, 0, 0)).injectBoolean(true));
    public final SliderSetting pulseSpeed = add(new SliderSetting("Speed", 1, 0, 5, 0.1, v -> pulse.booleanValue));
    public final SliderSetting pulseCounter = add(new SliderSetting("Counter", 10, 1, 50, v -> pulse.booleanValue));
    
    public final SliderSetting animateTime = add(new SliderSetting("AnimTime", 300, 0, 1000));
    public final SliderSetting animateOffset = add(new SliderSetting("AnimOffset", -40, -200, 100));
    public final EnumSetting<FadeUtils.Quad> animQuad = add(new EnumSetting<>("Quad", FadeUtils.Quad.In));
    public final BooleanSetting keepHistory = add(new BooleanSetting("KeepHistory", true));
    public final BooleanSetting infiniteChat = add(new BooleanSetting("InfiniteChat", true));
    public final EnumSetting<Style> messageStyle = add(new EnumSetting<>("MessageStyle", Style.Mio));
    public final EnumSetting<code> messageCode = add(new EnumSetting<>("MessageCode", code.Mio));
    public final StringSetting start = add(new StringSetting("StartCode", "[", v -> messageCode.getValue() == code.Custom));
    public final StringSetting end = add(new StringSetting("EndCode", "]", v -> messageCode.getValue() == code.Custom));
    public ChatSetting() {
        super("ChatSetting", Category.Client);
        INSTANCE = this;
    }

    public enum Style {
        Mio,
        Basic,
        Future,
        Earth,
        None
    }
    public enum code {
        Mio,
        Earth,
        Custom,
        None
    }
    public static final HashMap<OrderedText, StringVisitable> chatMessage = new HashMap<>();
    @Override
    public void enable() {
        this.state = true;
    }

    @Override
    public void disable() {
        this.state = true;
    }

    @Override
    public boolean isOn() {
        return true;
    }
}
