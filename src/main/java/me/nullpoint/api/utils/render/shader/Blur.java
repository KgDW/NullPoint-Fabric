package me.nullpoint.api.utils.render.shader;

import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import net.minecraft.util.Identifier;

public class Blur {
    private static final ManagedShaderEffect blur = ShaderEffectManager.getInstance().manage(new Identifier("shaders/post/fade_in_blur.json"));
    private static final Uniform1f blurProgress = blur.findUniform1f("Progress");
    private static final Uniform1f blurRadius = blur.findUniform1f("Radius");
    public static void blur(float deltaTick, float radius) {
        blurProgress.set(1);
        blurRadius.set(radius);
        blur.render(deltaTick);
    }
}
