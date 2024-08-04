package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.TravelEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class ElytraFlyBypass extends Module {
    public static ElytraFlyBypass INSTANCE;
    private boolean hasElytra = false;
    public SliderSetting factor = add(new SliderSetting("UpPitch", 1.3f, 0.1f, 4.0f));

    public ElytraFlyBypass(){
        super("ElytraFlyBypass", Category.Movement);
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        for (ItemStack is : mc.player.getArmorItems()) {
            if (is.getItem() instanceof ElytraItem) {
                hasElytra = true;
                break;
            } else {
                hasElytra = false;
            }
        }
    }

    @EventHandler
    public void onMove(TravelEvent event) {
        if (nullCheck() || !hasElytra || !mc.player.isFallFlying()) return;
        float yaw = (float) Math.toRadians(mc.player.getYaw());

        mc.player.addVelocity(-MathHelper.sin(yaw) * factor.getValue() / 10, 0, MathHelper.cos(yaw) * factor.getValue() / 10);
    }


}
