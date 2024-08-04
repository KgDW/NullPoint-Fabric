package me.nullpoint.mod.modules.impl.render;

import com.google.common.collect.Maps;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.asm.accessors.IEntity;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.Notify;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Map;
import java.util.UUID;

public class LogoutSpots extends Module {
    private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
    private final BooleanSetting box = add(new BooleanSetting("Box", true));
    private final BooleanSetting outline = add(new BooleanSetting("Outline", true));
    private final BooleanSetting text = add(new BooleanSetting("Text", true));
    private final BooleanSetting message = add(new BooleanSetting("Message", true));
    private final BooleanSetting notify = add(new BooleanSetting("Notify",true));

    private final Map<UUID, PlayerEntity> playerCache = Maps.newConcurrentMap();
    private final Map<UUID, PlayerEntity> logoutCache = Maps.newConcurrentMap();

    public LogoutSpots() {
        super("LogoutSpots", Category.Render);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof PlayerListS2CPacket packet) {
            if (packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
                for (PlayerListS2CPacket.Entry addedPlayer : packet.getPlayerAdditionEntries()) {
                    for (UUID uuid : logoutCache.keySet()) {
                        if (!uuid.equals(addedPlayer.profile().getId())) continue;
                        PlayerEntity player = logoutCache.get(uuid);
                        if(Notify.INSTANCE.isOn() && Notify.INSTANCE.type.getValue()== Notify.Notifys.type.Both) {
                            if (notify.getValue())
                                sendNotify("\u00a7e[!] \u00a7b" + player.getName().getString() + " \u00a7alogged back at X: " + (int) player.getX() + " Y: " + (int) player.getY() + " Z: " + (int) player.getZ());
                            if (message.getValue())
                                CommandManager.sendChatMessage("§e[!] §b" + player.getName().getString() + " §alogged back at X: " + (int) player.getX() + " Y: " + (int) player.getY() + " Z: " + (int) player.getZ());
                        }else if(Notify.INSTANCE.isOn() && Notify.INSTANCE.type.getValue()== Notify.Notifys.type.Notify) {
                            if (notify.getValue())
                                sendNotify("\u00a7e[!] \u00a7b" + player.getName().getString() + " \u00a7alogged back at X: " + (int) player.getX() + " Y: " + (int) player.getY() + " Z: " + (int) player.getZ());
                        }else if(Notify.INSTANCE.isOn() && Notify.INSTANCE.type.getValue()== Notify.Notifys.type.Chat) {
                            if (message.getValue())
                                CommandManager.sendChatMessage("§e[!] §b" + player.getName().getString() + " §alogged back at X: " + (int) player.getX() + " Y: " + (int) player.getY() + " Z: " + (int) player.getZ());
                        }
                        logoutCache.remove(uuid);
                    }
                }
            }
            playerCache.clear();
        } else if (event.getPacket() instanceof PlayerRemoveS2CPacket packet) {
            for (UUID uuid2 : packet.profileIds()) {
                for (UUID uuid : playerCache.keySet()) {
                    if (!uuid.equals(uuid2)) continue;
                    final PlayerEntity player = playerCache.get(uuid);
                    if (!logoutCache.containsKey(uuid)) {
                        if(Notify.INSTANCE.isOn() && Notify.INSTANCE.type.getValue()== Notify.Notifys.type.Both) {
                            if (notify.getValue())
                                sendNotify("\u00a7e[!] \u00a7b" + player.getName().getString() + " \u00a7alogged back at X: " + (int) player.getX() + " Y: " + (int) player.getY() + " Z: " + (int) player.getZ());
                            if (message.getValue())
                                CommandManager.sendChatMessage("§e[!] §b" + player.getName().getString() + " §clogged out at X: " + (int) player.getX() + " Y: " + (int) player.getY() + " Z: " + (int) player.getZ());
                        }else if(Notify.INSTANCE.isOn() && Notify.INSTANCE.type.getValue()== Notify.Notifys.type.Notify) {
                            if (notify.getValue())
                                sendNotify("\u00a7e[!] \u00a7b" + player.getName().getString() + " \u00a7alogged back at X: " + (int) player.getX() + " Y: " + (int) player.getY() + " Z: " + (int) player.getZ());
                        }else if(Notify.INSTANCE.isOn() && Notify.INSTANCE.type.getValue()== Notify.Notifys.type.Chat) {
                            if (message.getValue())
                                CommandManager.sendChatMessage("§e[!] §b" + player.getName().getString() + " §clogged out at X: " + (int) player.getX() + " Y: " + (int) player.getY() + " Z: " + (int) player.getZ());
                        }
                        logoutCache.put(uuid, player);
                    }
                }
            }
            playerCache.clear();
        }
    }

    @Override
    public void onEnable() {
        playerCache.clear();
        logoutCache.clear();
    }

    @Override
    public void onUpdate() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == null || player.equals(mc.player)) continue;
            playerCache.put(player.getGameProfile().getId(), player);
        }
    }

    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        for (UUID uuid : logoutCache.keySet()) {
            final PlayerEntity data = logoutCache.get(uuid);
            if (data == null) continue;
            Render3DUtil.draw3DBox(matrixStack, ((IEntity) data).getDimensions().getBoxAt(data.getPos()), color.getValue(), outline.getValue(), box.getValue());
            if (text.getValue()) {
                Render3DUtil.drawText3D(data.getName().getString(), new Vec3d(data.getX(), ((IEntity) data).getDimensions().getBoxAt(data.getPos()).maxY + 0.5, data.getZ()), ColorUtil.injectAlpha(color.getValue(), 255));
            }
        }
    }
}