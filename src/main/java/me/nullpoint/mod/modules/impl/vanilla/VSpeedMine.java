package me.nullpoint.mod.modules.impl.vanilla;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.eventbus.EventPriority;
import me.nullpoint.api.events.impl.ClickBlockEvent;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.entity.TPUtil;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.asm.accessors.IPlayerMoveC2SPacket;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.impl.combat.AnchorAura;
import me.nullpoint.mod.modules.impl.combat.AutoCrystal;
import me.nullpoint.mod.modules.settings.impl.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static me.nullpoint.api.utils.world.BlockUtil.getBlock;

public class VSpeedMine extends Module {
	public static final List<Block> godBlocks = Arrays.asList(Blocks.COMMAND_BLOCK, Blocks.LAVA_CAULDRON, Blocks.LAVA, Blocks.WATER_CAULDRON, Blocks.WATER, Blocks.BEDROCK, Blocks.BARRIER, Blocks.END_PORTAL, Blocks.NETHER_PORTAL, Blocks.END_PORTAL_FRAME);

	private final SliderSetting delay = add(new SliderSetting("Delay", 50, 0, 500, 1));
	private final SliderSetting damage = add(new SliderSetting("Damage", 0.7f, 0.0f, 2.0f, 0.01));
	private final SliderSetting range = add(new SliderSetting("Range", 60f, 3.0f, 300.0f, 0.1));
	private final SliderSetting maxBreak = add(new SliderSetting("MaxBreak", 3, 0, 20, 1));
	public final BooleanSetting preferWeb = add(new BooleanSetting("PreferWeb", true));
	private final BooleanSetting instant = add(new BooleanSetting("Instant", false));
	private final BooleanSetting cancelPacket = add(new BooleanSetting("CancelPacket", false));
	private final BooleanSetting wait = add(new BooleanSetting("Wait", true, v -> !instant.getValue()));
	private final BooleanSetting mineAir = add(new BooleanSetting("MineAir", true, v -> wait.getValue()));
	public final BooleanSetting farCancel = add(new BooleanSetting("FarCancel", false));
	public final BooleanSetting hotBar = add(new BooleanSetting("HotbarSwap", false));
	public final BooleanSetting ghostHand = add(new BooleanSetting("GhostHand", true));
	private final BooleanSetting checkGround = add(new BooleanSetting("CheckGround", true));
	private final BooleanSetting onlyGround = add(new BooleanSetting("OnlyGround", true));
	private final BooleanSetting doubleBreak = add(new BooleanSetting("DoubleBreak", true));
	private final BooleanSetting usingPause = add(new BooleanSetting("UsingPause", false));
	private final BooleanSetting swing = add(new BooleanSetting("Swing", true));
	private final BooleanSetting endSwing = add(new BooleanSetting("EndSwing", false));
	private final BooleanSetting bypassGround = add(new BooleanSetting("BypassGround", true));
	private final SliderSetting bypassTime = add(new SliderSetting("BypassTime", 400, 0, 2000, v -> bypassGround.getValue()));
	private final BooleanSetting rotate = add(new BooleanSetting("Rotate", true));
	private final SliderSetting time = add(new SliderSetting("Time", 100, 0, 2000, v -> rotate.getValue()));
	private final BooleanSetting switchReset = add(new BooleanSetting("SwitchReset", false));
	private final BooleanSetting crystal = add(new BooleanSetting("Crystal", false).setParent());
	private final BooleanSetting onlyHeadBomber = add(new BooleanSetting("OnlyHeadBomber", false, v -> crystal.isOpen()));
	private final BooleanSetting waitPlace = add(new BooleanSetting("WaitPlace", false, v -> crystal.isOpen()));
	private final BooleanSetting spamPlace = add(new BooleanSetting("SpamPlace", false, v -> crystal.isOpen()));
	private final BooleanSetting afterBreak = add(new BooleanSetting("AfterBreak", true, v -> crystal.isOpen()));
	private final BooleanSetting checkDamage = add(new BooleanSetting("DetectProgress", true, v -> crystal.isOpen()));
	private final SliderSetting crystalDamage = add(new SliderSetting("Progress", 0.7f, 0.0f, 1.0f, 0.01, v -> crystal.isOpen() && checkDamage.getValue()));
	public final BindSetting obsidian = add(new BindSetting("Obsidian", -1));
	private final BindSetting enderChest = add(new BindSetting("EnderChest", -1));
	private final SliderSetting placeDelay = add(new SliderSetting("PlaceDelay", 100, 0, 1000));
	private final EnumSetting<FadeUtils.Quad> quad = add(new EnumSetting<>("Quad", FadeUtils.Quad.In));

