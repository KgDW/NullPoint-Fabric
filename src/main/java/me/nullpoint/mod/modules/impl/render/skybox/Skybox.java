package me.nullpoint.mod.modules.impl.render.skybox;

import me.nullpoint.mod.modules.Beta;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.combat.WebAura;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.impl.client.rendering.DimensionRenderingRegistryImpl;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Map;

@Beta
public class Skybox extends Module {

    public static Skybox INSTANCE;
    public final ColorSetting color =
            add(new ColorSetting("Color", new Color(0.77f, 0.31f, 0.73f)));
    public final ColorSetting color2 =
            add(new ColorSetting("Color2", new Color(0.77f, 0.31f, 0.73f)));
    public final ColorSetting color3 =
            add(new ColorSetting("Color3", new Color(0.77f, 0.31f, 0.73f)));
    public final ColorSetting color4 =
            add(new ColorSetting("Color4", new Color(0.77f, 0.31f, 0.73f)));
    public final ColorSetting color5 =
            add(new ColorSetting("Color5", new Color(255, 255 ,255)));
    final BooleanSetting stars =
            add(new BooleanSetting("Stars", true));
    public Skybox() {
        super("Skybox", "Custom skybox", Category.Render);
        INSTANCE = this;
    }

    public static final CustomSkyRenderer skyRenderer = new CustomSkyRenderer();

    @Override
    public void onEnable() {
        try {
            Field field = DimensionRenderingRegistryImpl.class.getDeclaredField("SKY_RENDERERS");
            field.setAccessible(true);
            Map<RegistryKey<World>, DimensionRenderingRegistry.SkyRenderer> SKY_RENDERERS = (Map<RegistryKey<World>, DimensionRenderingRegistry.SkyRenderer>) field.get(null);
            SKY_RENDERERS.putIfAbsent(World.OVERWORLD, skyRenderer);
            SKY_RENDERERS.putIfAbsent(World.NETHER, skyRenderer);
            SKY_RENDERERS.putIfAbsent(World.END, skyRenderer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            Field field = DimensionRenderingRegistryImpl.class.getDeclaredField("SKY_RENDERERS");
            field.setAccessible(true);
            Map<RegistryKey<World>, DimensionRenderingRegistry.SkyRenderer> SKY_RENDERERS = (Map<RegistryKey<World>, DimensionRenderingRegistry.SkyRenderer>) field.get(null);
            SKY_RENDERERS.remove(World.OVERWORLD, skyRenderer);
            SKY_RENDERERS.remove(World.NETHER, skyRenderer);
            SKY_RENDERERS.remove(World.END, skyRenderer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}