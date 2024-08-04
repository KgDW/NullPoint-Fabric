package me.nullpoint.mod.modules.impl.client;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.TotemEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class Desktop extends Module {
    private static InputStream inputStream = Nullpoint.class.getClassLoader().getResourceAsStream("assets/minecraft/icon.png");

    private static final Image image;

    static {
        try {
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    final TrayIcon icon = new TrayIcon(image, "NullPoint");
    private final BooleanSetting onlyTabbed =
            add(new BooleanSetting("OnlyTabbed", false));
    private final BooleanSetting visualRange =
            add(new BooleanSetting("VisualRange", true));
    private final BooleanSetting selfPop =
            add(new BooleanSetting("TotemPop", true));
    private final BooleanSetting mention =
            add(new BooleanSetting("Mention", true));
    private final BooleanSetting dm =
            add(new BooleanSetting("DM", true));
    private final List<Entity> knownPlayers = new ArrayList<>();
    private List<Entity> players;

    public Desktop() {
        super("Desktop", "Desktop notifications", Category.Client);
    }

    @Override
    public void onDisable() {
        knownPlayers.clear();

        removeIcon();
    }

    @Override
    public void onEnable() {
        addIcon();
    }


    @Override
    public void onUpdate() {
        if (nullCheck() || !visualRange.getValue()) return;

        try {
            if (onlyTabbed.getValue()) {
                return;
            }

        } catch (Exception ignored) {

        }

        players = mc.world.getPlayers().stream().filter(Objects::nonNull).collect(Collectors.toList());

        try {
            for (Entity entity : players) {

                if (entity instanceof PlayerEntity
                        && !entity.getName().equals(mc.player.getName())
                        && !knownPlayers.contains(entity)
                        && !Nullpoint.FRIEND.isFriend(entity.getName().getString())) {

                    knownPlayers.add(entity);

                    icon.displayMessage("NullPoint", entity.getName() + " has entered your visual range!", TrayIcon.MessageType.INFO);
                }
            }
        } catch (Exception ignored) {

        }

        try {
            knownPlayers.removeIf(entity -> entity instanceof PlayerEntity
                    && !entity.getName().equals(mc.player.getName())
                    && !players.contains(entity));

        } catch (Exception ignored) {

        }
    }

    @EventHandler
    public void onTotemPop(TotemEvent event) {
        if (nullCheck() || event.getPlayer() != mc.player || !selfPop.getValue()) return;

        icon.displayMessage("NullPoint", "You are popping!", TrayIcon.MessageType.WARNING);
    }

    @EventHandler
    public void onClientChatReceived(PacketEvent.Receive event) {
        if (nullCheck()) return;
        if(event.getPacket() instanceof GameMessageS2CPacket e){
            String message = String.valueOf(e.content());

            if (message.contains(mc.player.getName().getString()) && mention.getValue()) {
                icon.displayMessage("NullPoint", "New chat mention!", TrayIcon.MessageType.INFO);
            }
            if (message.contains("whispers:") && dm.getValue()) {
                icon.displayMessage("NullPoint", "New direct message!", TrayIcon.MessageType.INFO);
            }
        }
    }

    private void addIcon() {
        SystemTray tray = SystemTray.getSystemTray();

        icon.setImageAutoSize(true);
        icon.setToolTip("NullPoint" + Nullpoint.VERSION);

        try {
            tray.add(icon);

        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private void removeIcon() {
        SystemTray tray = SystemTray.getSystemTray();
        tray.remove(icon);
    }
}

