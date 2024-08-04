package me.nullpoint.mod.gui.clickgui.components.impl;

import me.nullpoint.api.managers.GuiManager;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.api.utils.render.TextUtil;
import me.nullpoint.mod.gui.clickgui.components.Component;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.gui.clickgui.tabs.ClickGuiTab;
import me.nullpoint.mod.modules.impl.client.UIModule;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
public class ColorComponents extends Component {
    private float hue;
    private float saturation;
    private float brightness;
    private int alpha;

    private boolean afocused;
    private boolean hfocused;
    private boolean sbfocused;

    private float spos, bpos, hpos, apos;

    private Color prevColor;

    private boolean firstInit;

    private final ColorSetting colorSetting;

    public ColorSetting getColorSetting() {
        return colorSetting;
    }

    public ColorComponents(ClickGuiTab parent, ColorSetting setting) {
        super();
        this.parent = parent;
        this.colorSetting = setting;
        prevColor = getColorSetting().getValue();
        updatePos();
        firstInit = true;
    }

    @Override
    public boolean isVisible() {
        if (colorSetting.visibility != null) {
            return colorSetting.visibility.test(null);
        }
        return true;
    }
    private void updatePos() {
        float[] hsb = Color.RGBtoHSB(getColorSetting().getValue().getRed(), getColorSetting().getValue().getGreen(), getColorSetting().getValue().getBlue(), null);
        hue = -1 + hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = getColorSetting().getValue().getAlpha();
    }

    private void setColor(Color color) {
        getColorSetting().setValue(color.getRGB());
        prevColor = color;
    }

