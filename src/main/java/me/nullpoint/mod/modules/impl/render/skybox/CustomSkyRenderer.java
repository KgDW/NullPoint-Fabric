package me.nullpoint.mod.modules.impl.render.skybox;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.CheckedRandom;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;

public class CustomSkyRenderer
        implements DimensionRenderingRegistry.SkyRenderer {
    public static Identifier id(String path) {
        return new Identifier("better_sky", path);
    }
    private static final Identifier NEBULA_1 = id("textures/sky/nebula_2.png");
    private static final Identifier NEBULA_2 = id("textures/sky/nebula_3.png");
    private static final Identifier HORIZON = id("textures/sky/nebula_1.png");
    private static final Identifier STARS = id("textures/sky/stars.png");
    private static final Identifier FOG = id("textures/sky/fog.png");
    private VertexBuffer nebula1;
    private VertexBuffer nebula2;
    private VertexBuffer horizon;
    private VertexBuffer stars1;
    private VertexBuffer stars2;
    private VertexBuffer stars3;
    private VertexBuffer stars4;
    private VertexBuffer fog;
    private Vector3f axis1;
    private Vector3f axis2;
    private Vector3f axis3;
    private Vector3f axis4;
    private boolean initialised;

    private void initialise() {
        if (!this.initialised) {
            this.initStars();
            CheckedRandom random = new CheckedRandom(131L);
            this.axis1 = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
            this.axis2 = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
            this.axis3 = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
            this.axis4 = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
            this.axis1.normalize();
            this.axis2.normalize();
            this.axis3.normalize();
            this.axis4.normalize();
            this.initialised = true;
        }
    }

    public void render(WorldRenderContext context) {
        float a;
        if (context.world() == null || context.matrixStack() == null) {
            return;
        }
        this.initialise();
        Matrix4f projectionMatrix = context.projectionMatrix();
        MatrixStack matrices = context.matrixStack();
        float time = ((float)context.world().getTimeOfDay() + context.tickDelta()) % 360000.0f * 1.7453292E-5f;
        float time2 = time * 2.0f;
        float time3 = time * 3.0f;
        BackgroundRenderer.applyFogColor();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        float blindA = 1.0f - BackgroundInfo.blindness;
        float blind02 = blindA * 0.2f;
        float blind06 = blindA * 0.6f;
        if (blindA > 0.0f) {

            matrices.push();
            matrices.multiply(new Quaternionf().rotationXYZ(0.0f, time, 0.0f));
            RenderSystem.setShaderTexture(0, HORIZON);
            Color color = Skybox.INSTANCE.color.getValue();
            this.renderBuffer(matrices, projectionMatrix, this.horizon, VertexFormats.POSITION_TEXTURE, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 0.7f * blindA);
            matrices.pop();

            matrices.push();
            matrices.multiply(new Quaternionf().rotationXYZ(0.0f, -time, 0.0f));
            RenderSystem.setShaderTexture(0, NEBULA_1);
            color = Skybox.INSTANCE.color2.getValue();
            this.renderBuffer(matrices, projectionMatrix, this.nebula1, VertexFormats.POSITION_TEXTURE, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, blind02);
            matrices.pop();

            matrices.push();
            matrices.multiply(new Quaternionf().rotationXYZ(0.0f, time2, 0.0f));
            RenderSystem.setShaderTexture(0, NEBULA_2);
            color = Skybox.INSTANCE.color3.getValue();
            this.renderBuffer(matrices, projectionMatrix, this.nebula2, VertexFormats.POSITION_TEXTURE, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, blind02);
            matrices.pop();
            if(Skybox.INSTANCE.stars.getValue()) {

                RenderSystem.setShaderTexture(0, STARS);
                matrices.push();
                matrices.multiply(new Quaternionf().setAngleAxis(time, this.axis3.x, this.axis3.y, this.axis3.z));
                color = Skybox.INSTANCE.color4.getValue();
                this.renderBuffer(matrices, projectionMatrix, this.stars3, VertexFormats.POSITION_TEXTURE, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, blind06);
                matrices.pop();

                matrices.push();
                matrices.multiply(new Quaternionf().setAngleAxis(time2, this.axis4.x, this.axis4.y, this.axis4.z));
                color = Skybox.INSTANCE.color5.getValue();
                this.renderBuffer(matrices, projectionMatrix, this.stars4, VertexFormats.POSITION_TEXTURE, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, blind06);
                matrices.pop();
            }
        }
        if ((a = BackgroundInfo.fogDensity - 1.0f) > 0.0f) {
            if (a > 1.0f) {
                a = 1.0f;
            }
            RenderSystem.setShaderTexture(0, FOG);
            this.renderBuffer(matrices, projectionMatrix, this.fog, VertexFormats.POSITION_TEXTURE, BackgroundInfo.fogColorRed, BackgroundInfo.fogColorGreen, BackgroundInfo.fogColorBlue, a);
        }
        if(Skybox.INSTANCE.stars.getValue()) {
            if (blindA > 0.0f) {
                matrices.push();
                matrices.multiply(new Quaternionf().setAngleAxis(time3, this.axis1.x, this.axis1.y, this.axis1.z));
                this.renderBuffer(matrices, projectionMatrix, this.stars1, VertexFormats.POSITION, 1.0f, 1.0f, 1.0f, blind06);
                matrices.pop();
                matrices.push();
                matrices.multiply(new Quaternionf().setAngleAxis(time2, this.axis2.x, this.axis2.y, this.axis2.z));
                this.renderBuffer(matrices, projectionMatrix, this.stars2, VertexFormats.POSITION, 0.95f, 0.64f, 0.93f, blind06);
                matrices.pop();
            }
        }
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderBuffer(MatrixStack matrices, Matrix4f matrix4f, VertexBuffer buffer, VertexFormat format, float r, float g, float b, float a) {
            RenderSystem.setShaderColor(r, g, b, a);
            buffer.bind();
            if (format == VertexFormats.POSITION) {
                buffer.draw(matrices.peek().getPositionMatrix(), matrix4f, GameRenderer.getPositionProgram());
            } else {
                buffer.draw(matrices.peek().getPositionMatrix(), matrix4f, GameRenderer.getPositionTexProgram());
            }
            VertexBuffer.unbind();
    }

    private void initStars() {

            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        if(Skybox.INSTANCE.stars.getValue()) {
            this.stars1 = this.buildBuffer(buffer, this.stars1, 0.1, 0.3, 3500, 41315L, this::makeStars);
            this.stars2 = this.buildBuffer(buffer, this.stars2, 0.1, 0.35, 2000, 35151L, this::makeStars);
            this.stars3 = this.buildBuffer(buffer, this.stars3, 0.4, 1.2, 1000, 61354L, this::makeUVStars);
            this.stars4 = this.buildBuffer(buffer, this.stars4, 0.4, 1.2, 1000, 61355L, this::makeUVStars);
        }
            this.nebula1 = this.buildBuffer(buffer, this.nebula1, 40.0, 60.0, 30, 11515L, this::makeFarFog);
            this.nebula2 = this.buildBuffer(buffer, this.nebula2, 40.0, 60.0, 10, 14151L, this::makeFarFog);
            this.horizon = this.buildBufferHorizon(buffer, this.horizon);
            this.fog = this.buildBufferFog(buffer, this.fog);


    }

    private VertexBuffer buildBuffer(BufferBuilder bufferBuilder, VertexBuffer buffer, double minSize, double maxSize, int count, long seed, BufferFunction fkt) {
        if (buffer != null) {
            buffer.close();
        }
        buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        fkt.make(bufferBuilder, minSize, maxSize, count, seed);
        BufferBuilder.BuiltBuffer renderedBuffer = bufferBuilder.end();
        buffer.bind();
        buffer.upload(renderedBuffer);
        return buffer;
    }

    private VertexBuffer buildBufferHorizon(BufferBuilder bufferBuilder, VertexBuffer buffer) {
        return this.buildBuffer(bufferBuilder, buffer, 0.0, 0.0, 0, 0L, (_builder, _minSize, _maxSize, _count, _seed) -> this.makeCylinder(_builder, 16, 50.0, 100.0));
    }

    private VertexBuffer buildBufferFog(BufferBuilder bufferBuilder, VertexBuffer buffer) {
        return this.buildBuffer(bufferBuilder, buffer, 0.0, 0.0, 0, 0L, (_builder, _minSize, _maxSize, _count, _seed) -> this.makeCylinder(_builder, 16, 50.0, 70.0));
    }
    public static double randRange(double min, double max, net.minecraft.util.math.random.Random random) {
        return min + random.nextDouble() * (max - min);
    }

    private void makeStars(BufferBuilder buffer, double minSize, double maxSize, int count, long seed) {
        CheckedRandom random = new CheckedRandom(seed);
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        for (int i = 0; i < count; ++i) {
            double posX = random.nextDouble() * 2.0 - 1.0;
            double posY = random.nextDouble() * 2.0 - 1.0;
            double posZ = random.nextDouble() * 2.0 - 1.0;
            double size = randRange(minSize, maxSize, random);
            double length = posX * posX + posY * posY + posZ * posZ;
            if (!(length < 1.0) || !(length > 0.001)) continue;
            length = 1.0 / Math.sqrt(length);
            double px = (posX *= length) * 100.0;
            double py = (posY *= length) * 100.0;
            double pz = (posZ *= length) * 100.0;
            double angle = Math.atan2(posX, posZ);
            double sin1 = Math.sin(angle);
            double cos1 = Math.cos(angle);
            angle = Math.atan2(Math.sqrt(posX * posX + posZ * posZ), posY);
            double sin2 = Math.sin(angle);
            double cos2 = Math.cos(angle);
            angle = random.nextDouble() * Math.PI * 2.0;
            double sin3 = Math.sin(angle);
            double cos3 = Math.cos(angle);
            for (int index = 0; index < 4; ++index) {
                double x = (double)((index & 2) - 1) * size;
                double y = (double)((index + 1 & 2) - 1) * size;
                double aa = x * cos3 - y * sin3;
                double ab = y * cos3 + x * sin3;
                double dy = aa * sin2 + 0.0 * cos2;
                double ae = 0.0 * sin2 - aa * cos2;
                double dx = ae * sin1 - ab * cos1;
                double dz = ab * sin1 + ae * cos1;
                buffer.vertex(px + dx, py + dy, pz + dz).next();
            }
        }
    }

    private void makeUVStars(BufferBuilder buffer, double minSize, double maxSize, int count, long seed) {
        CheckedRandom random = new CheckedRandom(seed);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        for (int i = 0; i < count; ++i) {
            double posX = random.nextDouble() * 2.0 - 1.0;
            double posY = random.nextDouble() * 2.0 - 1.0;
            double posZ = random.nextDouble() * 2.0 - 1.0;
            double size = randRange(minSize, maxSize, random);
            double length = posX * posX + posY * posY + posZ * posZ;
            if (!(length < 1.0) || !(length > 0.001)) continue;
            length = 1.0 / Math.sqrt(length);
            double px = (posX *= length) * 100.0;
            double py = (posY *= length) * 100.0;
            double pz = (posZ *= length) * 100.0;
            double angle = Math.atan2(posX, posZ);
            double sin1 = Math.sin(angle);
            double cos1 = Math.cos(angle);
            angle = Math.atan2(Math.sqrt(posX * posX + posZ * posZ), posY);
            double sin2 = Math.sin(angle);
            double cos2 = Math.cos(angle);
            angle = random.nextDouble() * Math.PI * 2.0;
            double sin3 = Math.sin(angle);
            double cos3 = Math.cos(angle);
            float minV = (float)random.nextInt(4) / 4.0f;
            for (int index = 0; index < 4; ++index) {
                double x = (double)((index & 2) - 1) * size;
                double y = (double)((index + 1 & 2) - 1) * size;
                double aa = x * cos3 - y * sin3;
                double ab = y * cos3 + x * sin3;
                double dy = aa * sin2 + 0.0 * cos2;
                double ae = 0.0 * sin2 - aa * cos2;
                double dx = ae * sin1 - ab * cos1;
                double dz = ab * sin1 + ae * cos1;
                float texU = index >> 1 & 1;
                float texV = (float)(index + 1 >> 1 & 1) / 4.0f + minV;
                buffer.vertex(px + dx, py + dy, pz + dz).texture(texU, texV).next();
            }
        }
    }

    private void makeFarFog(BufferBuilder buffer, double minSize, double maxSize, int count, long seed) {
        CheckedRandom random = new CheckedRandom(seed);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        for (int i = 0; i < count; ++i) {
            double posX = random.nextDouble() * 2.0 - 1.0;
            double posY = random.nextDouble() - 0.5;
            double posZ = random.nextDouble() * 2.0 - 1.0;
            double size = randRange(minSize, maxSize, random);
            double length = posX * posX + posY * posY + posZ * posZ;
            double distance = 2.0;
            if (!(length < 1.0) || !(length > 0.001)) continue;
            length = distance / Math.sqrt(length);
            size *= distance;
            double px = (posX *= length) * 100.0;
            double py = (posY *= length) * 100.0;
            double pz = (posZ *= length) * 100.0;
            double angle = Math.atan2(posX, posZ);
            double sin1 = Math.sin(angle);
            double cos1 = Math.cos(angle);
            angle = Math.atan2(Math.sqrt(posX * posX + posZ * posZ), posY);
            double sin2 = Math.sin(angle);
            double cos2 = Math.cos(angle);
            angle = random.nextDouble() * Math.PI * 2.0;
            double sin3 = Math.sin(angle);
            double cos3 = Math.cos(angle);
            for (int index = 0; index < 4; ++index) {
                double x = (double)((index & 2) - 1) * size;
                double y = (double)((index + 1 & 2) - 1) * size;
                double aa = x * cos3 - y * sin3;
                double ab = y * cos3 + x * sin3;
                double dy = aa * sin2 + 0.0 * cos2;
                double ae = 0.0 * sin2 - aa * cos2;
                double dx = ae * sin1 - ab * cos1;
                double dz = ab * sin1 + ae * cos1;
                float texU = index >> 1 & 1;
                float texV = index + 1 >> 1 & 1;
                buffer.vertex(px + dx, py + dy, pz + dz).texture(texU, texV).next();
            }
        }
    }

    private void makeCylinder(BufferBuilder buffer, int segments, double height, double radius) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        for (int i = 0; i < segments; ++i) {
            double a1 = (double)i * Math.PI * 2.0 / (double)segments;
            double a2 = (double)(i + 1) * Math.PI * 2.0 / (double)segments;
            double px1 = Math.sin(a1) * radius;
            double pz1 = Math.cos(a1) * radius;
            double px2 = Math.sin(a2) * radius;
            double pz2 = Math.cos(a2) * radius;
            float u0 = (float)i / (float)segments;
            float u1 = (float)(i + 1) / (float)segments;
            buffer.vertex(px1, -height, pz1).texture(u0, 0.0f).next();
            buffer.vertex(px1, height, pz1).texture(u0, 1.0f).next();
            buffer.vertex(px2, height, pz2).texture(u1, 1.0f).next();
            buffer.vertex(px2, -height, pz2).texture(u1, 0.0f).next();
        }
    }

    @FunctionalInterface
    interface BufferFunction {
        void make(BufferBuilder var1, double var2, double var4, int var6, long var7);
    }
}
