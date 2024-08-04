package me.nullpoint.asm.mixins;

import me.nullpoint.mod.modules.impl.movement.Velocity;
import me.nullpoint.mod.modules.impl.render.NoRender;
import me.nullpoint.mod.modules.impl.render.Shader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Entity.class)
public class MixinEntity {

	@Inject(at = {@At("HEAD")}, method = "isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z", cancellable = true)
	private void onIsInvisibleCheck(PlayerEntity message, CallbackInfoReturnable<Boolean> cir) {
		if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.invisible.getValue()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
	void isGlowingHook(CallbackInfoReturnable<Boolean> cir) {
		if (Shader.INSTANCE.isOn()) {
			cir.setReturnValue(Shader.INSTANCE.shouldRender((Entity) (Object) this));
		}
	}

	@ModifyArgs(method = "pushAwayFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
	private void pushAwayFromHook(Args args) {
		if ((Entity) (Object) this == MinecraftClient.getInstance().player) {
			double value = 1;
			if (Velocity.INSTANCE.isOn() && Velocity.INSTANCE.entityPush.getValue()) {
				value = 0;
			}
			args.set(0, (double) args.get(0) * value);
			args.set(1, (double) args.get(1) * value);
			args.set(2, (double) args.get(2) * value);
		}
	}

	@Inject(method = "isOnFire", at = @At("HEAD"), cancellable = true)
	void isOnFireHook(CallbackInfoReturnable<Boolean> cir) {
		if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.fireEntity.getValue()) {
			cir.setReturnValue(false);
		}
	}
}
