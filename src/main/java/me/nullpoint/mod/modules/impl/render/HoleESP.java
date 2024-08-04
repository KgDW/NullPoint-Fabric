package me.nullpoint.mod.modules.impl.render;

import com.mojang.authlib.GameProfile;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.eventbus.EventPriority;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.managers.BreakManager;
import me.nullpoint.api.managers.RotateManager;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static me.nullpoint.api.utils.world.BlockUtil.*;

public class HoleESP extends Module {
    public static HoleESP INSTANCE;
    private final SliderSetting range =
            add(new SliderSetting("Range", 6.0, 0.0, 20.0));
    private final SliderSetting yRange = add(new SliderSetting("yRange", 5.0, 0.0, 20.0));
    private final ColorSetting obiHole = add(new ColorSetting("obsidianHole", new Color(255, 255, 255, 200)));
    private final ColorSetting brHole = add(new ColorSetting("bedrockHole", new Color(0, 255, 0, 200)));
    private final ColorSetting otherHole = add(new ColorSetting("OtherHole", new Color(255, 255, 0, 200)));
    public final BooleanSetting doubleHole = add(new BooleanSetting("DoubleHole", true));
    /*private static boolean update = true;
    public static Thread thread;
    public final HashMap<BlockPos, HoleType> holeMap = new HashMap<>();*/


    public HoleESP(){
        super("HoleESP", "Hole ESP", Category.Render);
        INSTANCE = this;
    }

    /*@Override
    public void onUpdate() {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(() -> {
                while (INSTANCE.isOn()) {
                    update = true;
                    updateHolePos();
                }
                holeMap.clear();
            });
            try {
                thread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/

    @Override
    public String getInfo() {
        return "";
    }

    /*private void updateHolePos(){
        if (nullCheck()) {
            return;
        }
        if(!update) return;
        update = false;
        BlockPos playerPos = mc.player.getBlockPos();
        for (int x = playerPos.getX() - range.getValueInt(); x < playerPos.getX() + range.getValueInt(); ++x) {
            for (int z = playerPos.getZ() - range.getValueInt(); z < playerPos.getZ() + range.getValueInt(); ++z){
                for (int y = playerPos.getY() + yRange.getValueInt(); y > playerPos.getY() - yRange.getValueInt(); --y){
                    BlockPos pos = new BlockPos(x, y, z);
                    if(isHole(pos, true, true, Blocks.OBSIDIAN, false)){
                        holeMap.put(pos, HoleType.Obsidian);
                    }
                    else if(isHole(pos, true, true, Blocks.BEDROCK, false)){
                        holeMap.put(pos, HoleType.Bedrock);
                    }
                    else if(isHole(pos, true, true, Blocks.BEDROCK, true)){
                        holeMap.put(pos, HoleType.Other);
                    }
                }
            }
        }
    }*/

    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        if (nullCheck()) {
            return;
        }
        /*for(BlockPos a : new HashMap<>(holeMap).keySet()){
            if(holeMap.get(a) == HoleType.Obsidian){
                Render3DUtil.drawHole(matrixStack, a, obiHole.getValue(), true, true, 0.8f);
            }
            if(holeMap.get(a) == HoleType.Bedrock){
                Render3DUtil.drawHole(matrixStack, a, brHole.getValue(), true, true, 0.8f);
            }
            if(holeMap.get(a) == HoleType.Other){
                Render3DUtil.drawHole(matrixStack, a, otherHole.getValue(), true, true, 0.8f);
            }

        }
        holeMap.clear();
        update = true;*/
        BlockPos playerPos = mc.player.getBlockPos();
        for (int x = playerPos.getX() - range.getValueInt(); x < playerPos.getX() + range.getValueInt(); ++x) {
            for (int z = playerPos.getZ() - range.getValueInt(); z < playerPos.getZ() + range.getValueInt(); ++z){
                for (int y = playerPos.getY() + yRange.getValueInt(); y > playerPos.getY() - yRange.getValueInt(); --y){
                    BlockPos pos = new BlockPos(x, y, z);
                    if(isHole(pos, true, true, Blocks.OBSIDIAN, false)){
                        Render3DUtil.drawHole(matrixStack, pos, obiHole.getValue(), true, true, 0.8f, 2);
                    }
                    else if(isHole(pos, true, true, Blocks.BEDROCK, false)){
                        Render3DUtil.drawHole(matrixStack, pos, brHole.getValue(), true, true, 0.8f, 2);
                    }
                    else if(isHole(pos, true, true, Blocks.BEDROCK, true)){
                        Render3DUtil.drawHole(matrixStack, pos, otherHole.getValue(), true, true, 0.8f, 2);
                    }
                }
            }
        }
    }



    public static boolean isHole(BlockPos pos, boolean canStand, boolean checkTrap, Block block, boolean hard) {
        int blockProgress = 0;
        for (Direction i : Direction.values()) {
            if (i == Direction.UP || i == Direction.DOWN) continue;
            if (CombatUtil.isHard(pos.offset(i)) && hard ||BlockUtil.getBlock(pos.offset(i)) == block)
                blockProgress++;
        }
        return
                (
                        !checkTrap || (getBlock(pos) == Blocks.AIR
                                && getBlock(pos.add(0, 1, 0)) == Blocks.AIR
                                && getBlock(pos.add(0, 2, 0)) == Blocks.AIR)
                )
                        && blockProgress > 3
                        && (!canStand || getState(pos.add(0, -1, 0)).blocksMovement());
    }

    public enum HoleType{
        Obsidian,
        Bedrock,
        Other
    }

}
