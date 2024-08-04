package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.eventbus.EventPriority;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.managers.RotateManager;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

public class SelfFlatten extends Module {
	public static SelfFlatten INSTANCE;
	public final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));
	private final BooleanSetting checkMine =
			add(new BooleanSetting("DetectMining", true, v -> page.getValue() == Page.General));
	private final BooleanSetting eatingPause =
			add(new BooleanSetting("EatingPause", true, v -> page.getValue() == Page.General));
	private final BooleanSetting inventory =
			add(new BooleanSetting("InventorySwap", true, v -> page.getValue() == Page.General));
	private final SliderSetting delay =
			add(new SliderSetting("Delay", 100, 0, 1000, v -> page.getValue() == Page.General));
	private final BooleanSetting rotate =
			add(new BooleanSetting("Rotate", true, v -> page.getValue() == Page.Rotate).setParent());
	private final BooleanSetting newRotate =
			add(new BooleanSetting("NewRotate", false, v -> rotate.isOpen() && page.getValue() == Page.Rotate));
	private final SliderSetting yawStep =
			add(new SliderSetting("YawStep", 0.3f, 0.1f, 1.0f, 0.01f, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
	private final BooleanSetting packet =
			add(new BooleanSetting("Packet", false, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
	private final BooleanSetting checkLook =
			add(new BooleanSetting("CheckLook", true, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
	private final SliderSetting fov =
			add(new SliderSetting("Fov", 5f, 0f, 30f, v -> rotate.isOpen() && newRotate.getValue() && checkLook.getValue() && page.getValue() == Page.Rotate));
	public SelfFlatten() {
		super("SelfFlatten", Category.Combat);
		INSTANCE = this;
	}

	private final Timer timer = new Timer();
	public Vec3d directionVec = null;
	private float lastYaw = 0;
	private float lastPitch = 0;

	@EventHandler(priority = EventPriority.HIGH - 2)
	public void onRotate(RotateEvent event) {
		if (newRotate.getValue() && directionVec != null) {
			float[] newAngle = injectStep(EntityUtil.getLegitRotations(directionVec), yawStep.getValueFloat());
			lastYaw = newAngle[0];
			lastPitch = newAngle[1];
			event.setYaw(lastYaw);
			event.setPitch(lastPitch);
		} else {
			lastYaw = Nullpoint.ROTATE.lastYaw;
			lastPitch = Nullpoint.ROTATE.lastPitch;
		}
	}

	@Override
	public void onUpdate() {
		if (!mc.player.isOnGround()) {
			return;
		}
		if (eatingPause.getValue() && EntityUtil.isUsing()) {
			return;
		}
		if (!timer.passedMs(delay.getValueInt())) return;
		directionVec = null;
		int oldSlot = mc.player.getInventory().selectedSlot;
		int block;
		if ((block = getBlock()) == -1) {
			return;
		}
		if (!EntityUtil.isInsideBlock()) return;

		BlockPos pos1 = new BlockPosX(mc.player.getX() + 0.6, mc.player.getY() + 0.5, mc.player.getZ() + 0.6).down();
		BlockPos pos2 = new BlockPosX(mc.player.getX() - 0.6, mc.player.getY() + 0.5, mc.player.getZ() + 0.6).down();
		BlockPos pos3 = new BlockPosX(mc.player.getX() + 0.6, mc.player.getY() + 0.5, mc.player.getZ() - 0.6).down();
		BlockPos pos4 = new BlockPosX(mc.player.getX() - 0.6, mc.player.getY() + 0.5, mc.player.getZ() - 0.6).down();

		if (!canPlace(pos1) && !canPlace(pos2) && !canPlace(pos3) && !canPlace(pos4)) {
			return;
		}
		doSwap(block);
		if (tryPlaceObsidian(pos1, rotate.getValue())) {

		}
		else if (tryPlaceObsidian(pos2, rotate.getValue())) {

		}
		else if (tryPlaceObsidian(pos3, rotate.getValue())) {

		}
		else {
			tryPlaceObsidian(pos4, rotate.getValue());
		}
		if (inventory.getValue()) {
			doSwap(block);
			EntityUtil.syncInventory();
		} else {
			doSwap(oldSlot);
		}
	}

	private boolean tryPlaceObsidian(BlockPos pos, boolean rotate) {
		if (canPlace(pos)) {
			if (checkMine.getValue() && BlockUtil.isMining(pos)) {
				return false;
			}
			Direction side;
			if ((side = BlockUtil.getPlaceSide(pos)) == null) return false;
			BlockUtil.placedPos.add(pos);
			clickBlock(pos.offset(side), side.getOpposite(), rotate, Hand.MAIN_HAND, true);
			timer.reset();
			return true;
		}
		return false;
	}

	public static void clickBlock(BlockPos pos, Direction side, boolean rotate, Hand hand, boolean packet) {
		Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
		if (rotate) {
			EntityUtil.faceVector(directionVec);
		}
		EntityUtil.swingHand(hand, CombatSetting.INSTANCE.swingMode.getValue());
		BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
		if (packet) {
			mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, BlockUtil.getWorldActionId(mc.world)));
		} else {
			mc.interactionManager.interactBlock(mc.player, hand, result);
		}
	}

	private boolean faceVector(Vec3d directionVec) {
		if (!newRotate.getValue()) {
			RotateManager.lastEvent.cancelRotate();
			EntityUtil.faceVector(directionVec);
			return true;
		} else {
			this.directionVec = directionVec;
			float[] angle = EntityUtil.getLegitRotations(directionVec);
			if (Math.abs(MathHelper.wrapDegrees(angle[0] - lastYaw)) < fov.getValueFloat() && Math.abs(MathHelper.wrapDegrees(angle[1] - lastPitch)) < fov.getValueFloat()) {
				if (packet.getValue()) EntityUtil.sendYawAndPitch(angle[0], angle[1]);
				return true;
			}
		}
		return !checkLook.getValue();
	}

	private float[] injectStep(float[] angle, float steps) {
		if (steps < 0.01f) steps = 0.01f;

		if (steps > 1) steps = 1;

		if (steps < 1 && angle != null) {
			float packetYaw = lastYaw;
			float diff = MathHelper.wrapDegrees(angle[0] - packetYaw);

			if (Math.abs(diff) > 90 * steps) {
				angle[0] = (packetYaw + (diff * ((90 * steps) / Math.abs(diff))));
			}

			float packetPitch = lastPitch;
			diff = angle[1] - packetPitch;
			if (Math.abs(diff) > 90 * steps) {
				angle[1] = (packetPitch + (diff * ((90 * steps) / Math.abs(diff))));
			}
		}

		return new float[]{
				angle[0],
				angle[1]
		};
	}

	private void doSwap(int slot) {
		if (inventory.getValue()) {
			InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
		} else {
			InventoryUtil.switchToSlot(slot);
		}
	}

	private boolean canPlace(BlockPos pos) {
		if (BlockUtil.getPlaceSide(pos) == null) {
			return false;
		}
		if (!BlockUtil.canReplace(pos)) {
			return false;
		}
		return !hasEntity(pos);
	}

	private boolean hasEntity(BlockPos pos) {
		for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
			if (entity == mc.player) continue;
			if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof ExperienceBottleEntity || entity instanceof ArrowEntity || entity instanceof EndCrystalEntity || entity instanceof ArmorStandEntity && CombatSetting.INSTANCE.obsMode.getValue())
				continue;
			return true;
		}
		return false;
	}

	private int getBlock() {
		if (inventory.getValue()) {
				return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
		} else {
				return InventoryUtil.findBlock(Blocks.OBSIDIAN);
		}
	}

	public enum Page {
		General,
		Rotate
	}
}