package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.DeathEvent;
import me.nullpoint.api.events.impl.TotemEvent;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.Notify;
import me.nullpoint.mod.modules.impl.client.UIModule;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import net.minecraft.entity.player.PlayerEntity;

public class PopCounter
        extends Module {

    public static PopCounter INSTANCE;
    private final EnumSetting<PopCounter.type> notitype = add(new EnumSetting<>("Type", type.Notify));
    public final BooleanSetting unPop =
            add(new BooleanSetting("Dead", true));
    public PopCounter() {
        super("PopCounter", "Counts players totem pops", Category.Misc);
        INSTANCE = this;
    }

    @EventHandler
    public void onPlayerDeath(DeathEvent event) {
        PlayerEntity player = event.getPlayer();
        if (Nullpoint.POP.popContainer.containsKey(player.getName().getString())) {
            int l_Count = Nullpoint.POP.popContainer.get(player.getName().getString());
            if (l_Count == 1) {
                if (player.equals(mc.player)) {
                    sendMessage("§fYou§r died after popping " + "§f" + l_Count + "§r totem.", player.getId());
                } else {
                    sendMessage("§f" + player.getName().getString() + "§r died after popping " + "§f" + l_Count + "§r totem.", player.getId());
                }
            } else {
                if (player.equals(mc.player)) {
                    sendMessage("§fYou§r died after popping " + "§f" + l_Count + "§r totems.", player.getId());
                } else {
                    sendMessage("§f" + player.getName().getString() + "§r died after popping " + "§f" + l_Count + "§r totems.", player.getId());
                }
            }
        } else if (unPop.getValue()) {
            if (player.equals(mc.player)) {
                sendMessage("§fYou§r died.", player.getId());
            } else {
                sendMessage("§f" + player.getName().getString() + "§r died.", player.getId());
            }
        }
    }

    @EventHandler
    public void onTotem(TotemEvent event) {
        PlayerEntity player = event.getPlayer();
        int l_Count = 1;
        if (Nullpoint.POP.popContainer.containsKey(player.getName().getString())) {
            l_Count = Nullpoint.POP.popContainer.get(player.getName().getString());
        }
        if (l_Count == 1) {
            if (player.equals(mc.player)) {
                sendMessage("§fYou§r popped " + "§f" + l_Count + "§r totem.", player.getId());
            } else {
                sendMessage("§f" + player.getName().getString() + " §rpopped " + "§f" + l_Count + "§r totems.", player.getId());
            }
        } else {
            if (player.equals(mc.player)) {
                sendMessage("§fYou§r popped " + "§f" + l_Count + "§r totem.", player.getId());
            } else {
                sendMessage("§f" + player.getName().getString() + " §rhas popped " + "§f" + l_Count + "§r totems.", player.getId());
            }
        }
    }
    
    public void sendMessage(String message, int id) {
        if (!nullCheck()) {
            if(notitype.getValue() == type.Notify){
                sendNotify("§6[!] " + message);
            }else if(notitype.getValue() == type.Both) {
                CommandManager.sendChatMessageWidthId("§6[!] " + message, id);
                sendNotify("§6[!] " + message);
            }else if(notitype.getValue() == type.Chat) {
                CommandManager.sendChatMessageWidthId("§6[!] " + message, id);
            }
        }
    }
    public enum type {
        Notify,
        Chat,
        Both
    }
}

