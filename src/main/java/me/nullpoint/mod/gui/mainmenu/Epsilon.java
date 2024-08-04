package me.nullpoint.mod.gui.mainmenu;

import com.mojang.blaze3d.systems.RenderSystem;
import me.nullpoint.api.utils.render.LogoDrawer;
import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.api.utils.render.TextUtil;
import me.nullpoint.mod.gui.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static me.nullpoint.api.utils.Wrapper.mc;

public class Epsilon extends Screen {
    private static final Identifier sky = new Identifier("isolation/sky.png");
    private static final Identifier station = new Identifier("isolation/station.png");
    private static final Identifier pillar = new Identifier("isolation/pillar.png");
    private static final Identifier moon = new Identifier("isolation/moon.png");
    private static final Identifier setting = new Identifier("isolation/setting.png");
    private static final Identifier exit = new Identifier("isolation/exit.png");
    private static final Identifier single = new Identifier("isolation/single.png");
    private static final Identifier multi = new Identifier("isolation/multi.png");
    public Epsilon() {
        super(Text.translatable("narrator.screen.title"));
    }
    float zoomSingle=1, zoomMulti=1,zoomSetting=1,zoomExit=1;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        LogoDrawer.draw(context, width/4, height/4, 1);
        context.drawTexture(moon, 50, 15, 0, 0, width, height);
        context.drawTexture(sky, 0, 0, 0, 0, 0, width, height, width, height);
        context.drawTexture(station, (mouseX - width) / 64, 0, 0, 0, 0, width, height, width, height);
        context.drawTexture(pillar, (mouseX - width) / 64, 0, 0, 0, 0, width, height, width, height);
//        context.drawTexture(pillar, 400 + (mouseX - width) / 64, 300, 0, 0, 0, 150, 150, 150, 150);

        MatrixStack matrices = context.getMatrices();
        float maxButtonWidth = (float) width / 2;
        int buttonWidth = height / 12;
        float scaled = 1.2f;
        int startX = width / 4;
        int buttonY = (height - buttonWidth) / 2;
        int charc = (int) ((maxButtonWidth-4*buttonWidth)/5);
        double zoomAdd=0.06;
        float alphaFac= (float) (1 - Math.abs(mouseY - height / 2) / height) /255;
        //todo 通过alphaFac在鼠标离按钮远的时候降低按钮透明度

        matrices.push();
        if(isMouseHoveringRect(startX,buttonY,buttonWidth,mouseX,mouseY)){
            if(zoomSingle<scaled){
                zoomSingle+=zoomAdd;
            }
        }
        else{
            if(zoomSingle>1.0){
                zoomSingle-=zoomAdd;
            }
        }
        if(zoomSingle>1) {
            System.out.println("single");
            matrices.translate(startX + buttonWidth, buttonY + buttonWidth, 0);
            matrices.scale((float)zoomSingle,zoomSingle, 1);
            matrices.translate(-(startX + buttonWidth), -(buttonY + buttonWidth), 0);

        }
        RenderSystem.setShaderTexture(0, single);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
//        context.drawTexture(single, startX, buttonY, 0, 0, 0, buttonWidth, buttonWidth, buttonWidth, buttonWidth);
        Render2DUtil.drawRound(matrices, startX, buttonY, FontRenderers.Arial.getWidth("Singleplayer"), FontRenderers.Arial.getFontHeight(), 4f, new Color(255,255,255,120));
        FontRenderers.Arial.drawString(context.getMatrices(), "Singleplayer",startX, buttonY, 0xFFFFFF);
        matrices.pop();
        startX += (int) (buttonWidth + 0.2 * buttonWidth + charc);

