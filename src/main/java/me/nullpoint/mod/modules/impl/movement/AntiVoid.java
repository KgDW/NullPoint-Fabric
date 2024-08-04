package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;


public class AntiVoid extends Module {

    private final SliderSetting voidHeight =
            add(new SliderSetting("VoidHeight", -64, -64, 319, 1));
    private final SliderSetting height =
            add(new SliderSetting("Height", 100, -40, 256, 1));

    public AntiVoid() {
        super("AntiVoid", "Allows you to fly over void blocks", Category.Movement);
    }

    @Override
    public void onUpdate() {
        boolean isVoid = true;

        for (int i = (int) mc.player.getY(); i > voidHeight.getValueInt() - 1; --i) {
            if (mc.world.getBlockState(new BlockPosX(mc.player.getX(), i, mc.player.getZ())).getBlock() != Blocks.AIR) {
                isVoid = false;
                break;
            }
        }
        if (mc.player.getY() < height.getValue() + voidHeight.getValue() && isVoid) {
            MovementUtil.setMotionY(0);
        }
    }
}
