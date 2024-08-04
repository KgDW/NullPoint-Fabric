package me.nullpoint.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.ShaderManager;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.mod.gui.font.FontRenderers;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static com.mojang.blaze3d.systems.RenderSystem.disableBlend;

public class Render3DUtil implements Wrapper {
    public static MatrixStack matrixFrom(double x, double y, double z) {
        MatrixStack matrices = new MatrixStack();

        Camera camera = mc.gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

        matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);

        return matrices;
    }
    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    } public static void endRender() {
        RenderSystem.disableBlend();
    }


    public static void drawText3D(String text, Vec3d vec3d, Color color) {
        drawText3D(Text.of(text), vec3d.x, vec3d.y, vec3d.z, 0, 0, 1, color.getRGB());
    }

    public static void drawText3D(String text, Vec3d vec3d, int color) {
        drawText3D(Text.of(text), vec3d.x, vec3d.y, vec3d.z, 0, 0, 1, color);
    }
    public static void drawText3D(Text text, Vec3d vec3d, double offX, double offY, double scale, Color color) {
        drawText3D(text, vec3d.x, vec3d.y, vec3d.z, offX, offY, scale, color.getRGB());
    }
    public static void drawText3D(Text text, double x, double y, double z, double offX, double offY, double scale, int color) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        MatrixStack matrices = matrixFrom(x, y, z);

        Camera camera = mc.gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        matrices.translate(offX, offY, 0);
        matrices.scale(-0.025f * (float) scale, -0.025f * (float) scale, 1);

        int halfWidth = mc.textRenderer.getWidth(text) / 2;

        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

        matrices.push();
        matrices.translate(1, 1, 0);
        mc.textRenderer.draw(Text.of(text.getString().replaceAll("§[a-zA-Z0-9]", "")), -halfWidth, 0f, 0x202020, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xf000f0);
        immediate.draw();
        matrices.pop();

        mc.textRenderer.draw(text.copy(), -halfWidth, 0f, color, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xf000f0);
        immediate.draw();

        RenderSystem.disableBlend();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
    public static void drawTextIn3D(String text, Vec3d pos, double offX, double offY, double textOffset, Color color) {
        MatrixStack matrices = new MatrixStack();
        Camera camera = mc.gameRenderer.getCamera();
        //RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrices.translate(pos.getX() - camera.getPos().x, pos.getY() - camera.getPos().y, pos.getZ() - camera.getPos().z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        matrices.translate(offX, offY - 0.1, -0.01);
        matrices.scale(-0.025f, -0.025f, 0);
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

        FontRenderers.Arial.drawCenteredString(matrices, text, textOffset, 0f, color.getRGB());
        immediate.draw();
        RenderSystem.enableCull();
        //RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void drawFill(MatrixStack matrixStack, Box bb, Color color) {
        draw3DBox(matrixStack, bb, color, false, true);
    }
    public static void drawBox(MatrixStack matrixStack, Box bb, Color color) {
        draw3DBox(matrixStack, bb, color, true, false);
    }
    public static void drawLine(Box b, Color color, float lineWidth) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        MatrixStack matrices = matrixFrom(b.minX, b.minY, b.minZ);
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);

        RenderSystem.lineWidth(lineWidth);
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Box box = b.offset(new Vec3d(b.minX, b.minY, b.minZ).negate());
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;
        vertexLine(matrices, buffer, x1, y1, z1, x2, y1, z1, color);
        vertexLine(matrices, buffer, x2, y1, z1, x2, y1, z2, color);
        vertexLine(matrices, buffer, x2, y1, z2, x1, y1, z2, color);
        vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);
        vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
        vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
        vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
        vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);
        vertexLine(matrices, buffer, x1, y2, z1, x2, y2, z1, color);
        vertexLine(matrices, buffer, x2, y2, z1, x2, y2, z2, color);
        vertexLine(matrices, buffer, x2, y2, z2, x1, y2, z2, color);
        vertexLine(matrices, buffer, x1, y2, z2, x1, y2, z1, color);
        tessellator.draw();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
    public static void drawSphere(MatrixStack matrix, EndCrystalEntity entity, Float radius, Float height, Float lineWidth, Color color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        double x = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * mc.getTickDelta();
        double y = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * mc.getTickDelta();
        double z = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * mc.getTickDelta();
        double pix2 = Math.PI * 2.0;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.lineWidth(lineWidth);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 180; i++) {
            bufferBuilder.vertex(
                    matrix.peek().getPositionMatrix(),
                    (float) (x + radius * Math.cos(i * pix2 / 45.0)),
                    (float) (y + height),
                    (float) (z + radius * Math.sin(i * pix2 / 45.0))
            ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        }
        tessellator.draw();
        RenderSystem.disableBlend();
    }


    public static void draw3DBox(MatrixStack matrixStack, Box box, Color color) {
        draw3DBox(matrixStack, box, color, true, true);
    }

    public static void draw3DBox(MatrixStack matrixStack, Box box, Color color, boolean outline, boolean fill) {
        box = box.offset(mc.gameRenderer.getCamera().getPos().negate());
        RenderSystem.enableBlend();
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        if (outline) {
            RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
            RenderSystem.setShader(GameRenderer::getPositionProgram);
            bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();

            tessellator.draw();
        }

        if (fill) {
            RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
            RenderSystem.setShader(GameRenderer::getPositionProgram);

            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();

            tessellator.draw();
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderSystem.disableBlend();
    }

    public static void drawHole(MatrixStack matrixStack, BlockPos pos, Color color, boolean outline, boolean fill, float height, float lineWidth){
        if (outline) {
            drawHoleLine(new Box(pos), color, lineWidth);
        }

        Box box = new Box(pos);
        box = box.offset(mc.gameRenderer.getCamera().getPos().negate());
        RenderSystem.enableBlend();
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        if (fill) {
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.disableCull();

            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY + height, (float) box.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY + height, (float) box.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY + height, (float) box.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY + height, (float) box.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY + height, (float) box.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY + height, (float) box.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY + height, (float) box.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY + height, (float) box.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).next();

            tessellator.draw();

            RenderSystem.enableCull();
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderSystem.disableBlend();
    }

    public static void drawHoleLine(Box b, Color color, float lineWidth) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        MatrixStack matrices = matrixFrom(b.minX, b.minY, b.minZ);
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);

        RenderSystem.lineWidth(lineWidth);
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Box box = b.offset(new Vec3d(b.minX, b.minY, b.minZ).negate());
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float z2 = (float) box.maxZ;
        vertexLine(matrices, buffer, x1, y1, z1, x2, y1, z1, color);
        vertexLine(matrices, buffer, x2, y1, z1, x2, y1, z2, color);
        vertexLine(matrices, buffer, x2, y1, z2, x1, y1, z2, color);
        vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);
        tessellator.draw();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, Color color, float width) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        MatrixStack matrices = matrixFrom(x1, y1, z1);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(width);
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        vertexLine(matrices, buffer, 0f, 0f, 0f, (float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1), color);
        tessellator.draw();
        RenderSystem.enableCull();
        RenderSystem.lineWidth(1f);
        disableBlend();
    }

    public static void vertexLine(MatrixStack matrices, VertexConsumer buffer, double x1, double y1, double z1, double x2, double y2, double z2, Color lineColor) {
        Matrix4f model = matrices.peek().getPositionMatrix();
        Matrix3f normal = matrices.peek().getNormalMatrix();
        Vector3f normalVec = getNormal((float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2);
        buffer.vertex(model, (float) x1, (float) y1, (float) z1).color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), lineColor.getAlpha()).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
        buffer.vertex(model, (float) x2, (float) y2, (float) z2).color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), lineColor.getAlpha()).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
    }

    public static Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
        float xNormal = x2 - x1;
        float yNormal = y2 - y1;
        float zNormal = z2 - z1;
        float normalSqrt = MathHelper.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal);

        return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
    }
}
