package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.render.PlaceRender;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AntiPiston
        extends Module {
    public static AntiPiston INSTANCE;
    public final BooleanSetting rotate = add(new BooleanSetting("Rotate", true));
    public final BooleanSetting packet = add(new BooleanSetting("Packet", true));
    public final BooleanSetting helper = add(new BooleanSetting("Helper", true));
    public final BooleanSetting trap = add(new BooleanSetting("Trap", true).setParent());
    private final BooleanSetting usingPause =
            add(new BooleanSetting("UsingPause", true));
    private final BooleanSetting onlyBurrow = add(new BooleanSetting("OnlyBurrow", true, v -> this.trap.isOpen()).setParent());
    private final BooleanSetting whenDouble = add(new BooleanSetting("WhenDouble", true, v -> this.onlyBurrow.isOpen()));
    private final BooleanSetting inventory = add(new BooleanSetting("InventorySwap", true));

    public AntiPiston() {
        super("AntiPiston", "Trap self when piston kick", Category.Combat);
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (!mc.player.isOnGround()) {
            return;
        }
        if (usingPause.getValue() && mc.player.isUsingItem()) {
            return;
        }
        this.block();
    }


    private void block() {
        BlockPos pos = EntityUtil.getPlayerPos();
        if (this.getBlock(pos.up(2)) == Blocks.OBSIDIAN || this.getBlock(pos.up(2)) == Blocks.BEDROCK) {
            return;
        }
        int progress = 0;
        if (this.whenDouble.getValue()) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP || !(this.getBlock(pos.offset(i).up()) instanceof PistonBlock) || (AntiPiston.mc.world.getBlockState(pos.offset(i).up())).get(FacingBlock.FACING).getOpposite() != i) continue;
                ++progress;
            }
        }
        for (Direction i : Direction.values()) {
            if (i == Direction.DOWN || i == Direction.UP || !(this.getBlock(pos.offset(i).up()) instanceof PistonBlock) || (AntiPiston.mc.world.getBlockState(pos.offset(i).up())).get(FacingBlock.FACING).getOpposite() != i) continue;
            this.placeBlock(pos.up().offset(i, -1));
            if (this.trap.getValue() && (this.getBlock(pos) != Blocks.AIR || !this.onlyBurrow.getValue() || progress >= 2)) {
                this.placeBlock(pos.up(2));
                if (!BlockUtil.canPlace(pos.up(2))) {
                    for (Direction i2 : Direction.values()) {
                        if (!AntiPiston.canPlace(pos.offset(i2).up(2))) continue;
                        this.placeBlock(pos.offset(i2).up(2));
                        break;
                    }
                }
            }
            if (BlockUtil.canPlace(pos.up().offset(i, -1)) || !this.helper.getValue()) continue;
            if (BlockUtil.canPlace(pos.offset(i, -1))) {
                this.placeBlock(pos.offset(i, -1));
                continue;
            }
            this.placeBlock(pos.offset(i, -1).down());
        }
    }

    private Block getBlock(BlockPos block) {
        return AntiPiston.mc.world.getBlockState(block).getBlock();
    }

    private void placeBlock(BlockPos pos) {
        if (!canPlace(pos)) {
            return;
        }
        int old = mc.player.getInventory().selectedSlot;
        int block = findBlock(Blocks.OBSIDIAN);
        if (block == -1) return;
        doSwap(block);
        BlockUtil.placeBlock(pos, this.rotate.getValue(), this.packet.getValue());
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.syncInventory();
        } else {
            doSwap(old);
        }
    }

    public static boolean canPlace(BlockPos pos) {
        if (!BlockUtil.canBlockFacing(pos)) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        return !BlockUtil.hasEntity(pos, false);
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

