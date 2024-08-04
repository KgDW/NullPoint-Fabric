package me.nullpoint.mod.modules.impl.player;


import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.miscellaneous.AutoPearl;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PearlClip extends Module {
    public static PearlClip INSTANCE;

    public PearlClip() {
        super("PearlClip", Category.Misc);
        INSTANCE = this;
    }
    public final BooleanSetting autoYaw =
            add(new BooleanSetting("AutoYaw", true));
    public final BooleanSetting bypass =
            add(new BooleanSetting("Bypass", true));

    @Override
    public void onEnable() {
        if (nullCheck()) {
            disable();
            return;
        }
        Vec3d targetPos = new Vec3d(mc.player.getX() + MathHelper.clamp(roundToClosest(mc.player.getX(), Math.floor(mc.player.getX()) + 0.241, Math.floor(mc.player.getX()) + 0.759) - mc.player.getX(), -0.03, 0.03), mc.player.getY(), mc.player.getZ() + MathHelper.clamp(roundToClosest(mc.player.getZ(), Math.floor(mc.player.getZ()) + 0.241, Math.floor(mc.player.getZ()) + 0.759) - mc.player.getZ(), -0.03, 0.03));
        AutoPearl.INSTANCE.throwPearl(autoYaw.getValue() ? EntityUtil.getLegitRotations(targetPos)[0] : mc.player.getYaw(), bypass.getValue() ? 89 : 80);
        disable();
    }

    private double roundToClosest(double num, double low, double high) {
        double d1 = num - low;
        double d2 = high - num;

        if (d2 > d1) {
            return low;

        } else {
            return high;
        }
    }
}