        matrices.push();
        if(isMouseHoveringRect(startX,buttonY,buttonWidth,mouseX,mouseY)){
            if(zoomMulti<scaled){
                zoomMulti+=zoomAdd;
            }
        }
        else{
            if(zoomMulti>1.0){
                zoomMulti-=zoomAdd;
            }
        }
        if(zoomMulti>1) {
            System.out.println("muilt");
            matrices.translate(startX + buttonWidth, buttonY + buttonWidth, 0);
            matrices.scale((float) Math.min(scaled, zoomMulti), (float) Math.min(scaled, zoomMulti), 1);
            matrices.translate(-(startX + buttonWidth), -(buttonY + buttonWidth), 0);

        }
        RenderSystem.setShaderTexture(0, multi);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        context.drawTexture(multi, startX, buttonY, 0, 0, 0, buttonWidth, buttonWidth, buttonWidth, buttonWidth);
        matrices.pop();
        startX += (int) (buttonWidth + 0.2 * buttonWidth + charc);

        matrices.push();
        if(isMouseHoveringRect(startX,buttonY,buttonWidth,mouseX,mouseY)){
            if(zoomSetting<scaled){
                zoomSetting+=zoomAdd;
            }
        }
        else{
            if(zoomSetting>1.0){
                zoomSetting-=zoomAdd;
            }
        }
        if(zoomSetting>1) {
            matrices.translate(startX + buttonWidth, buttonY + buttonWidth, 0);
            matrices.scale((float) Math.min(scaled, zoomSetting), (float) Math.min(scaled, zoomSetting), 1);
            matrices.translate(-(startX + buttonWidth), -(buttonY + buttonWidth), 0);
        }
        RenderSystem.setShaderTexture(0, setting);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        context.drawTexture(setting, startX, buttonY, 0, 0, 0, buttonWidth, buttonWidth, buttonWidth, buttonWidth);
        matrices.pop();
        startX += (int) (buttonWidth + 0.2 * buttonWidth + charc);

        matrices.push();
        if(isMouseHoveringRect(startX,buttonY,buttonWidth,mouseX,mouseY)){
            if(zoomExit<scaled){
                zoomExit+=zoomAdd;
            }
        }
        else{
            if(zoomExit>1.0){
                zoomExit-=zoomAdd;
            }
        }
        if(zoomExit>1) {
            matrices.translate(startX + buttonWidth, buttonY + buttonWidth, 0);
            matrices.scale((float) Math.min(scaled, zoomExit), (float) Math.min(scaled, zoomExit), 1);
            matrices.translate(-(startX + buttonWidth), -(buttonY + buttonWidth), 0);

        }
        RenderSystem.setShaderTexture(0, exit);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        context.drawTexture(exit, startX, buttonY, 0, 0, 0, buttonWidth, buttonWidth, buttonWidth, buttonWidth);
        matrices.pop();
        //  context.setShaderColor(1f,1f,1f,1f);




    }
    public boolean isMouseHoveringRect(float x, float y, int width, int mouseX, int mouseY){
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + width;
    }
    public boolean isMouseHoveringRect(float x, float y, int width, double mouseX, double mouseY){
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + width;
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        float maxButtonWidth = (float) width / 2;
        int buttonWidth = height / 12;
        float scaled = 1.2f;
        int startX = width / 4;
        int buttonY = (height - buttonWidth) / 2;
        int charc = (int) ((maxButtonWidth-4*buttonWidth)/5);

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if(isMouseHoveringRect(startX,buttonY,buttonWidth,mouseX,mouseY)){
                client.setScreen(new SelectWorldScreen(this));
            }
            startX += (int) (buttonWidth + 0.2 * buttonWidth + charc);

            if(isMouseHoveringRect(startX,buttonY,buttonWidth,mouseX,mouseY)){
                if (!mc.options.skipMultiplayerWarning) {
                    mc.options.skipMultiplayerWarning = true;
                    mc.options.write();
                }
                Screen screen = new MultiplayerScreen(this);
                mc.setScreen(screen);
            }
            startX += (int) (buttonWidth + 0.2 * buttonWidth + charc);

            if(isMouseHoveringRect(startX,buttonY,buttonWidth,mouseX,mouseY)){
                mc.setScreen(new OptionsScreen(this, mc.options));
            }
            startX += (int) (buttonWidth + 0.2 * buttonWidth + charc);

            if(isMouseHoveringRect(startX,buttonY,buttonWidth,mouseX,mouseY)){
                mc.stop();
            }

        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}