package me.nullpoint.mod.modules.impl.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.Event;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.managers.RotateManager;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public final class DesyncESP extends Module {
    private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255)));
    private final EnumSetting<Type> type = add(new EnumSetting<>("Type", Type.ServerSide));
    public enum Type {
        ClientSide,
        ServerSide
    }

    public static DesyncESP INSTANCE;

    public DesyncESP() {
        super("DesyncESP", Category.Render);
        INSTANCE = this;
    }

    Model model;
    boolean update = true;
    @Override
    public void onLogin() {
        update = true;
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        if (update) {
            model = new Model();
            update = false;
        }
    }

    float lastYaw;
    float lastPitch;
    @EventHandler
    public void onUpdateWalkingPost(UpdateWalkingEvent event) {
        if (event.getStage() == Event.Stage.Post) {
            lastYaw = mc.player.getYaw();
            lastPitch = mc.player.getPitch();
        }
    }
    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        if (nullCheck() || model == null) return;
        if (mc.options.getPerspective() == Perspective.FIRST_PERSON) {
            return;
        }
        if (Math.abs(lastYaw - Nullpoint.ROTATE.lastYaw) < 1 && Math.abs(lastPitch - Nullpoint.ROTATE.lastPitch) < 1) return;
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 0, 1);
/*        model.modelPlayer.leftPants.visible = false;
        model.modelPlayer.rightPants.visible = false;
        model.modelPlayer.leftSleeve.visible = false;
        model.modelPlayer.rightSleeve.visible = false;
        model.modelPlayer.jacket.visible = false;
        model.modelPlayer.hat.visible = false;*/
        double x = mc.player.prevX + (mc.player.getX() - mc.player.prevX) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = mc.player.prevY + (mc.player.getY() - mc.player.prevY) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = mc.player.prevZ + (mc.player.getZ() - mc.player.prevZ) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();

        float bodyYaw = type.getValue() == Type.ServerSide ? RotateManager.getPrevRenderYawOffset() + (RotateManager.getRenderYawOffset() - RotateManager.getPrevRenderYawOffset()) * mc.getTickDelta()
                : mc.player.prevBodyYaw + (mc.player.bodyYaw - mc.player.prevBodyYaw) * mc.getTickDelta();
        float headYaw = type.getValue() == Type.ServerSide ? RotateManager.getPrevRotationYawHead() + (RotateManager.getRotationYawHead() - RotateManager.getPrevRotationYawHead()) * mc.getTickDelta()
                : mc.player.prevHeadYaw + (mc.player.headYaw - mc.player.prevHeadYaw) * mc.getTickDelta();
        float pitch = type.getValue() == Type.ServerSide ? RotateManager.getPrevPitch() + (RotateManager.getRenderPitch() - RotateManager.getPrevPitch()) * mc.getTickDelta()
                : mc.player.prevPitch + (mc.player.getPitch() - mc.player.prevPitch) * mc.getTickDelta();
        matrixStack.push();
        matrixStack.translate((float) x, (float) y, (float) z);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtil.rad(180 - bodyYaw)));
        prepareScale(matrixStack);
        
        model.modelPlayer.animateModel(mc.player, mc.player.limbAnimator.getPos(mc.getTickDelta()), mc.player.limbAnimator.getSpeed(mc.getTickDelta()), mc.getTickDelta());
        model.modelPlayer.setAngles(mc.player, mc.player.limbAnimator.getPos(mc.getTickDelta()), mc.player.limbAnimator.getSpeed(mc.getTickDelta()), mc.player.age, headYaw - bodyYaw, pitch);

        RenderSystem.enableBlend();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        model.modelPlayer.render(matrixStack, buffer, 10, 0, color.getValue().getRed() / 255f, color.getValue().getGreen() / 255f, color.getValue().getBlue() / 255f, color.getValue().getAlpha() / 255f);
        tessellator.draw();
        RenderSystem.disableBlend();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        matrixStack.pop();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }
    
    private static void prepareScale(MatrixStack matrixStack) {
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        matrixStack.scale(1.6f, 1.8f, 1.6f);
        matrixStack.translate(0.0F, -1.501F, 0.0F);
    }

    private static class Model {
        private final PlayerEntityModel<PlayerEntity> modelPlayer;

        public Model() {
            modelPlayer = new PlayerEntityModel<>(new EntityRendererFactory.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderManager(), mc.getEntityRenderDispatcher().getHeldItemRenderer(), mc.getResourceManager(), mc.getEntityModelLoader(), mc.textRenderer).getPart(EntityModelLayers.PLAYER), false);
            modelPlayer.getHead().scale(new Vector3f(-0.3f, -0.3f, -0.3f));
        }
    }
}
