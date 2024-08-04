package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.Random;

public class BowBomb extends Module {

    private final Timer delayTimer = new Timer();
    private final BooleanSetting rotation = add(new BooleanSetting("Rotation", false));
    private final SliderSetting spoofs = add(new SliderSetting("Spoofs", 50, 0, 200, 1));
    private final EnumSetting<exploitEn> exploit = add(new EnumSetting<>("Exploit", exploitEn.Strong));
    private final BooleanSetting minimize = add(new BooleanSetting("Minimize", false));
    private final SliderSetting delay = add(new SliderSetting("Delay", 5f, 0f, 10f).setSuffix("s"));
    private final SliderSetting activeTime = this.add(new SliderSetting("ActiveTime", 0.4f, 0f, 3f).setSuffix("s"));
    private final Random random = new Random();
    public BowBomb() {
        super("BowBomb", "exploit", Category.Exploit);
    }
    private final Timer activeTimer = new Timer();


    @Override
    public void onUpdate() {
        if (!mc.player.isUsingItem() || mc.player.getActiveItem().getItem() != Items.BOW) {
            activeTimer.reset();
        }
    }

    @EventHandler
    protected void onPacketSend(PacketEvent.Send event) {
        if (nullCheck() || !delayTimer.passedMs((long) (delay.getValue() * 1000)) || !activeTimer.passedMs((long) (activeTime.getValue() * 1000))) return;
        if (event.getPacket() instanceof PlayerActionC2SPacket packet && packet.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM) { // && (mc.player.getActiveItemStack().getItem() == Items.BOW)

            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));

            if (exploit.getValue() == exploitEn.Fast) {
                for (int i = 0; i < getRuns(); i++) {
                    spoof(mc.player.getX(), minimize.getValue() ? mc.player.getY() : mc.player.getY() - 1e-10, mc.player.getZ(), true);
                    spoof(mc.player.getX(), mc.player.getY() + 1e-10, mc.player.getZ(), false);
                }
            }
            if (exploit.getValue() == exploitEn.Strong) {
                for (int i = 0; i < getRuns(); i++) {
                    spoof(mc.player.getX(), mc.player.getY() + 1e-10, mc.player.getZ(), false);
                    spoof(mc.player.getX(), minimize.getValue() ? mc.player.getY() : mc.player.getY() - 1e-10, mc.player.getZ(), true);
                }
            }
            if (exploit.getValue() == exploitEn.Phobos) {
                for (int i = 0; i < getRuns(); i++) {
                    spoof(mc.player.getX(), mc.player.getY() + 0.00000000000013, mc.player.getZ(), true);
                    spoof(mc.player.getX(), mc.player.getY() + 0.00000000000027, mc.player.getZ(), false);
                }
            }
            if (exploit.getValue() == exploitEn.Strict) {
                double[] strict_direction = new double[]{100f * -Math.sin(Math.toRadians(mc.player.getYaw())), 100f * Math.cos(Math.toRadians(mc.player.getYaw()))};
                for (int i = 0; i < getRuns(); i++) {
                    if (random.nextBoolean()) {
                        spoof(mc.player.getX() - strict_direction[0], mc.player.getY(), mc.player.getZ() - strict_direction[1], false);
                    } else {
                        spoof(mc.player.getX() + strict_direction[0], mc.player.getY(), mc.player.getZ() + strict_direction[1], true);
                    }
                }
            }

            delayTimer.reset();
        }
    }

    private void spoof(double x, double y, double z, boolean ground) {
        if (rotation.getValue()) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, mc.player.getYaw(), mc.player.getPitch(), ground));
        } else {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, ground));
        }
    }

    private int getRuns() {
        return spoofs.getValueInt();
    }

    private enum exploitEn {
        Strong, Fast, Strict, Phobos
    }
}
