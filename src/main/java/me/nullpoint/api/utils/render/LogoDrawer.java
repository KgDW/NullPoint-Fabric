package me.nullpoint.api.utils.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class LogoDrawer {
    public static final Identifier LOGO_TEXTURE = new Identifier("icon.png");

    public static void draw(DrawContext context, int screenWidth, int screenHeight, float alpha) {
        context.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        int i = screenWidth / 2 - 25;
        int o = screenHeight / 4 - 25;
        context.drawTexture(LOGO_TEXTURE, i, o, 0.0F, 0.0F, 50, 50, 50, 50);
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
