package me.nullpoint.asm.mixins;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.ShaderManager;
import me.nullpoint.mod.modules.impl.player.FreeCam;
import me.nullpoint.mod.modules.impl.render.NoInterp;
import me.nullpoint.mod.modules.impl.render.Shader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.nullpoint.api.utils.Wrapper.mc;
@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
	@Shadow
	protected abstract void renderEndSky(MatrixStack matrices);
	@Final
	@Shadow
	private MinecraftClient client;
	@Shadow
	@Final
	private EntityRenderDispatcher entityRenderDispatcher;

	@Inject(method = {"renderEntity"}, at = {@At("HEAD")}, cancellable = true)
	private void renderEntityHook(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
		if (NoInterp.INSTANCE.isOn() && entity != MinecraftClient.getInstance().player && entity instanceof PlayerEntity) {
			ci.cancel();
			double d = entity.getX();
			double e = entity.getY();
			double f = entity.getZ();
			float g = entity.getYaw();
			this.entityRenderDispatcher.render(entity, d - cameraX, e - cameraY, f - cameraZ, g, 0, matrices, vertexConsumers, this.entityRenderDispatcher.getLight(entity, tickDelta));
		}
	}


	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(F)V", ordinal = 0))
	void replaceShaderHook(PostEffectProcessor instance, float tickDelta) {
		ShaderManager.Shader shaders = Shader.INSTANCE.mode.getValue();
		if (Shader.INSTANCE.isOn() && mc.world != null) {
			Nullpoint.SHADER.setupShader(shaders, Nullpoint.SHADER.getShaderOutline(shaders));
		} else {
			instance.render(tickDelta);
		}
	}

	@Inject(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At(value = "HEAD"), cancellable = true)
	private void renderSkyHead(MatrixStack matrices, Matrix4f matrix4f, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo info) {
		if (Shader.INSTANCE.isOn() && Shader.INSTANCE.sky.getValue()) {
			Nullpoint.SHADER.applyShader(() -> renderEndSky(matrices), Shader.INSTANCE.skyMode.getValue());
			info.cancel();
		}
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V"), index = 3)
	private boolean renderSetupTerrainModifyArg(boolean spectator) {
		return FreeCam.INSTANCE.isOn() || spectator;
	}
}
