package me.nullpoint.asm.mixins;

import me.nullpoint.mod.modules.impl.render.Ambience;
import me.nullpoint.mod.modules.impl.render.NoRender;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld extends World {
    protected MixinClientWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void onGetSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> info) {
        if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.sky.booleanValue) {
            Color sky = Ambience.INSTANCE.sky.getValue();
            info.setReturnValue(new Vec3d(sky.getRed() / 255.0, sky.getGreen() / 255.0, sky.getBlue() / 255.0));
        }
    }

    @Override
    public float getRainGradient(float delta) {
        return NoRender.INSTANCE.isOn() && NoRender.INSTANCE.weather.getValue() ? 0 : super.getRainGradient(delta);
    }

    @Override
    public float getThunderGradient(float delta) {
        return NoRender.INSTANCE.isOn() && NoRender.INSTANCE.weather.getValue() ? 0 : super.getThunderGradient(delta);
    }

}