	private final BooleanSetting autoColor = add(new BooleanSetting("AutoColor", true));
	public final ColorSetting color = add(new ColorSetting("Color",new Color(255, 255, 255, 100)));
	public final ColorSetting endColor = add(new ColorSetting("EndColor",new Color(25, 255, 50, 100))).injectBoolean(false);
	public final ColorSetting endboxColor = add(new ColorSetting("EndBoxColor",new Color(25, 255, 50, 100), v-> endColor.booleanValue));

	public final ColorSetting doubleColor = add(new ColorSetting("DoubleColor", new Color(88, 94, 255, 100), v -> doubleBreak.getValue()));
	private final BooleanSetting bold = add(new BooleanSetting("Bold", false)).setParent();
	private final SliderSetting lineWidth = add(new SliderSetting("LineWidth", 4,1,5, v ->bold.isOpen()));

	private final BooleanSetting text = add(new BooleanSetting("Text", true));
	private final BooleanSetting box = add(new BooleanSetting("Box", true));
	private final BooleanSetting outline = add(new BooleanSetting("Outline", true));
	int lastSlot = -1;

	public static VSpeedMine INSTANCE;
	public static BlockPos breakPos;
	public static BlockPos secondPos;
	public static double progress = 0;
	public static double secondProgress = 0;

	private final Timer mineTimer = new Timer();
	private final FadeUtils animationTime = new FadeUtils(1000);
	private final FadeUtils secondAnim = new FadeUtils(1000);
	private boolean startMine = false;
	private int breakNumber = 0;
	public final Timer secondTimer = new Timer();
	private final Timer delayTimer = new Timer();
	private final Timer placeTimer = new Timer();
	public static boolean sendGroundPacket = false;
	public VSpeedMine() {
		super("VSpeedMine", Category.Combat);
		INSTANCE = this;
	}

	@Override
	public String getInfo() {
		if (instant.getValue()) {
			return "Instant";
		}
		return "Aborted";
	}

