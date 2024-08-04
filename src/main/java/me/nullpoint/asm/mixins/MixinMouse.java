package me.nullpoint.asm.mixins;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse implements Wrapper {
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if(button != 3 && button != 4){
            return;
        }
        if (mc.currentScreen instanceof ClickGuiScreen && action == 1 && Nullpoint.MODULE.setBind(button)) {
            return;
        }
        if (action == 1) {
            Nullpoint.MODULE.onKeyPressed(button);
        }
        if (action == 0) {
            Nullpoint.MODULE.onKeyReleased(button);
        }
    }
}
