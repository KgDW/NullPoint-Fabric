package me.nullpoint.asm.mixins;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.mod.modules.impl.miscellaneous.SilentDisconnect;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public class MixinClientCommonNetworkHandler {
    @Inject(method = "onDisconnected", at = @At("HEAD"), cancellable = true)
    private void onDisconnected(Text reason, CallbackInfo ci) {
        if (Wrapper.mc.player != null && Wrapper.mc.world != null && SilentDisconnect.INSTANCE.isOn()) {
            CommandManager.sendChatMessage("§4[!] §cDisconnect! reason: §7" + reason.getString());
            ci.cancel();
        }
    }

    @Inject(method = "sendPacket", at = @At("HEAD"))
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {
//        StackTraceElement callerStackTraceElement = Thread.currentThread().getStackTrace()[2];
//        String callerClassName = callerStackTraceElement.getClassName();
//        String callerMethodName = callerStackTraceElement.getMethodName();
//        CommandManager.sendChatMessage(String.format("caller1: %s caller2: %s", callerClassName,callerMethodName));
    }
}
