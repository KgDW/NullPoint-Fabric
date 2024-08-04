package me.nullpoint.asm.mixins;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.impl.BoatMoveEvent;
import me.nullpoint.mod.modules.impl.movement.EntityControl;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public class MixinBoatEntity {
    @Shadow private boolean pressingLeft;

    @Shadow private boolean pressingRight;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"), cancellable = true)
    private void onTickInvokeMove(CallbackInfo info) {
        BoatMoveEvent event = new BoatMoveEvent((BoatEntity) (Object) this);
        Nullpoint.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Redirect(method = "updatePaddles", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/vehicle/BoatEntity;pressingLeft:Z"))
    private boolean onUpdatePaddlesPressingLeft(BoatEntity boat) {
        if (EntityControl.INSTANCE.isOn() && EntityControl.INSTANCE.fly.getValue()) return false;
        return pressingLeft;
    }

    @Redirect(method = "updatePaddles", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/vehicle/BoatEntity;pressingRight:Z"))
    private boolean onUpdatePaddlesPressingRight(BoatEntity boat) {
        if (EntityControl.INSTANCE.isOn() && EntityControl.INSTANCE.fly.getValue()) return false;
        return pressingRight;
    }
}
