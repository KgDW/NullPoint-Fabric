package me.nullpoint.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class CrystalPlaceESP extends Module {
    public CrystalPlaceESP() {
        super("CrystalPlaceESP", Category.Render);
    }
    BooleanSetting range=add(new BooleanSetting("Check Range",true)).setParent();
    SliderSetting rangeValue =add(new SliderSetting("Range",12,0,256,v->range.getValue()));
    ColorSetting color   =add(new ColorSetting("Color ",new Color(255,255,255,150)));

    SliderSetting animationTime  =add(new SliderSetting("AnimationTime ",500,0,1500));
    SliderSetting fadeSpeed  =add(new SliderSetting("FadeSpeed",500,0,1500,0.1));
    EnumSetting mode=add(new EnumSetting<Mode>("Mode",Mode.Normal));
    SliderSetting pointsNew =add(new SliderSetting("Points",3,1,10,v->mode.getValue()==Mode.Normal));
    SliderSetting interval    =add(new SliderSetting("Interval ",2,1,100,v->mode.getValue()==Mode.New));

    private final ConcurrentHashMap<EndCrystalEntity, RenderInfo> cryList = new ConcurrentHashMap<>();
    private final Timer timer = new Timer();

    public enum Mode {
        Normal,
        New
    }

    @EventHandler
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {


        for (Entity e : new Iterable<Entity>() {
            @Override
            public Iterator<Entity> iterator() {
                return mc.world.getEntities().iterator();
            }
        }) {
            if (!(e instanceof EndCrystalEntity)) continue;
            if (range.getValue() && mc.player.distanceTo(e) > rangeValue.getValue()) continue;
            if (!cryList.containsKey(e)) {
                cryList.put((EndCrystalEntity) e, new RenderInfo((EndCrystalEntity) e, System.currentTimeMillis()));
            }
        }

        if (mode.getValue().equals(Mode.Normal)) {
            cryList.forEach((e, renderInfo) -> draw(matrixStack, renderInfo.entity, renderInfo.time, renderInfo.time));
        } else if (mode.getValue().equals(Mode.New)) {
            var time = 0;
            for (int i = 0; i < pointsNew.getValue(); i++) {
                if (timer.passedMs(500)) {
                    int finalTime = time;
                    cryList.forEach((e, renderInfo) ->
                            draw(matrixStack, renderInfo.entity, renderInfo.time - finalTime, renderInfo.time - finalTime)
                    );
                }
                time += interval.getValue();
            }
        }

        cryList.forEach((e, renderInfo) -> {
            if (((System.currentTimeMillis() - renderInfo.time) > animationTime.getValue()) && !e.isAlive()) {
                cryList.remove(e);
            }
            if (((System.currentTimeMillis() - renderInfo.time) > animationTime.getValue()) && mc.player.distanceTo(e) > rangeValue.getValue()) {
                cryList.remove(e);
            }
        });

    }

    private void draw(MatrixStack matrixStack, EndCrystalEntity entity, long radTime, long heightTime) {
        var rad = System.currentTimeMillis() - radTime;
        var height = System.currentTimeMillis() - heightTime;
        if (rad <= animationTime.getValue()) {
            drawCircle3D(matrixStack, entity, rad / fadeSpeed.getValueFloat(), height / 1000F, color.getValue());
        }
    }

    public static void drawCircle3D(MatrixStack stack, Entity ent, float radius,float height, Color color) {
        Render3DUtil.setupRender();
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        GL11.glLineWidth(2);
        double x = ent.prevX + (ent.getX() - ent.prevX) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = ent.prevY + (ent.getY() - ent.prevY) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = ent.prevZ + (ent.getZ() - ent.prevZ) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        stack.push();
        stack.translate(x, y, z);

        Matrix4f matrix = stack.peek().getPositionMatrix();
        for (int i = 0; i <= 180; i++) {
            bufferBuilder.vertex(matrix, (float) (radius * Math.cos(i * 6.28 / 45)), 0f, (float) (radius * Math.sin(i * 6.28 / 45))).color(color.getRGB()).next();
        }

        tessellator.draw();
        Render3DUtil.endRender();
        stack.translate(-x, -y+height, -z);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        stack.pop();
    }


    @Override
    public void onDisable() {
        cryList.clear();
    }

    record RenderInfo(EndCrystalEntity entity, long time) {
    }
    
}
