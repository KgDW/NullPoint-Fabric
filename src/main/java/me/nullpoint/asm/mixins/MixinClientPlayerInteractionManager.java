package me.nullpoint.asm.mixins;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.impl.ClickBlockEvent;
import me.nullpoint.mod.modules.impl.exploit.MineTweak;
import me.nullpoint.mod.modules.impl.player.Reach;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

	@Shadow
	private ItemStack selectedStack;

	@ModifyVariable(method = "isCurrentlyBreaking", at = @At("STORE"))
	private ItemStack stack(ItemStack stack) {
		return MineTweak.INSTANCE.noReset() ? this.selectedStack : stack;
	}

	@ModifyConstant(method = "updateBlockBreakingProgress", constant = @Constant(intValue = 5))
	private int MiningCooldownFix(int value) {
		return MineTweak.INSTANCE.noDelay() ? 0 : value;
	}

	@Inject(method = "cancelBlockBreaking", at = @At("HEAD"), cancellable = true)
	private void hookCancelBlockBreaking(CallbackInfo callbackInfo) {
		if (MineTweak.INSTANCE.noAbort())
			callbackInfo.cancel();
	}
	@Inject(at = { @At("HEAD") }, method = { "getReachDistance()F" }, cancellable = true)
	private void onGetReachDistance(CallbackInfoReturnable<Float> ci) {
		Reach reachHack = Reach.INSTANCE;
		if (reachHack.isOn()) {
			ci.setReturnValue(reachHack.distance.getValueFloat());
		}
	}

	@Inject(at = { @At("HEAD") }, method = { "hasExtendedReach()Z" }, cancellable = true)
	private void hasExtendedReach(CallbackInfoReturnable<Boolean> cir) {
		Reach reachHack = Reach.INSTANCE;
		if (reachHack.isOn())
			cir.setReturnValue(true);
	}

	@Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
	private void onAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		ClickBlockEvent event = new ClickBlockEvent(pos);
		Nullpoint.EVENT_BUS.post(event);
		if (event.isCancelled()) {
			cir.setReturnValue(false);
		}
	}
}
