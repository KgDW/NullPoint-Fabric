package me.nullpoint.asm.mixins;

import me.nullpoint.mod.modules.impl.render.Ambience;
import me.nullpoint.mod.modules.impl.render.NoRender;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.awt.*;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {
	@ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImage;setColor(III)V"))
	private void update(Args args) {
		if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.worldColor.booleanValue) {
			args.set(2, new Color(Ambience.INSTANCE.worldColor.getValue().getBlue(), Ambience.INSTANCE.worldColor.getValue().getGreen(), Ambience.INSTANCE.worldColor.getValue().getRed()).getRGB());
		}
	}

	@Inject(method = "getDarknessFactor(F)F", at = @At("HEAD"), cancellable = true)
	private void getDarknessFactor(float tickDelta, CallbackInfoReturnable<Float> info) {
		if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.darkness.getValue()) info.setReturnValue(0.0f);
	}
}