	private int findCrystal() {
		if (!hotBar.getValue()) {
			return InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL);
		} else {
			return InventoryUtil.findItem(Items.END_CRYSTAL);
		}
	}
	private int findBlock(Block block) {
		if (!hotBar.getValue()) {
			return InventoryUtil.findBlockInventorySlot(block);
		} else {
			return InventoryUtil.findBlock(block);
		}
	}

	private void doSwap(int slot, int inv) {
		if (hotBar.getValue()) {
			InventoryUtil.switchToSlot(slot);
		} else {
			InventoryUtil.inventorySwap(inv, mc.player.getInventory().selectedSlot);
		}
	}

	static DecimalFormat df = new DecimalFormat("0.0");
	@Override
	public void onRender3D(MatrixStack matrixStack, float partialTicks) {
		update();
		if (!mc.player.isCreative()) {
			if (secondPos != null) {
				int slot = getTool(secondPos);
				if (slot == -1) {
					slot = mc.player.getInventory().selectedSlot;
				}
				double breakTime = getBreakTime(secondPos, slot);
				secondProgress = (double) secondTimer.getPassedTimeMs() / breakTime;

				if (isAir(secondPos)) {
					secondPos = null;
					return;
				}
				double iProgress = secondProgress > 1 ? 1 : secondProgress;

				double ease = (1 - secondAnim.getQuad(quad.getValue())) * 0.5;
				if (!bold.getValue()) {
					Render3DUtil.draw3DBox(matrixStack, new Box(secondPos).shrink(ease, ease, ease).shrink(-ease, -ease, -ease)
							, ColorUtil.injectAlpha(doubleColor.getValue(), (int) (doubleColor.getValue().getAlpha() * iProgress))
							, outline.getValue()
							, box.getValue());
				} else {
					Render3DUtil.drawLine(new Box(secondPos).shrink(ease, ease, ease).shrink(-ease, -ease, -ease)
							, ColorUtil.injectAlpha(doubleColor.getValue(), (int) (doubleColor.getValue().getAlpha() * iProgress)), lineWidth.getValueInt());
					Render3DUtil.drawFill(matrixStack, new Box(secondPos).shrink(ease, ease, ease).shrink(-ease, -ease, -ease)
							, ColorUtil.injectAlpha(doubleColor.getValue(), (int) (doubleColor.getValue().getAlpha() * iProgress))
					);
				}
			} else {
				secondProgress = 0;
			}
			if (breakPos != null) {
				int slot = getTool(breakPos);
				if (slot == -1) {
					slot = mc.player.getInventory().selectedSlot;
				}
				double breakTime = getBreakTime(breakPos, slot);
				progress = (double) mineTimer.getPassedTimeMs() / breakTime;
				animationTime.setLength((long) getBreakTime(breakPos, slot));
				double ease = (1 - animationTime.getQuad(quad.getValue())) * 0.5;
				Color color = this.color.getValue();
				double iProgress = progress > 1 ? 1 : progress;
				if (!bold.getValue()) {
					Render3DUtil.draw3DBox(matrixStack,
							new Box(breakPos).shrink(ease, ease, ease).shrink(-ease, -ease, -ease),
							ColorUtil.injectAlpha(autoColor.getValue() ? new Color((int) (255 * iProgress),
											(int) (255 * iProgress), 0) :
											!endColor.booleanValue ? color : iProgress >= 1 ? endColor.getValue() : color,
									(int) (!endColor.booleanValue ? color.getAlpha() * iProgress : iProgress >= 1 ? endColor.getValue().getAlpha() * iProgress : color.getAlpha() * iProgress))
							, outline.getValue(),
							box.getValue());
				} else {
					Render3DUtil.drawLine(new Box(breakPos).shrink(ease, ease, ease).shrink(-ease, -ease, -ease),
							ColorUtil.injectAlpha(autoColor.getValue() ? new Color((int) (255 * iProgress),
											(int) (255 * iProgress), 0) :
											!endColor.booleanValue ? color : iProgress >= 1 ? endboxColor.getValue() : color
									, (int) (!endColor.booleanValue ? color.getAlpha() * iProgress : iProgress >= 1 ? endColor.getValue().getAlpha() * iProgress : color.getAlpha() * iProgress)),
							lineWidth.getValueInt());
					Render3DUtil.drawFill(matrixStack,
							new Box(breakPos).shrink(ease, ease, ease).shrink(-ease, -ease, -ease),
							ColorUtil.injectAlpha(autoColor.getValue() ? new Color((int) (255 * iProgress),
											(int) (255 * iProgress), 0) :
											!endColor.booleanValue ? color : iProgress >= 1 ? endColor.getValue() : color
									, (int) (!endColor.booleanValue ? color.getAlpha() * iProgress : iProgress >= 1 ? endColor.getValue().getAlpha() * iProgress : color.getAlpha() * iProgress)));


				}
				if (text.getValue()) {
					if (isAir(breakPos)) {
						Render3DUtil.drawText3D("Waiting", breakPos.toCenterPos(), -1);
					} else {
						if ((int) mineTimer.getPassedTimeMs() < breakTime) {
							Render3DUtil.drawText3D(df.format(progress * 100) + "%", breakPos.toCenterPos(), -1);
						} else {
							Render3DUtil.drawText3D("100.0%", breakPos.toCenterPos(), -1);
						}
					}
				}
			} else {
				progress = 0;
			}
		} else {
			progress = 0;
			secondProgress = 0;
		}
	}

	@Override
	public void onLogin() {
		startMine = false;
		breakPos = null;
		secondPos = null;
	}

	@Override
	public void onDisable() {
		startMine = false;
		breakPos = null;
	}

	@Override
	public void onUpdate() {
		update();
	}


	public void update() {
		if (nullCheck()) return;
		if (mc.player.isDead()) {
			secondPos = null;
		}
		if (secondPos != null && secondTimer.passed(getBreakTime(secondPos, mc.player.getInventory().selectedSlot, 1.3))) {
			secondPos = null;
		}
		if (secondPos != null && isAir(secondPos)) {
			secondPos = null;
		}
		if (mc.player.isCreative()) {
			startMine = false;
			breakNumber = 0;
			breakPos = null;
			return;
		}
		if (breakPos == null) {
			breakNumber = 0;
			startMine = false;
			return;
		}
		if (isAir(breakPos)) {
			breakNumber = 0;
		}
		if (breakNumber > maxBreak.getValue() - 1 && maxBreak.getValue() > 0 || !wait.getValue() && isAir(breakPos) && !instant.getValue()) {
			if (breakPos.equals(secondPos)) {
				secondPos = null;
			}
			startMine = false;
			breakNumber = 0;
			breakPos = null;
			return;
		}
		if (godBlocks.contains(mc.world.getBlockState(breakPos).getBlock())) {
			breakPos = null;
			startMine = false;
			return;
		}
		if (usingPause.getValue() && EntityUtil.isUsing()) {
			return;
		}
		if (MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(breakPos.toCenterPos())) > range.getValue()) {
			if (farCancel.getValue()) {
				startMine = false;
				breakNumber = 0;
				breakPos = null;
			}
			return;
		}
		if (breakPos.equals(AnchorAura.INSTANCE.currentPos)) return;
		if (!hotBar.getValue() && mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof InventoryScreen) && !(mc.currentScreen instanceof ClickGuiScreen)) {
			return;
		}

		int slot = getTool(breakPos);
		if (slot == -1) {
			slot = mc.player.getInventory().selectedSlot;
		}
		if (isAir(breakPos)) {
			if (shouldCrystal()) {
				for (Direction facing : Direction.values()) {
					CombatUtil.attackCrystal(breakPos.offset(facing), rotate.getValue(), true);
				}
			}
			if (placeTimer.passedMs(placeDelay.getValue())) {
				if (BlockUtil.canPlace(breakPos) && mc.currentScreen == null) {
					if (enderChest.isPressed()) {
						int eChest = findBlock(Blocks.ENDER_CHEST);
						if (eChest != -1) {
							int oldSlot = mc.player.getInventory().selectedSlot;
							doSwap(eChest, eChest);
							TPUtil.tp(() -> BlockUtil.placeBlock(breakPos, rotate.getValue(), true), breakPos.toCenterPos());
							//BlockUtil.placeBlock(breakPos, rotate.getValue(), true);
							doSwap(oldSlot, eChest);
							placeTimer.reset();
						}
					} else if (obsidian.isPressed()) {

						int obsidian = findBlock(Blocks.OBSIDIAN);
						if (obsidian != -1) {

							boolean hasCrystal = false;
							if (shouldCrystal()) {
								for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(breakPos.up()))) {
									if (entity instanceof EndCrystalEntity) {
										hasCrystal = true;
										break;
									}
								}
							}

							if (!hasCrystal || spamPlace.getValue()) {
								int oldSlot = mc.player.getInventory().selectedSlot;
								doSwap(obsidian, obsidian);
								TPUtil.tp(() -> BlockUtil.placeBlock(breakPos, rotate.getValue(), true), breakPos.toCenterPos());
								//BlockUtil.placeBlock(breakPos, rotate.getValue(), true);
								doSwap(oldSlot, obsidian);
								placeTimer.reset();
							}
						}
					}
				}
			}
			breakNumber = 0;
		} else if (canPlaceCrystal(breakPos.up(), true)) {
			if (waitPlace.getValue()) {
				for (Direction i : Direction.values()) {
					if (breakPos.offset(i).equals(AutoCrystal.crystalPos)) {
						if (AutoCrystal.INSTANCE.canPlaceCrystal(AutoCrystal.crystalPos, false, false)) {
							return;
						}
						break;
					}
				}
			}
			if (shouldCrystal()) {
				if (placeTimer.passedMs(placeDelay.getValue())) {
					if (checkDamage.getValue()) {
						if (mineTimer.getPassedTimeMs() / getBreakTime(breakPos, slot) >= crystalDamage.getValue()) {
							int crystal = findCrystal();
							if (crystal != -1) {
								int oldSlot = mc.player.getInventory().selectedSlot;
								doSwap(crystal, crystal);
								TPUtil.tp(() -> BlockUtil.placeCrystal(breakPos.up(), rotate.getValue()), breakPos.toCenterPos());
								//BlockUtil.placeCrystal(breakPos.up(), rotate.getValue());
								doSwap(oldSlot, crystal);
								placeTimer.reset();
								if (waitPlace.getValue()) return;
							}
						}
					} else {
						int crystal = findCrystal();
						if (crystal != -1) {
							int oldSlot = mc.player.getInventory().selectedSlot;
							doSwap(crystal, crystal);
							TPUtil.tp(() -> BlockUtil.placeCrystal(breakPos.up(), rotate.getValue()), breakPos.toCenterPos());
							//BlockUtil.placeCrystal(breakPos.up(), rotate.getValue());
							doSwap(oldSlot, crystal);
							placeTimer.reset();
							if (waitPlace.getValue()) return;
						}
					}
				} else if (startMine) {
					return;
				}
			}
		}
		if (!delayTimer.passedMs((long) delay.getValue())) return;
		if (startMine) {
			if (isAir(breakPos)) {
				return;
			}
			if (onlyGround.getValue() && !mc.player.isOnGround()) return;
			if (mineTimer.passedMs((long) getBreakTime(breakPos, slot))) {
				int old = mc.player.getInventory().selectedSlot;
				boolean shouldSwitch;
				if (hotBar.getValue()) {
					shouldSwitch = slot != old;
				} else {
					if (slot < 9) {
						slot = slot + 36;
					}
					shouldSwitch = old + 36 != slot;
				}
				if (shouldSwitch) {
					if (hotBar.getValue()) {
						InventoryUtil.switchToSlot(slot);
					} else {
						InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
					}
				}
				if (rotate.getValue()) {
					EntityUtil.facePosSide(breakPos, BlockUtil.getClickSide(breakPos));
				}
				if (endSwing.getValue()) EntityUtil.swingHand(Hand.MAIN_HAND, CombatSetting.INSTANCE.swingMode.getValue());
				TPUtil.tp(() -> mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, breakPos, BlockUtil.getClickSide(breakPos))), breakPos.toCenterPos());
				//mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, breakPos, BlockUtil.getClickSide(breakPos)));
				if (shouldSwitch && ghostHand.getValue()) {
					if (hotBar.getValue()) {
						InventoryUtil.switchToSlot(old);
					} else {
						InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
						EntityUtil.syncInventory();
					}
				}
				breakNumber++;
				delayTimer.reset();
				if (afterBreak.getValue() && shouldCrystal()) {
					for (Direction facing : Direction.values()) {
						CombatUtil.attackCrystal(breakPos.offset(facing), rotate.getValue(), true);
					}
				}
			}
		} else {
			if (!mineAir.getValue() && isAir(breakPos)) {
				return;
			}
			animationTime.setLength((long) getBreakTime(breakPos, slot));
			mineTimer.reset();
			if (swing.getValue()) {
				EntityUtil.swingHand(Hand.MAIN_HAND, CombatSetting.INSTANCE.swingMode.getValue());
			}
			TPUtil.tp(() -> mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, breakPos, BlockUtil.getClickSide(breakPos))), breakPos.toCenterPos());
			//mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, breakPos, BlockUtil.getClickSide(breakPos)));
			delayTimer.reset();
		}
	}

	@EventHandler
	public void onAttackBlock(ClickBlockEvent event) {
		if (nullCheck() || mc.player.isCreative()) {
			return;
		}
		event.cancel();
		if (godBlocks.contains(mc.world.getBlockState(event.getBlockPos()).getBlock())) {
			return;
		}
		if (event.getBlockPos().equals(breakPos)) {
			return;
		}
		breakPos = event.getBlockPos();
		mineTimer.reset();
		animationTime.reset();
		if (godBlocks.contains(mc.world.getBlockState(event.getBlockPos()).getBlock())) {
			return;
		}
		startMine();
	}
	public static boolean canPlaceCrystal(BlockPos pos, boolean ignoreItem) {
		BlockPos obsPos = pos.down();
		BlockPos boost = obsPos.up();
		return (BlockUtil.getBlock(obsPos) == Blocks.BEDROCK || BlockUtil.getBlock(obsPos) == Blocks.OBSIDIAN)
				&& BlockUtil.getClickSideStrict(obsPos) != null
				&& noEntity(boost, ignoreItem)
				&& noEntity(boost.up(), ignoreItem)
				&& (!CombatSetting.INSTANCE.lowVersion.getValue() || getBlock(boost.up()) == Blocks.AIR);
	}

	public static boolean noEntity(BlockPos pos, boolean ignoreItem) {
		for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
			if (entity instanceof ItemEntity && ignoreItem || entity instanceof ArmorStandEntity && CombatSetting.INSTANCE.obsMode.getValue()) continue;
			return false;
		}
		return true;
	}
	public void mine(BlockPos pos) {
		if (nullCheck() || mc.player.isCreative()) {
			return;
		}
		if (isOff()) {
			return;
		}
		if (godBlocks.contains(mc.world.getBlockState(pos).getBlock())) {
			return;
		}
		if (pos.equals(breakPos)) {
			return;
		}
		if (breakPos != null && preferWeb.getValue() && BlockUtil.getBlock(breakPos) == Blocks.COBWEB) {
			return;
		}
		breakPos = pos;
		mineTimer.reset();
		animationTime.reset();
		startMine();
	}
	private boolean shouldCrystal() {
		return crystal.getValue() && (!onlyHeadBomber.getValue() || obsidian.isPressed()); //|| HeadBomber.INSTANCE.isOn());
	}
	private void startMine() {
		if (rotate.getValue()) {
			Vec3i vec3i = BlockUtil.getClickSide(breakPos).getVector();
			EntityUtil.faceVector(breakPos.toCenterPos().add(new Vec3d(vec3i.getX() * 0.5, vec3i.getY() * 0.5, vec3i.getZ() * 0.5)));
		}
		TPUtil.tp(() -> mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, breakPos, BlockUtil.getClickSide(breakPos))), breakPos.toCenterPos());
		//mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, breakPos, BlockUtil.getClickSide(breakPos)));
		if (doubleBreak.getValue()) {
			if (secondPos == null || isAir(secondPos)) {
				int slot = getTool(breakPos);
				if (slot == -1) {
					slot = mc.player.getInventory().selectedSlot;
				}
				double breakTime = (getBreakTime(breakPos, slot, 1));
				secondAnim.reset();
				secondAnim.setLength((long) breakTime);
				secondTimer.reset();
				secondPos = breakPos;
			}
			TPUtil.tp(() -> mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, breakPos, BlockUtil.getClickSide(breakPos))), breakPos.toCenterPos());
			//mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, breakPos, BlockUtil.getClickSide(breakPos)));
			TPUtil.tp(() -> mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, breakPos, BlockUtil.getClickSide(breakPos))), breakPos.toCenterPos());
			//mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, breakPos, BlockUtil.getClickSide(breakPos)));
		}
		if (swing.getValue()) {
			EntityUtil.swingHand(Hand.MAIN_HAND, CombatSetting.INSTANCE.swingMode.getValue());
		}
		breakNumber = 0;
	}

	public int getTool(BlockPos pos) {
		if (hotBar.getValue()) {
			int index = -1;
			float CurrentFastest = 1.0f;
			for (int i = 0; i < 9; ++i) {
				final ItemStack stack = mc.player.getInventory().getStack(i);
				if (stack != ItemStack.EMPTY) {
					final float digSpeed = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
					final float destroySpeed = stack.getMiningSpeedMultiplier(mc.world.getBlockState(pos));
					if (digSpeed + destroySpeed > CurrentFastest) {
						CurrentFastest = digSpeed + destroySpeed;
						index = i;
					}
				}
			}
			return index;
		} else {
			AtomicInteger slot = new AtomicInteger();
			slot.set(-1);
			float CurrentFastest = 1.0f;
			for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
				if (!(entry.getValue().getItem() instanceof AirBlockItem)) {
					final float digSpeed = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, entry.getValue());
					final float destroySpeed = entry.getValue().getMiningSpeedMultiplier(mc.world.getBlockState(pos));
					if (digSpeed + destroySpeed > CurrentFastest) {
						CurrentFastest = digSpeed + destroySpeed;
						slot.set(entry.getKey());
					}
				}
			}
			return slot.get();
		}
	}

	@EventHandler(priority =  EventPriority.LOW)
	public void onRotate(RotateEvent event) {
		if (nullCheck() || mc.player.isCreative()) {
			return;
		}
		if (onlyGround.getValue() && !mc.player.isOnGround()) return;
		if (rotate.getValue() && breakPos != null && !isAir(breakPos) && time.getValue() > 0) {
			if (MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(breakPos.toCenterPos())) > range.getValue()) {
				return;
			}
			int slot = getTool(breakPos);
			if (slot == -1) {
				slot = mc.player.getInventory().selectedSlot;
			}
			double breakTime = (getBreakTime(breakPos, slot) - time.getValue());
			if (breakTime <= 0 || mineTimer.passedMs((long) breakTime)) {
				facePosFacing(breakPos, BlockUtil.getClickSide(breakPos), event);
			}
		}
	}

	public static void facePosFacing(BlockPos pos, Direction side, RotateEvent event) {
		final Vec3d hitVec = pos.toCenterPos().add(new Vec3d(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5));
		faceVector(hitVec, event);
	}

	private static void faceVector(Vec3d vec, RotateEvent event) {
		float[] rotations = EntityUtil.getLegitRotations(vec);
		event.setRotation(rotations[0], rotations[1]);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPacketSend(PacketEvent.Send event) {
		if (nullCheck() || mc.player.isCreative()) {
			return;
		}
		if (event.getPacket() instanceof PlayerMoveC2SPacket) {
			if (bypassGround.getValue() && breakPos != null && !isAir(breakPos) && bypassTime.getValue() > 0 && MathHelper.sqrt((float) breakPos.toCenterPos().squaredDistanceTo(EntityUtil.getEyesPos())) <= range.getValueFloat() + 2) {
				int slot = getTool(breakPos);
				if (slot == -1) {
					slot = mc.player.getInventory().selectedSlot;
				}
				double breakTime = (getBreakTime(breakPos, slot) - bypassTime.getValue());
				if (breakTime <= 0 || mineTimer.passedMs((long) breakTime)) {
					sendGroundPacket = true;
					((IPlayerMoveC2SPacket) event.getPacket()).setOnGround(true);
				}
			} else {
				sendGroundPacket = false;
			}
			return;
		}
		if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket packet) {
			if (packet.getSelectedSlot() != lastSlot) {
				lastSlot = packet.getSelectedSlot();
				if (switchReset.getValue()) {
					startMine = false;
					mineTimer.reset();
					animationTime.reset();
				}
			}
			return;
		}
		if (!(event.getPacket() instanceof PlayerActionC2SPacket)) {
			return;
		}
		if (((PlayerActionC2SPacket) event.getPacket()).getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
			if (breakPos == null || !((PlayerActionC2SPacket) event.getPacket()).getPos().equals(breakPos)) {
				if (cancelPacket.getValue()) event.cancel();
				return;
			}
			startMine = true;
		} else if (((PlayerActionC2SPacket) event.getPacket()).getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) {
			if (breakPos == null || !((PlayerActionC2SPacket) event.getPacket()).getPos().equals(breakPos)) {
				if (cancelPacket.getValue()) event.cancel();
				return;
			}
			if (!instant.getValue()) {
				startMine = false;
			}
		}
	}

	public final double getBreakTime(BlockPos pos, int slot) {
		return getBreakTime(pos, slot, damage.getValue());
	}

	public final double getBreakTime(BlockPos pos, int slot, double damage) {
		return (1 / getBlockStrength(pos, mc.player.getInventory().getStack(slot)) / 20 * 1000 * damage);
	}


	private boolean canBreak(BlockPos pos) {
		final BlockState blockState = mc.world.getBlockState(pos);
		final Block block = blockState.getBlock();
		return block.getHardness() != -1;
	}

	public float getBlockStrength(BlockPos position, ItemStack itemStack) {
		BlockState state = mc.world.getBlockState(position);
		float hardness = state.getHardness(mc.world, position);
		if (hardness < 0) {
			return 0;
		}
		if (!canBreak(position)) {
			return getDigSpeed(state, itemStack) / hardness / 100F;
		} else {
			return getDigSpeed(state, itemStack) / hardness / 30F;
		}
	}

	public float getDigSpeed(BlockState state, ItemStack itemStack) {
		float digSpeed = getDestroySpeed(state, itemStack);
		if (digSpeed > 1) {
			int efficiencyModifier = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack);
			if (efficiencyModifier > 0 && !itemStack.isEmpty()) {
				digSpeed += StrictMath.pow(efficiencyModifier, 2) + 1;
			}
		}
		if (mc.player.hasStatusEffect(StatusEffects.HASTE)) {
			digSpeed *= 1 + (mc.player.getStatusEffect(StatusEffects.HASTE).getAmplifier() + 1) * 0.2F;
		}
		if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
			float fatigueScale;
			switch (mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
				case 0 -> fatigueScale = 0.3F;
				case 1 -> fatigueScale = 0.09F;
				case 2 -> fatigueScale = 0.0027F;
				default -> fatigueScale = 8.1E-4F;
			}
			digSpeed *= fatigueScale;
		}
		if (mc.player.isSubmergedInWater() && !EnchantmentHelper.hasAquaAffinity(mc.player)) {
			digSpeed /= 5;
		}
		if (!mc.player.isOnGround() && INSTANCE.checkGround.getValue()) {
			digSpeed /= 5;
		}
		return (digSpeed < 0 ? 0 : digSpeed);
	}

	public float getDestroySpeed(BlockState state, ItemStack itemStack) {
		float destroySpeed = 1;
		if (itemStack != null && !itemStack.isEmpty()) {
			destroySpeed *= itemStack.getMiningSpeedMultiplier(state);
		}
		return destroySpeed;
	}

	private boolean isAir(BlockPos breakPos) {
		return mc.world.isAir(breakPos) || BlockUtil.getBlock(breakPos) == Blocks.FIRE && BlockUtil.hasCrystal(breakPos);
	}
}