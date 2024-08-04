package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BlockerESP extends Module {

	public BlockerESP() {
		super("BlockerESP", Category.Render);
	}

	private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
	private final BooleanSetting box = add(new BooleanSetting("Box", true));
	private final BooleanSetting outline = add(new BooleanSetting("Outline", true));
	private final BooleanSetting burrow = add(new BooleanSetting("Burrow", true));
	private final BooleanSetting surround = add(new BooleanSetting("Surround", true));
	final List<BlockPos> renderList = new ArrayList<>();
    @Override
	public void onRender3D(MatrixStack matrixStack, float partialTicks) {
		renderList.clear();
		float pOffset = (float) CombatSetting.getOffset();
		for (Entity player : CombatUtil.getEnemies(10)) {
			if (burrow.getValue()) {
				float[] offset = new float[]{-pOffset, 0f, pOffset};
				for (float x : offset) {
					for (float z : offset) {
						BlockPos tempPos;
						if (isObsidian(tempPos = new BlockPosX(player.getPos().add(x, 0, z)))) {
							renderList.add(tempPos);
						}
						if (isObsidian(tempPos = new BlockPosX(player.getPos().add(x, 0.5, z)))) {
							renderList.add(tempPos);
						}
					}
				}
			}

			if (surround.getValue()) {
				BlockPos pos = EntityUtil.getEntityPos(player, true);
				if (!BlockUtil.isHole(pos)) continue;
				for (Direction i : Direction.values()) {
					if (i == Direction.UP || i == Direction.DOWN) continue;
					if (isObsidian(pos.offset(i))) {
						renderList.add(pos.offset(i));
					}
				}
			}
		}
		for (BlockPos pos : renderList) {
			Render3DUtil.draw3DBox(matrixStack, new Box(pos), color.getValue(), outline.getValue(), box.getValue());
		}
	}

	private boolean isObsidian(BlockPos pos) {
		return (BlockUtil.getBlock(pos) == Blocks.OBSIDIAN || BlockUtil.getBlock(pos) == Blocks.ENDER_CHEST) && !renderList.contains(pos);
	}
}
