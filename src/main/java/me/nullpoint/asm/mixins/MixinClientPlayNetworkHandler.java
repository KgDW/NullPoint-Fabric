package me.nullpoint.asm.mixins;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.impl.SendMessageEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    @Shadow
    public abstract void sendChatMessage(String content);

    @Unique
    private boolean nullpoint_ignoreChatMessage;

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (nullpoint_ignoreChatMessage) return;
        if (message.startsWith(Nullpoint.PREFIX)) {
            Nullpoint.COMMAND.command(message.split(" "));
            ci.cancel();
        } else {
            SendMessageEvent event = new SendMessageEvent(message);
            Nullpoint.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
            } else if (!event.message.equals(event.defaultMessage)) {
                nullpoint_ignoreChatMessage = true;
                sendChatMessage(event.message);
                nullpoint_ignoreChatMessage = false;
                ci.cancel();
            }
        }
    }
}
