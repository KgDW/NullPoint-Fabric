package me.nullpoint.mod.gui.font;

import me.nullpoint.Nullpoint;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class FontRenderers {
    public static FontAdapter Arial;
    public static FontAdapter Calibri;
    public static @NotNull RendererFontAdapter createDefault(float size, String name) throws IOException, FontFormatException {
        return new RendererFontAdapter(Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(FontRenderers.class.getClassLoader().getResourceAsStream("assets/minecraft/font/" + name + ".ttf"))).deriveFont(Font.PLAIN, size), size);
    }

    public static RendererFontAdapter createArial(float size) {
        Font font = new Font("tahoma", Font.PLAIN, (int) size);
        return new RendererFontAdapter(font, size);
    }

    public static RendererFontAdapter create(String name, int style, float size) {
        return new RendererFontAdapter(new Font(name, style, (int) size), size);
    }
}