    private final Timer clickTimer = new Timer();
    private double lastMouseX;
    private double lastMouseY;
    boolean clicked = false;
    boolean popped = false;
    boolean hover = false;
    @Override
    public void update(int offset, double mouseX, double mouseY, boolean mouseClicked) {
        int x = parent.getX();
        int y = (int) (parent.getY() + currentOffset) - 2;
        int width = parent.getWidth();
        double cx = x + 3;
        double cy = y + defaultHeight;
        double cw = width - 19;
        double ch = getHeight() - 17;
        hover = Render2DUtil.isHovered(mouseX, mouseY, (float) x + 1, (float) y + 1, (float) width - 2, (float) defaultHeight);
        if (hover) {
            if (GuiManager.currentGrabbed == null && isVisible()) {
                if (ClickGuiScreen.rightClicked) {
                    ClickGuiScreen.rightClicked = false;
                    this.popped = !this.popped;
                }
            }
        }
        if (popped) {
            setHeight(45 + defaultHeight);
        } else {
            setHeight(defaultHeight);
        }
        if ((ClickGuiScreen.clicked || ClickGuiScreen.hoverClicked) && isVisible()) {
            if (!clicked) {
                if (Render2DUtil.isHovered(mouseX, mouseY, cx + cw + 9, cy, 4, ch)) {
                    afocused = true;
                    ClickGuiScreen.hoverClicked = true;
                    ClickGuiScreen.clicked = false;
                }
                if (Render2DUtil.isHovered(mouseX, mouseY, cx + cw + 4, cy, 4, ch)) {
                    hfocused = true;
                    ClickGuiScreen.hoverClicked = true;
                    ClickGuiScreen.clicked = false;
                    if (colorSetting.isRainbow) {
                        colorSetting.setRainbow(false);
                        lastMouseX = 0;
                        lastMouseY = 0;
                    } else {
                        if (!clickTimer.passedMs(400) && mouseX == lastMouseX && mouseY == lastMouseY) {
                            colorSetting.setRainbow(!colorSetting.isRainbow);
                        }
                        clickTimer.reset();
                        lastMouseX = mouseX;
                        lastMouseY = mouseY;
                    }
                }
                if (Render2DUtil.isHovered(mouseX, mouseY, cx, cy, cw, ch)) {
                    sbfocused = true;
                    ClickGuiScreen.hoverClicked = true;
                    ClickGuiScreen.clicked = false;
                }
                if (GuiManager.currentGrabbed == null && isVisible()) {
                    if (hover && getColorSetting().injectBoolean) {
                        getColorSetting().booleanValue = !getColorSetting().booleanValue;
                        ClickGuiScreen.clicked = false;
                    }
                }
            }
            clicked = true;
        } else {
            clicked = false;
            sbfocused = false;
            afocused = false;
            hfocused = false;
        }
        if (!popped) return;
        if (GuiManager.currentGrabbed == null && isVisible()) {
            Color value = Color.getHSBColor(hue, saturation, brightness);
            if (sbfocused) {
                saturation = (float) ((MathUtil.clamp((float) (mouseX - cx), 0f, (float) cw)) / cw);
                brightness = (float) ((ch - MathUtil.clamp((float) (mouseY - cy), 0, (float) ch)) / ch);
                value = Color.getHSBColor(hue, saturation, brightness);
                setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), alpha));
            }

            if (hfocused) {
                hue = (float) -((ch - MathUtil.clamp((float) (mouseY - cy), 0, (float) ch)) / ch);
                value = Color.getHSBColor(hue, saturation, brightness);
                setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), alpha));
            }

            if (afocused) {
                alpha = (int) (((ch - MathUtil.clamp((float) (mouseY - cy), 0, (float) ch)) / ch) * 255);
                setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), alpha));
            }
        }
    }


    public double currentWidth = 0;
    @Override
    public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
        currentOffset = animate(currentOffset, offset);
        if (back && Math.abs(currentOffset - offset) <= 0.5) {
            currentWidth = 0;
            return false;
        }
        int x = parent.getX();
        int y = (int) (parent.getY() + currentOffset - 2);
        int width = parent.getWidth();
        MatrixStack matrixStack = drawContext.getMatrices();

        Render2DUtil.drawRect(matrixStack, (float) x + 1, (float) y + 1, (float) width - 2, (float) defaultHeight - 1, hover ? UIModule.INSTANCE.shColor.getValue() : UIModule.INSTANCE.sbgColor.getValue());

        if (colorSetting.injectBoolean) {
            currentWidth = animate(currentWidth, colorSetting.booleanValue ? (width - 2D) : 0D, UIModule.INSTANCE.booleanSpeed.getValue());
            Render2DUtil.drawRect(matrixStack, (float) x + 1, (float) y + 1, (float) currentWidth, (float) defaultHeight - 1, hover ? UIModule.INSTANCE.mainHover.getValue() : color);
        }
        TextUtil.drawString(drawContext, colorSetting.getName(), x + 4, y + getTextOffsetY(), new Color(-1).getRGB());

        Render2DUtil.drawRound(matrixStack, (float) (x + width - 16), (float) (y + getTextOffsetY()), 12, 8, 1, ColorUtil.injectAlpha(getColorSetting().getValue(), 255));

        if (back) return true;
        if (!popped) {
            return true;
        }
        double cx = x;
        double cy = y + defaultHeight + 1;
        double cw = width - 15;
        double ch = defaultHeight - 32 + 60;

        if (prevColor != getColorSetting().getValue()) {
            updatePos();
            prevColor = getColorSetting().getValue();
        }

        if (firstInit) {
            spos = (float) ((cx + cw) - (cw - (cw * saturation)));
            bpos = (float) ((cy + (ch - (ch * brightness))));
            hpos = (float) ((cy + (ch - 3 + ((ch - 3) * hue))));
            apos = (float) ((cy + (ch - 3 - ((ch - 3) * (alpha / 255f)))));
            firstInit = false;
        }

        spos = (float) animate(spos, (float) ((cx + cw) - (cw - (cw * saturation))), .6f);
        bpos = (float) animate(bpos, (float) (cy + (ch - (ch * brightness))), .6f);
        hpos = (float) animate(hpos, (float) (cy + (ch - 3 + ((ch - 3) * hue))), .6f);
        apos = (float) animate(apos, (float) (cy + (ch - 3 - ((ch - 3) * (alpha / 255f)))), .6f);

        Color colorA = Color.getHSBColor(hue, 0.0F, 1.0F), colorB = Color.getHSBColor(hue, 1.0F, 1.0F);
        Color colorC = new Color(0, 0, 0, 0), colorD = new Color(0, 0, 0);

        Render2DUtil.horizontalGradient(matrixStack, (float) cx + 2, (float) cy, (float) (cx + cw), (float) (cy + ch), colorA, colorB);
        Render2DUtil.verticalGradient(matrixStack, (float) (cx + 2), (float) cy, (float) (cx + cw), (float) (cy + ch), colorC, colorD);

        for (float i = 1f; i < ch - 2f; i += 1f) {
            float curHue = (float) (1f / (ch / i));
            Render2DUtil.drawRect(matrixStack, (float) (cx + cw + 4), (float) (cy + i), 4, 1, Color.getHSBColor(curHue, 1f, 1f));
        }

        Render2DUtil.verticalGradient(matrixStack, (float) (cx + cw + 9), (float) (cy + 0.8f), (float) (cx + cw + 12.5), (float) (cy + ch - 2), new Color(getColorSetting().getValue().getRed(), getColorSetting().getValue().getGreen(), getColorSetting().getValue().getBlue(), 255), new Color(0, 0, 0, 0));

        Render2DUtil.drawRect(matrixStack, (float) (cx + cw + 3), hpos + 0.5f, 5, 1, Color.WHITE);
        Render2DUtil.drawRect(matrixStack, (float) (cx + cw + 8), apos + 0.5f, 5, 1, Color.WHITE);
        Render2DUtil.drawRound(matrixStack, spos - 1.5f, bpos - 1.5f, 3, 3, 1.5f, new Color(-1));
        return true;
    }
}
