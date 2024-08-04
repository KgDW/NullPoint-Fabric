package me.nullpoint.asm.mixins;

import me.nullpoint.mod.modules.impl.miscellaneous.AutoReconnect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(DisconnectedScreen.class)
public abstract class MixinDisconnectedScreen extends Screen {
    @Shadow
    @Final
    private DirectionalLayoutWidget grid;
    @Unique private ButtonWidget reconnectBtn;
    @Unique private double time = AutoReconnect.INSTANCE.delay.getValueInt() * 20;

    protected MixinDisconnectedScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/DirectionalLayoutWidget;refreshPositions()V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addButtons(CallbackInfo ci, ButtonWidget buttonWidget) {
        AutoReconnect autoReconnect = AutoReconnect.INSTANCE;

        if (autoReconnect.lastServerConnection != null) {
            reconnectBtn = new ButtonWidget.Builder(Text.literal(getText()), button -> tryConnecting()).build();
            grid.add(reconnectBtn);

            grid.add(
                    new ButtonWidget.Builder(Text.literal("Toggle Auto Reconnect"), button -> {
                        autoReconnect.toggle();
                        reconnectBtn.setMessage(Text.literal(getText()));
                        time = autoReconnect.delay.getValueInt() * 20;
                    }).build()
            );
        }
    }

    @Override
    public void tick() {
        AutoReconnect autoReconnect = AutoReconnect.INSTANCE;
        if (!autoReconnect.isOn() || autoReconnect.lastServerConnection == null) return;

        if (time <= 0) {
            tryConnecting();
        } else {
            time--;
            if (reconnectBtn != null) reconnectBtn.setMessage(Text.literal(getText()));
        }
    }

    @Unique
    private String getText() {
        String reconnectText = "Reconnect";
        if (AutoReconnect.INSTANCE.isOn()) reconnectText += " " + String.format("(%.1f)", time / 20);
        return reconnectText;
    }

    @Unique
    private void tryConnecting() {
        var lastServer = AutoReconnect.INSTANCE.lastServerConnection;
        ConnectScreen.connect(new TitleScreen(), MinecraftClient.getInstance(), lastServer.left(), lastServer.right(), false);
    }
}
