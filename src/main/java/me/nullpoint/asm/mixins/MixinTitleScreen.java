package me.nullpoint.asm.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.utils.render.LogoDrawer;
import me.nullpoint.mod.gui.mainmenu.Epsilon;
import me.nullpoint.mod.gui.mainmenu.Isolation;
import me.nullpoint.mod.modules.impl.client.UIModule;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {

    @Final
    @Shadow
    private boolean doBackgroundFade;
    @Shadow
    private long backgroundFadeStart;
    @Final
    @Shadow
    private static Identifier PANORAMA_OVERLAY;
    @Final
    @Shadow
    private RotatingCubeMapRenderer backgroundRenderer;
    private static final Identifier DreamDev = new Identifier("img.png");
    private static final Identifier cao = new Identifier("bg/cao.png");
    private static final Identifier bg = new Identifier("bg/bg.png");
    private static final Identifier dianxian = new Identifier("bg/dianxian.png");
    private static final Identifier dianxian2 = new Identifier("bg/dianxian2.png");
    private static final Identifier huoche = new Identifier("bg/huoche.png");
    private static final Identifier qiao = new Identifier("bg/qiao.png");
    private static final Identifier ren = new Identifier("bg/ren.png");
    int huochex = -width;


    protected MixinTitleScreen(Text title) {
        super(title);
    }

    /**
     * @author dreamdevv
     * @reason fuck u mojang
     */
    @Overwrite
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        switch (UIModule.INSTANCE.mainMenu.getValue()) {
            case DreamDev -> {
                if (this.backgroundFadeStart == 0L && this.doBackgroundFade) {
                    this.backgroundFadeStart = Util.getMeasuringTimeMs();
                }
                float f = this.doBackgroundFade ? (float) (Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0F : 1.0F;
                this.backgroundRenderer.render(delta, MathHelper.clamp(f, 0.0F, 1.0F));
                RenderSystem.enableBlend();
                context.setShaderColor(1.0F, 1.0F, 1.0F, this.doBackgroundFade ? (float) MathHelper.ceil(MathHelper.clamp(f, 0.0F, 1.0F)) : 1.0F);
                context.drawTexture(PANORAMA_OVERLAY, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
                context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                float g = this.doBackgroundFade ? MathHelper.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();

                if (this.backgroundFadeStart == 0L && this.doBackgroundFade) {
                    this.backgroundFadeStart = Util.getMeasuringTimeMs();
                }
                float L = this.doBackgroundFade ? (float) (Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0F : 1.0F;

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, L);
                context.drawTexture(bg, 0, 0, 0, 0, 0, width, height, width, height);
                context.drawTexture(qiao, 0, 0, 0, 0, 0, width, height, width, height);
                context.drawTexture(dianxian, 0, 0, 0, 0, 0, width, height, width, height);
                context.drawTexture(huoche, huochex, height / 3, 0, 0, 0, width * 2, height / 3, width * 2, height / 3);
                context.drawTexture(dianxian2, 0, 0, 0, 0, 0, width, height, width, height);
                context.drawTexture(cao, 0, 0, 0, 0, 0, width, height, width, height);
                context.drawTexture(ren, 0, 0, 0, 0, 0, width, height, width, height);
                if (huochex >= 0) {
                    huochex = -width;
                }
                huochex++;
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
                context.setShaderColor(1.0f, 1.0f, 1.0f, g);
                context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

                int i = MathHelper.ceil(g * 255.0F) << 24;
                if ((i & -67108864) != 0) {
                    String string = "Minecraft " + SharedConstants.getGameVersion().getName();
                    assert this.client != null;
                    if (this.client.isDemo()) {
                        string = string + " Demo";
                    }
                    string = string + "/§b" + Nullpoint.LOG_NAME + " " + Nullpoint.VERSION;

                    context.drawTextWithShadow(this.textRenderer, string, 2, this.height - 10, 16777215 | i);
                }
            }
        }

    }
    @Inject(method = "tick",at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        switch (UIModule.INSTANCE.mainMenu.getValue()) {
            case Isolation -> client.setScreen(new Isolation());
        }
    }
    @Inject(method = "init",at = @At("HEAD"))
    private void init(CallbackInfo ci) {
        switch (UIModule.INSTANCE.mainMenu.getValue()) {
            case Isolation -> client.setScreen(new Isolation());
            case Epsilon -> client.setScreen(new Epsilon());
        }

    }

}
