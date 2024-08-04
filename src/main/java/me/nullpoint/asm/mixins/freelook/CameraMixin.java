package me.nullpoint.asm.mixins.freelook;

import me.nullpoint.mod.modules.impl.player.freelook.FreeLook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private float cameraY;
    @Unique
    private float lastUpdate;

    @Inject(method = "update", at = @At("HEAD"))
    private void onCameraUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        var camera = FreeLook.INSTANCE.getCameraState();

        if (camera.doLock) {
            var limitNegativeYaw = camera.originalYaw() - 180;
            var limitPositiveYaw = camera.originalYaw() + 180;

            // TODO: make smoother transition if limit reached
            if (camera.lookYaw > limitPositiveYaw)
                camera.lookYaw = limitPositiveYaw;

            if (camera.lookYaw < limitNegativeYaw)
                camera.lookYaw = limitNegativeYaw;
        }
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void modifyRotationArgs(Args args) {
        var camera = FreeLook.INSTANCE.getCameraState();

        if (camera.doLock) {
            var yaw = camera.lookYaw;
            var pitch = camera.lookPitch;

            if (MinecraftClient.getInstance().options.getPerspective().isFrontView()) {
                yaw -= 180;
                pitch = -pitch;
            }

            args.set(0, yaw);
            args.set(1, pitch);
        } else if (camera.doTransition) {
            var delta = (getCurrentTime() - lastUpdate);

            var steps = 1.2f;
            var speed = 2f;
            var yawDiff = camera.lookYaw - camera.originalYaw();
            var pitchDiff = camera.lookPitch - camera.originalPitch();
            var yawStep = speed * (yawDiff * steps);
            var pitchStep = speed * (pitchDiff * steps);
            var yaw = MathHelper.stepTowards(camera.lookYaw, camera.originalYaw(), yawStep * delta);
            var pitch = MathHelper.stepTowards(camera.lookPitch, camera.originalPitch(), pitchStep * delta);

            camera.lookYaw = yaw;
            camera.lookPitch = pitch;

            args.set(0, yaw);
            args.set(1, pitch);

            camera.doTransition =
                    (int) camera.originalYaw() != (int) camera.lookYaw ||
                            (int) camera.originalPitch() != (int) camera.lookPitch;
        }

        lastUpdate = getCurrentTime();
    }

    @Unique
    private float getCurrentTime() {
        return (float) (System.nanoTime() * 0.00000001);
    }
}
