package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class BaseFinder extends Module {

    private static File basedata = new File("./BaseData.txt");

    private static File chestdata = new File("./ChestData.txt");


    public final ColorSetting color =
            add(new ColorSetting("Color",new Color(255, 255, 255, 100)));

    private final SliderSetting delay =
            add(new SliderSetting("Delay", 15, 0, 30));
    private final SliderSetting count =
            add(new SliderSetting("Count", 50, 1 , 2000));

    private final BooleanSetting log =
            add(new BooleanSetting("SaveChestLog", true));

    private final Timer timer = new Timer();
    public  BaseFinder(){
        super("BaseFinder", Category.Misc);
    }


    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        ArrayList<BlockEntity> blockEntities2 = BlockUtil.getTileEntities();
        for(BlockEntity blockEntity : blockEntities2) {
            if(blockEntity instanceof ChestBlockEntity || blockEntity instanceof TrappedChestBlockEntity) {
                Box box = new Box(blockEntity.getPos());
                Render3DUtil.draw3DBox(matrixStack, box, color.getValue());
            }
        }
        if(!timer.passed(delay.getValueInt() * 20L)){
            return;
        }
        int chest = 0;
        ArrayList<BlockEntity> blockEntities = BlockUtil.getTileEntities();
        for(BlockEntity blockEntity : blockEntities) {
            if(blockEntity instanceof ChestBlockEntity || blockEntity instanceof TrappedChestBlockEntity) {
                chest ++;
                if(log.getValue()){
                    writePacketData(chestdata, "FindChest:" + blockEntity.getPos());
                }
            }
        }
        if(chest >= count.getValue()){
            timer.reset();
            writePacketData(basedata, "Find:" + mc.player.getPos() + " Count:" + chest);
            CommandManager.sendChatMessage("Find:" + mc.player.getPos() + " Count:" + chest);
            chest = 0;
        }

    }

    private static void writePacketData(File file, String data) {
        new  Thread(() -> {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                writer.write(data);
                writer.newLine();
                //writer.flush();
                writer.close();
            } catch (IOException ignored) {
            }
        }).start();
    }
}
