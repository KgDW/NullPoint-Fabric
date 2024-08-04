package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Beta;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;

@Beta
public class AnchorAssist extends Module {
	public static AnchorAssist INSTANCE;
	private final BooleanSetting rotate =
			add(new BooleanSetting("Rotate", true));
	private final BooleanSetting inventory =
			add(new BooleanSetting("InventorySwap", true));
	private final BooleanSetting usingPause =
			add(new BooleanSetting("UsingPause", true));
	private final BooleanSetting checkMine =
			add(new BooleanSetting("CheckMine", false));
	private final SliderSetting range =
			add(new SliderSetting("TargetRange", 5.0, 0.0, 6.0, 0.1).setSuffix("m"));
	private final SliderSetting minDamage =
			add(new SliderSetting("MinDamage", 6.0, 0.0, 36.0, 0.1));
	private final SliderSetting delay =
			add(new SliderSetting("Delay", 0.0, 0.0, 0.5, 0.01).setSuffix("s"));
	private final Timer timer = new Timer();

	public AnchorAssist() {
		super("AnchorAssist", Category.Combat);
		INSTANCE = this;
	}

	@Override
	public String getInfo() {
		return foundPos != null ? "Helping" : null;
	}

	BlockPos foundPos;
	@Override
	public void onUpdate() {
		foundPos = null;
		int anchor = findBlock(Blocks.RESPAWN_ANCHOR);
		int glowstone = findBlock(Blocks.GLOWSTONE);
		int old = mc.player.getInventory().selectedSlot;
		if (anchor == -1) {
			return;
		}
		if (glowstone == -1) {
			return;
		}
		if (mc.player.isSneaking()) {
			return;
		}
		if (usingPause.getValue() && mc.player.isUsingItem()) {
			return;
		}
		if (!timer.passed((long) (delay.getValueFloat() * 1000))) {
			return;
		}
		timer.reset();
		double bestDamage = minDamage.getValue();
		ArrayList<AnchorAura.PlayerAndPredict> list = new ArrayList<>();
		for (PlayerEntity player : CombatUtil.getEnemies(range.getValue())) {
			list.add(new AnchorAura.PlayerAndPredict(player));
		}

		for (AnchorAura.PlayerAndPredict pap : list) {
			BlockPos pos = EntityUtil.getEntityPos(pap.player, true).up(2);
			if (mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR) {
				continue;
			}
			if (BlockUtil.clientCanPlace(pos, true)) {
				double damage = AnchorAura.INSTANCE.getAnchorDamage(pos, pap.player, pap.predict);
				if (damage >= bestDamage) {
					bestDamage = damage;
					foundPos = pos;
				}
			}
			for (Direction i : Direction.values()) {
				if (i == Direction.UP || i == Direction.DOWN) continue;
				if (BlockUtil.clientCanPlace(pos.offset(i), false)) {
					double damage = AnchorAura.INSTANCE.getAnchorDamage(pos.offset(i), pap.player, pap.predict);
					if (damage >= bestDamage) {
						bestDamage = damage;
						foundPos = pos.offset(i);
					}
				}
			}
		}
		if (foundPos != null && BlockUtil.getPlaceSide(foundPos, AnchorAura.INSTANCE.range.getValue()) == null) {
			BlockPos placePos;
			if ((placePos = getHelper(foundPos)) != null) {
				doSwap(anchor);
				BlockUtil.placeBlock(placePos, rotate.getValue());
				if (inventory.getValue()) {
					doSwap(anchor);
					EntityUtil.syncInventory();
				} else {
					doSwap(old);
				}
			}
		}
	}

	public BlockPos getHelper(BlockPos pos) {
		for (Direction i : Direction.values()) {
			if (checkMine.getValue() && BlockUtil.isMining(pos.offset(i))) continue;
			if (!BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite(), true)) continue;
			if (BlockUtil.canPlace(pos.offset(i))) return pos.offset(i);
		}
		return null;
	}

	public int findBlock(Block blockIn) {
		if (inventory.getValue()) {
			return InventoryUtil.findBlockInventorySlot(blockIn);
		} else {
			return InventoryUtil.findBlock(blockIn);
		}
	}

	private void doSwap(int slot) {
		if (inventory.getValue()) {
			InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
		} else {
			InventoryUtil.switchToSlot(slot);
		}
	}
}