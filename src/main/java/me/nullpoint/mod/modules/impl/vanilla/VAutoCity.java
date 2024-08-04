/**
 * Anti-Invis Module
 */
package me.nullpoint.mod.modules.impl.vanilla;


import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.combat.SilentDouble;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import static me.nullpoint.api.utils.world.BlockUtil.getBlock;


public class VAutoCity extends Module {
	public static VAutoCity INSTANCE;
	private final BooleanSetting burrow = add(new BooleanSetting("Burrow", true));
	private final BooleanSetting surround = add(new BooleanSetting("Surround", true));
	public final SliderSetting targetRange =
			add(new SliderSetting("TargetRange", 6.0, 0.0, 400.0, 0.1));
	public VAutoCity() {
		super("VAutoCity", Category.Combat);
		INSTANCE = this;
	}

	@Override
	public void onUpdate() {
		PlayerEntity player = CombatUtil.getClosestEnemy(targetRange.getValue());
		if (player == null) return;
		doBreak(player);
		doBreak(player);
	}

	private void doBreak(PlayerEntity player) {
		BlockPos pos = EntityUtil.getEntityPos(player, true);
		if (burrow.getValue()) {
			double[] yOffset = new double[]{-0.8, 0.5, 1.1};
			double[] xzOffset = new double[]{0.3, -0.3, 0};
			for (PlayerEntity entity : CombatUtil.getEnemies(targetRange.getValue())) {
				for (double y : yOffset) {
					for (double x : xzOffset) {
						for (double z : xzOffset) {
							BlockPos offsetPos = new BlockPosX(entity.getX() + x, entity.getY() + y, entity.getZ() + z);
							if (isObsidian(offsetPos) && offsetPos.equals(VSpeedMine.breakPos)) {
								return;
							}
						}
					}
				}
			}

			yOffset = new double[]{0.5, 1.1};
			for (double y : yOffset) {
				for (double offset : xzOffset) {
					BlockPos offsetPos = new BlockPosX(player.getX() + offset, player.getY() + y, player.getZ() + offset);
					if (isObsidian(offsetPos)) {
						VSpeedMine.INSTANCE.mine(offsetPos);
						return;
					}
				}
			}
			for (double y : yOffset) {
				for (double offset : xzOffset) {
					for (double offset2 : xzOffset) {
						BlockPos offsetPos = new BlockPosX(player.getX() + offset2, player.getY() + y, player.getZ() + offset);
						if (isObsidian(offsetPos)) {
							VSpeedMine.INSTANCE.mine(offsetPos);
							return;
						}
					}
				}
			}
		}
		if (surround.getValue()) {
			for (Direction i : Direction.values()) {
				if (i == Direction.UP || i == Direction.DOWN) continue;
				if (BlockUtil.isAir(pos.offset(i)) && !player.getBoundingBox().intersects(new Box(pos.offset(i)))) {
					return;
				}
			}
			for (Direction i : Direction.values()) {
				if (i == Direction.UP || i == Direction.DOWN) continue;
				if (!VSpeedMine.godBlocks.contains(getBlock(pos.offset(i))) && !(getBlock(pos.offset(i)) instanceof BedBlock)) {
					VSpeedMine.INSTANCE.mine(pos.offset(i));
					return;
				}
			}
		}
	}

	private boolean isObsidian(BlockPos pos) {
		return (getBlock(pos) == Blocks.OBSIDIAN || getBlock(pos) == Blocks.ENDER_CHEST) && BlockUtil.getClickSideStrict(pos) != null && (!pos.equals(VSpeedMine.secondPos) || !(mc.player.getMainHandStack().getItem() instanceof PickaxeItem || SilentDouble.INSTANCE.isOn()));
	}
}