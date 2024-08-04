package me.nullpoint.mod.modules.impl.combat;

import com.mojang.authlib.GameProfile;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.utils.combat.*;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Beta;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.SwingSide;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import static me.nullpoint.api.utils.world.BlockUtil.getWorldActionId;

@Beta
public class BedAura extends Module {
	public static BedAura INSTANCE;
	public final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));
	public final EnumSetting<Mode> mode = add(new EnumSetting<>("BedMod",  Mode.NullPoint));
	//General
	private final BooleanSetting yawDeceive = add(new BooleanSetting("YawDeceive", true, v -> page.getValue() == Page.General));
	private final BooleanSetting checkMine = add(new BooleanSetting("DetectMining", true, v -> page.getValue() == Page.General));
	private final BooleanSetting noUsing =
			add(new BooleanSetting("EatingPause", true, v -> page.getValue() == Page.General));
	private final EnumSetting<AnchorAura.CalcMode> calcMode = add(new EnumSetting<>("CalcMode", AnchorAura.CalcMode.OyVey, v -> page.getValue() == Page.General));
	private final EnumSetting<SwingSide> swingMode = add(new EnumSetting<>("Swing", SwingSide.Server, v -> page.getValue() == Page.General));
	private final SliderSetting antiSuicide =
			add(new SliderSetting("AntiSuicide", 3.0, 0.0, 10.0, v -> page.getValue() == Page.General));
	private final SliderSetting targetRange =
			add(new SliderSetting("TargetRange", 12.0, 0.0, 20.0, v -> page.getValue() == Page.General));
	private final SliderSetting updateDelay =
			add(new SliderSetting("UpdateDelay", 50, 0, 1000, v -> page.getValue() == Page.General));
	private final SliderSetting calcDelay =
			add(new SliderSetting("CalcDelay", 200, 0, 1000, v -> page.getValue() == Page.General));
	private final BooleanSetting inventorySwap =
			add(new BooleanSetting("InventorySwap", true, v -> page.getValue() == Page.General));
	//Rotate
	private final BooleanSetting rotate =
			add(new BooleanSetting("Rotate", true, v -> page.getValue() == Page.Rotate).setParent());
	private final BooleanSetting newRotate =
			add(new BooleanSetting("NewRotate", false, v -> rotate.isOpen() && page.getValue() == Page.Rotate));
	private final SliderSetting yawStep =
			add(new SliderSetting("YawStep", 0.3f, 0.1f, 1.0f, 0.01f, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
	private final BooleanSetting random =
			add(new BooleanSetting("Random", true, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
	private final BooleanSetting sync =
			add(new BooleanSetting("Sync", false, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
	private final BooleanSetting checkLook =
			add(new BooleanSetting("CheckLook", true, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
	private final SliderSetting fov =
			add(new SliderSetting("Fov", 5f, 0f, 30f, v -> rotate.isOpen() && newRotate.getValue() && checkLook.getValue() && page.getValue() == Page.Rotate));

	//Calc
	private final BooleanSetting place =
			add(new BooleanSetting("Place", true, v -> page.getValue() == Page.Calc));
	private final SliderSetting placeDelay =
			add(new SliderSetting("PlaceDelay", 300, 0, 1000, v -> page.getValue() == Page.Calc && place.getValue()));
	private final BooleanSetting Break =
			add(new BooleanSetting("Break", true, v -> page.getValue() == Page.Calc));
	private final SliderSetting breakDelay =
			add(new SliderSetting("BreakDelay", 300, 0, 1000, v -> page.getValue() == Page.Calc && Break.getValue()));
	private final SliderSetting range =
			add(new SliderSetting("Range", 5.0, 0.0, 6, v -> page.getValue() == Page.Calc));
	private final SliderSetting placeMinDamage =
			add(new SliderSetting("MinDamage", 5.0, 0.0, 36.0, v -> page.getValue() == Page.Calc));
	private final SliderSetting placeMaxSelf =
			add(new SliderSetting("MaxSelfDamage", 12.0, 0.0, 36.0, v -> page.getValue() == Page.Calc));
	private final BooleanSetting smart =
			add(new BooleanSetting("Smart", true, v -> page.getValue() == Page.Calc));
	private final BooleanSetting breakOnlyHasCrystal =
			add(new BooleanSetting("OnlyHasBed", false, v -> page.getValue() == Page.Calc && Break.getValue()));
	//Render
	private final BooleanSetting render =
			add(new BooleanSetting("Render", true, v -> page.getValue() == Page.Render));
	private final BooleanSetting shrink =
			add(new BooleanSetting("Shrink", true, v -> page.getValue() == Page.Render && render.getValue()));
	private final BooleanSetting outline =
			add(new BooleanSetting("Outline", true, v -> page.getValue() == Page.Render && render.getValue()).setParent());
	private final SliderSetting outlineAlpha =
			add(new SliderSetting("OutlineAlpha", 150, 0, 255, v -> outline.isOpen() && page.getValue() == Page.Render && render.getValue()));
	private final BooleanSetting box =
			add(new BooleanSetting("Box", true, v -> page.getValue() == Page.Render && render.getValue()).setParent());
	private final SliderSetting boxAlpha =
			add(new SliderSetting("BoxAlpha", 70, 0, 255, v -> box.isOpen() && page.getValue() == Page.Render && render.getValue()));
	private final BooleanSetting reset =
			add(new BooleanSetting("Reset", true, v -> page.getValue() == Page.Render && render.getValue()));
	private final ColorSetting color =
			add(new ColorSetting("Color", new Color(255, 255, 255), v -> page.getValue() == Page.Render && render.getValue()));
	private final SliderSetting animationTime =
			add(new SliderSetting("AnimationTime", 2f, 0f, 8f, v -> page.getValue() == Page.Render && render.getValue()));
	private final SliderSetting startFadeTime =
			add(new SliderSetting("StartFadeTime", 0.3d, 0d, 2d, 0.01, v -> page.getValue() == Page.Render && render.getValue()));
	private final SliderSetting fadeTime =
			add(new SliderSetting("FadeTime", 0.3d, 0d, 2d, 0.01, v -> page.getValue() == Page.Render && render.getValue()));
	//Predict
	private final SliderSetting predictTicks =
			add(new SliderSetting("PredictTicks", 4, 0, 10, v -> page.getValue() == Page.Predict));
	private final BooleanSetting terrainIgnore =
			add(new BooleanSetting("TerrainIgnore", true, v -> page.getValue() == Page.Predict));
	public BedAura() {
		super("BedAura", Category.Combat);
		INSTANCE = this;
	}
	public static BlockPos placePos;
	private final Timer delayTimer = new Timer();
	private final Timer calcTimer = new Timer();
	private final Timer breakTimer = new Timer();
	private final Timer placeTimer = new Timer();
	private final Timer noPosTimer = new Timer();
	private final FadeUtils fadeUtils = new FadeUtils(500);
	private final FadeUtils animation = new FadeUtils(500);
	double lastSize = 0;
	private PlayerEntity displayTarget;
	private float lastYaw = 0f;
	private float lastPitch = 0f;
	public float lastDamage;
	public Vec3d directionVec = null;
	private BlockPos renderPos = null;
	private Box lastBB = null;
	private Box nowBB = null;
	public enum Mode {
		NullPoint,
		Scanner
	}
	@Override
	public String getInfo() {
		if (displayTarget != null && placePos != null) {
			return displayTarget.getName().getString();
		}
		return super.getInfo();
	}

	@Override
	public void onEnable() {
		lastYaw = Nullpoint.ROTATE.lastYaw;
		lastPitch = Nullpoint.ROTATE.lastPitch;
	}

	@EventHandler()
	public void onRotate(RotateEvent event) {
		if (placePos != null && newRotate.getValue() && directionVec != null) {
			float[] newAngle = injectStep(EntityUtil.getLegitRotations(directionVec), yawStep.getValueFloat());
			lastYaw = newAngle[0];
			lastPitch = newAngle[1];
			if (random.getValue() && new Random().nextBoolean()) {
				lastPitch = Math.min(new Random().nextFloat() * 2 + lastPitch, 90);
			}
			event.setYaw(lastYaw);
			event.setPitch(lastPitch);
		} else {
			lastYaw = Nullpoint.ROTATE.lastYaw;
			lastPitch = Nullpoint.ROTATE.lastPitch;
		}
	}

	@EventHandler
	public void onUpdateWalking(UpdateWalkingEvent event) {
		update();
	}

	@Override
	public void onUpdate() {
		update();
	}

	private void update() {
		if (nullCheck()) return;
		animUpdate();
		if (!delayTimer.passedMs((long) updateDelay.getValue())) return;
		if (noUsing.getValue() && EntityUtil.isUsing()) {
			placePos = null;
			return;
		}
		if (mc.player.isSneaking()) {
			placePos = null;
			return;
		}
		if (mc.world.getRegistryKey().equals(World.OVERWORLD)) {
			placePos = null;
			return;
		}
		if (breakOnlyHasCrystal.getValue() && getBed() == -1) {
			placePos = null;
			return;
		}
		delayTimer.reset();
		if (calcTimer.passedMs(calcDelay.getValueInt())) {
			calcTimer.reset();
			placePos = null;
			lastDamage = 0f;
			ArrayList<PlayerAndPredict> list = new ArrayList<>();
			for (PlayerEntity target : CombatUtil.getEnemies(targetRange.getRange())) {
				list.add(new PlayerAndPredict(target));
			}
			PlayerAndPredict self = new PlayerAndPredict(mc.player);
			for (BlockPos pos : BlockUtil.getSphere((float) range.getValue())) {
				if (!canPlaceBed(pos) && !(BlockUtil.getBlock(pos) instanceof BedBlock)) continue;
				for (PlayerAndPredict pap : list) {
					float damage = calculateDamage(pos, pap.player, pap.predict);
					float selfDamage = calculateDamage(pos, self.player, self.predict);
					if (selfDamage > placeMaxSelf.getValue())
						continue;
					if (antiSuicide.getValue() > 0 && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount() - antiSuicide.getValue())
						continue;
					if (damage < EntityUtil.getHealth(pap.player)) {
						if (damage < placeMinDamage.getValueFloat()) continue;
						if (smart.getValue()) {
							if (damage < selfDamage) {
								continue;
							}
						}
					}
					if (placePos == null || damage > lastDamage) {
						displayTarget = pap.player;
						placePos = pos;
						lastDamage = damage;
					}
				}
			}
		}
		if (placePos != null) {
			doBed(placePos);
		}
	}

	public void doBed(BlockPos pos) {
		switch (mode.getValue()) {
			case NullPoint -> {
				if (canPlaceBed(pos) && !(BlockUtil.getBlock(pos) instanceof BedBlock)) {
					if (getBed() != -1) {
						doPlace(pos);
					}
				} else {
					doBreak(pos);
				}
			}
			case Scanner -> {
				doBreak(pos);
				doPlace(pos);
				doBreak(pos);
			}
		}

	}

	private void doBreak(BlockPos pos) {
		if (!Break.getValue()) return;
		if (mc.world.getBlockState(pos).getBlock() instanceof BedBlock) {
			Direction side = BlockUtil.getClickSide(pos);
			Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
			if (rotate.getValue()) {
				if (!faceVector(directionVec)) return;
			}
			if (!breakTimer.passedMs((long) breakDelay.getValue())) return;
			breakTimer.reset();
			EntityUtil.swingHand(Hand.MAIN_HAND, swingMode.getValue());
			BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
			mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, getWorldActionId(mc.world)));
		}
	}

	private void doPlace(BlockPos pos) {
		if (!place.getValue()) return;
		int bedSlot;
		if ((bedSlot = getBed()) == -1) {
			placePos = null;
			return;
		}

		int oldSlot = mc.player.getInventory().selectedSlot;
		Direction facing = null;
		for (Direction i : Direction.values()) {
			if (i == Direction.UP || i == Direction.DOWN) continue;
			if (BlockUtil.clientCanPlace(pos.offset(i), false) && BlockUtil.canClick(pos.offset(i).down()) && (!checkMine.getValue() || !BlockUtil.isMining(pos.offset(i)))) {
				facing = i;
				break;
			}
		}
		if (facing != null) {
			Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + Direction.UP.getVector().getX() * 0.5, pos.getY() + 0.5 + Direction.UP.getVector().getY() * 0.5, pos.getZ() + 0.5 + Direction.UP.getVector().getZ() * 0.5);
			if (rotate.getValue()) {
				if (!faceVector(directionVec)) return;
			}
			if (!placeTimer.passedMs((long) placeDelay.getValue())) return;
			placeTimer.reset();
			doSwap(bedSlot);
			if (yawDeceive.getValue()) HoleKick.pistonFacing(facing.getOpposite());
			BlockUtil.clickBlock(pos.offset(facing).down(), Direction.UP, false);
			if (rotate.getValue() && sync.getValue()) {
				EntityUtil.faceVector(directionVec);
			}
			if (inventorySwap.getValue()) {
				doSwap(bedSlot);
				EntityUtil.syncInventory();
			} else {
				doSwap(oldSlot);
			}
		}
	}

	
	@Override
	public void onRender3D(MatrixStack matrixStack, float partialTicks) {
		update();
		double quad = noPosTimer.passedMs(startFadeTime.getValue() * 1000L) ? fadeUtils.easeOutQuad() : 0;
		if (nowBB != null && render.getValue() && quad < 1) {
			Box bb = nowBB;
			if (shrink.getValue()) {
				bb = nowBB.shrink(quad * 0.5, quad * 0.5, quad * 0.5);
				bb = bb.shrink(-quad * 0.5, -quad * 0.5, -quad * 0.5);
			}
			if (this.box.getValue())
				Render3DUtil.drawFill(matrixStack, bb, ColorUtil.injectAlpha(color.getValue(), (int) (boxAlpha.getValue() * Math.abs(quad - 1))));
			if (outline.getValue())
				Render3DUtil.drawBox(matrixStack, bb, ColorUtil.injectAlpha(color.getValue(), (int) (outlineAlpha.getValue() * Math.abs(quad - 1))));
		} else if (reset.getValue()) nowBB = null;
	}
	private void animUpdate() {
		fadeUtils.setLength((long) (fadeTime.getValue() * 1000));
		if (placePos != null) {
			lastBB = new Box(placePos);
			noPosTimer.reset();
			if (nowBB == null) {
				nowBB = lastBB;
			}
			if (renderPos == null || !renderPos.equals(placePos)) {
				animation.setLength((animationTime.getValue() * 1000) <= 0 ? 0 :
						(long) ((Math.abs(nowBB.minX - lastBB.minX) + Math.abs(nowBB.minY - lastBB.minY) + Math.abs(nowBB.minZ - lastBB.minZ)) <= 5 ?
								(long) ((Math.abs(nowBB.minX - lastBB.minX) + Math.abs(nowBB.minY - lastBB.minY) + Math.abs(nowBB.minZ - lastBB.minZ)) * (animationTime.getValue() * 1000))
								: (animationTime.getValue() * 5000L))
				);
				animation.reset();
				renderPos = placePos;
			}
		}
		if (!noPosTimer.passedMs((long) (startFadeTime.getValue() * 1000))) {
			fadeUtils.reset();
		}
		double size = animation.easeOutQuad();
		if (nowBB != null && lastBB != null) {
			if (Math.abs(nowBB.minX - lastBB.minX) + Math.abs(nowBB.minY - lastBB.minY) + Math.abs(nowBB.minZ - lastBB.minZ) > 16) {
				nowBB = lastBB;
			}
			if (lastSize != size) {
				nowBB = new Box(nowBB.minX + (lastBB.minX - nowBB.minX) * size,
						nowBB.minY + (lastBB.minY - nowBB.minY) * size,
						nowBB.minZ + (lastBB.minZ - nowBB.minZ) * size,
						nowBB.maxX + (lastBB.maxX - nowBB.maxX) * size,
						nowBB.maxY + (lastBB.maxY - nowBB.maxY) * size,
						nowBB.maxZ + (lastBB.maxZ - nowBB.maxZ) * size
				);
				lastSize = size;
			}
		}
	}
	public int getBed() {
		return inventorySwap.getValue() ? InventoryUtil.findClassInventorySlot(BedItem.class) : InventoryUtil.findClass(BedItem.class);
	}

	private void doSwap(int slot) {
		if (inventorySwap.getValue()) {
			InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
		} else {
			InventoryUtil.switchToSlot(slot);
		}
	}

	public float calculateDamage(BlockPos pos, PlayerEntity player, PlayerEntity predict) {
		CombatUtil.modifyPos = pos;
		CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
		float damage = calculateDamage(pos.toCenterPos(), player, predict);
		CombatUtil.modifyPos = null;
		return damage;
	}

	public float calculateDamage(Vec3d pos, PlayerEntity player, PlayerEntity predict) {
		if (terrainIgnore.getValue()) {
			CombatUtil.terrainIgnore = true;
		}
		float damage = 0;
		switch (calcMode.getValue()) {
			case Meteor -> damage = (float) MeteorExplosionUtil.crystalDamage(player, pos, predict);
			case Thunder -> damage = ThunderExplosionUtil.calculateDamage(pos, player, predict, 6);
			case OyVey -> damage = OyveyExplosionUtil.calculateDamage(pos.getX(), pos.getY(), pos.getZ(), player, predict, 6);
			case Edit -> damage = ExplosionUtil.calculateDamage(pos.getX(), pos.getY(), pos.getZ(), player, predict, 6);
		}
		CombatUtil.terrainIgnore = false;
		return damage;
	}
	
	private boolean canPlaceBed(BlockPos pos) {
		if (BlockUtil.canReplace(pos) && (!checkMine.getValue() || !BlockUtil.isMining(pos))) {
			for (Direction i : Direction.values()) {
				if (i == Direction.UP || i == Direction.DOWN) continue;
				if (!BlockUtil.isStrictDirection(pos.offset(i).down(), Direction.UP)) continue;
				if (!isTrueFacing(pos.offset(i), i.getOpposite())) continue;
				if (BlockUtil.clientCanPlace(pos.offset(i), false) && BlockUtil.canClick(pos.offset(i).down()) && (!checkMine.getValue() || !BlockUtil.isMining(pos.offset(i)))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isTrueFacing(BlockPos pos, Direction facing) {
		if (yawDeceive.getValue()) return true;
		Vec3d hitVec = pos.toCenterPos().add(new Vec3d(0, -0.5, 0));
		return Direction.fromRotation(EntityUtil.getLegitRotations(hitVec)[0]) == facing;
	}

	public enum Page {
		General,
		Rotate,
		Calc,
		Predict,
		Render
	}

	public boolean faceVector(Vec3d directionVec) {
		if (!newRotate.getValue()) {
			EntityUtil.faceVector(directionVec);
			return true;
		} else {
			this.directionVec = directionVec;
			float[] angle = EntityUtil.getLegitRotations(directionVec);
			if (Math.abs(MathHelper.wrapDegrees(angle[0] - lastYaw)) < fov.getValueFloat() && Math.abs(MathHelper.wrapDegrees(angle[1] - lastPitch)) < fov.getValueFloat()) {
				if (sync.getValue()) EntityUtil.sendYawAndPitch(angle[0], angle[1]);
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
			float diff = MathHelper.wrapDegrees(angle[0] - lastYaw);

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
	public class PlayerAndPredict {
		final PlayerEntity player;
		final PlayerEntity predict;
		public PlayerAndPredict(PlayerEntity player) {
			this.player = player;
			if (predictTicks.getValueFloat() > 0) {
				predict = new PlayerEntity(mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {@Override public boolean isSpectator() {return false;} @Override public boolean isCreative() {return false;}};
				predict.setPosition(player.getPos().add(CombatUtil.getMotionVec(player, INSTANCE.predictTicks.getValueInt(), true)));
				predict.setHealth(player.getHealth());
				predict.prevX = player.prevX;
				predict.prevZ = player.prevZ;
				predict.prevY = player.prevY;
				predict.setOnGround(player.isOnGround());
				predict.getInventory().clone(player.getInventory());
				predict.setPose(player.getPose());
				for (StatusEffectInstance se : player.getStatusEffects()) {
					predict.addStatusEffect(se);
				}
			} else {
				predict = player;
			}
		}
	}
}
