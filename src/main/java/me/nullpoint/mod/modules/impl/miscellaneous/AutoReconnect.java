package me.nullpoint.mod.modules.impl.miscellaneous;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.ServerConnectBeginEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class AutoReconnect extends Module {
    public final SliderSetting delay =
            add(new SliderSetting("Delay", 3, 0, 20));
    public static AutoReconnect INSTANCE;
    public Pair<ServerAddress, ServerInfo> lastServerConnection;
    public AutoReconnect(){
        super("AutoReconnect", Category.Misc);
        INSTANCE = this;
    }

    @EventHandler
    private void onGameJoined(ServerConnectBeginEvent event) {
        lastServerConnection = new ObjectObjectImmutablePair<>(event.getAddress(), event.getInfo());
    }

}
