package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.Nullpoint;
import com.mojang.blaze3d.systems.RenderSystem;
import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.api.utils.render.TextUtil;
import me.nullpoint.mod.gui.font.FontRenderers;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.entity.*;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4d;

import java.awt.*;
import java.util.ArrayList;


public class TwoDESP extends Module {
    public TwoDESP() {
        super("2DESP", Category.Render);
    }

    private final EnumSetting page =
            add(new EnumSetting("Settings", Page.Target));

    private final BooleanSetting outline = add(new BooleanSetting("Outline", true, v -> page.getValue() == Page.Setting));
    private final BooleanSetting renderHealth = add(new BooleanSetting("renderHealth", true, v -> page.getValue() == Page.Setting));
    private final BooleanSetting renderArmor = add(new BooleanSetting("Armor Dura", true, v -> page.getValue() == Page.Setting));
    private final SliderSetting durascale = add(new SliderSetting("DuraScale", 1, 0, 2, 0.1, v -> renderArmor.getValue()));


    private final BooleanSetting drawItem = add(new BooleanSetting("draw Item Name", true, v -> page.getValue() == Page.Setting));
    private final BooleanSetting drawItemC = add(new BooleanSetting("draw Item Count", true, v -> page.getValue() == Page.Setting && drawItem.getValue()));
    private final BooleanSetting font = add(new BooleanSetting("CustomFont", true, v -> page.getValue() == Page.Setting));


    private final BooleanSetting players = add(new BooleanSetting("Players", true, v -> page.getValue() == Page.Target));
    private final BooleanSetting friends = add(new BooleanSetting("Friends", true, v -> page.getValue() == Page.Target));
    private final BooleanSetting crystals = add(new BooleanSetting("Crystals", true, v -> page.getValue() == Page.Target));
    private final BooleanSetting creatures = add(new BooleanSetting("Creatures", false, v -> page.getValue() == Page.Target));
    private final BooleanSetting monsters = add(new BooleanSetting("Monsters", false, v -> page.getValue() == Page.Target));
    private final BooleanSetting ambients = add(new BooleanSetting("Ambients", false, v -> page.getValue() == Page.Target));
    private final BooleanSetting others = add(new BooleanSetting("Others", false, v -> page.getValue() == Page.Target));


    private final ColorSetting playersC = add(new ColorSetting("PlayersBox", new Color(0xFF9200), v -> page.getValue() == Page.Color));
    private final ColorSetting friendsC = add(new ColorSetting("FriendsBox", new Color(0x30FF00), v -> page.getValue() == Page.Color));
    private final ColorSetting crystalsC = add(new ColorSetting("CrystalsBox", new Color(0x00BBFF), v -> page.getValue() == Page.Color));
    private final ColorSetting creaturesC = add(new ColorSetting("CreaturesBox", new Color(0xA0A4A6), v -> page.getValue() == Page.Color));
    private final ColorSetting monstersC = add(new ColorSetting("MonstersBox", new Color(0xFF0000), v -> page.getValue() == Page.Color));
    private final ColorSetting ambientsC = add(new ColorSetting("AmbientsBox", new Color(0x7B00FF), v -> page.getValue() == Page.Color));
    private final ColorSetting othersC = add(new ColorSetting("OthersBox", new Color(0xFF0062), v -> page.getValue() == Page.Color));
    public final ColorSetting armorDuraColor = add(new ColorSetting("Armor Dura Color", new Color(0x2fff00), v -> page.getValue() == Page.Color));
    public final ColorSetting textcolor = add(new ColorSetting("Item Name Color", new Color(255, 255, 255, 255), v -> page.getValue() == Page.Color && drawItem.getValue()));
    public final ColorSetting countColor = add(new ColorSetting("Item Count Color", new Color(255, 255, 0, 255), v -> page.getValue() == Page.Color && drawItemC.getValue()));
    public final ColorSetting hHealth = add(new ColorSetting("High Health Color", new Color(0,255,0,255), v -> page.getValue() == Page.Color));
    public final ColorSetting mHealth = add(new ColorSetting("Mid Health Color", new Color(255,255,0,255), v -> page.getValue() == Page.Color));
    public final ColorSetting lHealth = add(new ColorSetting("Low Health Color", new Color(255,0,0,255), v -> page.getValue() == Page.Color));

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        Render2DUtil.setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (Entity ent : mc.world.getEntities()) {
            if (shouldRender(ent))
                drawBox(bufferBuilder, ent, matrix,context);
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        Render2DUtil.endRender();

        for (Entity ent : mc.world.getEntities()) {
            if (shouldRender(ent))
                drawText(ent, context);
        }
    }

    public boolean shouldRender(Entity entity) {
        if (entity == null)
            return false;

        if (mc.player == null)
            return false;

        if (entity instanceof PlayerEntity) {
            if (entity == mc.player && mc.options.getPerspective().isFirstPerson()) {
                return false;
            }
            if (Nullpoint.FRIEND.isFriend((PlayerEntity) entity))
                return friends.getValue();
            return players.getValue();
        }

        if (entity instanceof EndCrystalEntity)
            return crystals.getValue();

        return switch (entity.getType().getSpawnGroup()) {
            case CREATURE, WATER_CREATURE -> creatures.getValue();
            case MONSTER -> monsters.getValue();
            case AMBIENT, WATER_AMBIENT -> ambients.getValue();
            default -> others.getValue();
        };
    }

    public Color getEntityColor(Entity entity) {
        if (entity == null)
            return new Color(-1);

        if (entity instanceof PlayerEntity) {
            if (Nullpoint.FRIEND.isFriend((PlayerEntity) entity))
                return friendsC.getValue();
            return playersC.getValue();
        }

        if (entity instanceof EndCrystalEntity)
            return crystalsC.getValue();

        return switch (entity.getType().getSpawnGroup()) {
            case CREATURE, WATER_CREATURE -> creaturesC.getValue();
            case MONSTER -> monstersC.getValue();
            case AMBIENT, WATER_AMBIENT -> ambientsC.getValue();
            default -> othersC.getValue();
        };
    }

    public void drawBox(BufferBuilder bufferBuilder, @NotNull Entity ent, Matrix4f matrix, DrawContext context) {
        double x = ent.prevX + (ent.getX() - ent.prevX) * mc.getTickDelta();
        double y = ent.prevY + (ent.getY() - ent.prevY) * mc.getTickDelta();
        double z = ent.prevZ + (ent.getZ() - ent.prevZ) * mc.getTickDelta();
        Box axisAlignedBB2 = ent.getBoundingBox();
        Box axisAlignedBB = new Box(axisAlignedBB2.minX - ent.getX() + x - 0.05, axisAlignedBB2.minY - ent.getY() + y, axisAlignedBB2.minZ - ent.getZ() + z - 0.05, axisAlignedBB2.maxX - ent.getX() + x + 0.05, axisAlignedBB2.maxY - ent.getY() + y + 0.15, axisAlignedBB2.maxZ - ent.getZ() + z + 0.05);
        Vec3d[] vectors = new Vec3d[]{new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ)};

        Color col = getEntityColor(ent);

        Vector4d position = null;
        for (Vec3d vector : vectors) {
            vector = TextUtil.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
            if (vector.z > 0 && vector.z < 1) {
                if (position == null) position = new Vector4d(vector.x, vector.y, vector.z, 0);
                position.x = Math.min(vector.x, position.x);
                position.y = Math.min(vector.y, position.y);
                position.z = Math.max(vector.x, position.z);
                position.w = Math.max(vector.y, position.w);
            }
        }


        if (position != null) {
            double posX = position.x;
            double posY = position.y;
            double endPosX = position.z;
            double endPosY = position.w;

            if (outline.getValue()) {
                Render2DUtil.setRectPoints(bufferBuilder,matrix, (float) (posX - 1F), (float) posY, (float) (posX + 0.5), (float) (endPosY + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
                Render2DUtil.setRectPoints(bufferBuilder,matrix, (float) (posX - 1F), (float) (posY - 0.5), (float) (endPosX + 0.5), (float) (posY + 0.5 + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
                Render2DUtil.setRectPoints(bufferBuilder,matrix, (float) (endPosX - 0.5 - 0.5), (float) posY, (float) (endPosX + 0.5), (float) (endPosY + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
                Render2DUtil.setRectPoints(bufferBuilder,matrix, (float) (posX - 1), (float) (endPosY - 0.5 - 0.5), (float) (endPosX + 0.5), (float) (endPosY + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
                Render2DUtil.setRectPoints(bufferBuilder,matrix, (float) (posX - 0.5f), (float) posY, (float) (posX + 0.5 - 0.5), (float) endPosY, col, col, col, col);
                Render2DUtil.setRectPoints(bufferBuilder,matrix, (float) posX, (float) (endPosY - 0.5f), (float) endPosX, (float) endPosY, col, col, col, col);
                Render2DUtil.setRectPoints(bufferBuilder,matrix, (float) (posX - 0.5), (float) posY, (float) endPosX, (float) (posY + 0.5), col, col, col, col);
                Render2DUtil.setRectPoints(bufferBuilder,matrix, (float) (endPosX - 0.5), (float) posY, (float) endPosX, (float) endPosY, col, col, col, col);
            }


            if (ent instanceof LivingEntity lent && lent.getHealth() != 0 && renderHealth.getValue()) {
                Render2DUtil.setRectPoints(bufferBuilder,matrix, (float) (posX - 4), (float) posY, (float) posX - 3, (float) endPosY, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
                Color color=getcolor(lent.getHealth());
                Render2DUtil.setRectPoints(bufferBuilder,matrix, (float) (posX - 4), (float) (endPosY + (posY - endPosY) * lent.getHealth() / lent.getMaxHealth()), (float) posX - 3, (float) endPosY,color,color,color,color);
            }
            if (ent instanceof PlayerEntity player && renderArmor.getValue()) {
                double height = (endPosY - posY) / 4;
                ArrayList<ItemStack> stacks = new ArrayList<>();
                stacks.add(player.getInventory().armor.get(3));
                stacks.add(player.getInventory().armor.get(2));
                stacks.add(player.getInventory().armor.get(1));
                stacks.add(player.getInventory().armor.get(0));

                int i = -1;
                for (ItemStack armor : stacks) {
                    ++i;
                    if (!armor.isEmpty()) {

                        float durability = armor.getMaxDamage() - armor.getDamage();
                        int percent = (int) ((durability / (float) armor.getMaxDamage()) * 100F);
                        double finalH = height * (percent / 100);
                        Render2DUtil.setRectPoints(bufferBuilder,matrix, (float) (endPosX + 1.5), (float) ((float) posY + height * i + 1.2 * (i + 1)), (float) ((float) endPosX + 3),  (int)(posY + height * i + 1.2 * (i + 1) +finalH), armorDuraColor.getValue(), armorDuraColor.getValue(), armorDuraColor.getValue(), armorDuraColor.getValue());
                    }
                }

            }


        }

    }

    public void drawText(Entity ent, DrawContext context) {
        double x = ent.prevX + (ent.getX() - ent.prevX) * mc.getTickDelta();
        double y = ent.prevY + (ent.getY() - ent.prevY) * mc.getTickDelta();
        double z = ent.prevZ + (ent.getZ() - ent.prevZ) * mc.getTickDelta();
        Box axisAlignedBB2 = ent.getBoundingBox();
        Box axisAlignedBB = new Box(axisAlignedBB2.minX - ent.getX() + x - 0.05, axisAlignedBB2.minY - ent.getY() + y, axisAlignedBB2.minZ - ent.getZ() + z - 0.05, axisAlignedBB2.maxX - ent.getX() + x + 0.05, axisAlignedBB2.maxY - ent.getY() + y + 0.15, axisAlignedBB2.maxZ - ent.getZ() + z + 0.05);
        Vec3d[] vectors = new Vec3d[]{new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ)};

        Color col = getEntityColor(ent);

        Vector4d position = null;
        for (Vec3d vector : vectors) {
            vector = TextUtil.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
            if (vector.z > 0 && vector.z < 1) {
                if (position == null) position = new Vector4d(vector.x, vector.y, vector.z, 0);
                position.x = Math.min(vector.x, position.x);
                position.y = Math.min(vector.y, position.y);
                position.z = Math.max(vector.x, position.z);
                position.w = Math.max(vector.y, position.w);
            }
        }


        if (position != null) {
            double posX = position.x;
            double posY = position.y;
            double endPosX = position.z;
            double endPosY = position.w;
            if (ent instanceof ItemEntity entity && drawItem.getValue()) {
                float diff = (float) ((endPosX - posX) / 2f);
                float textWidth = (FontRenderers.Arial.getWidth(entity.getDisplayName().getString()) * 1);
                float tagX = (float) ((posX + diff - textWidth / 2f) * 1);
                int count = entity.getStack().getCount();

                /*if (font.getValue()) {
                    FontRenderers.biggerDef.drawString(context.getMatrices(), entity.getDisplayName().getString(), tagX, (float) posY - 10, textcolor.getValue().getRGB());
                    if (drawItemC.getValue()) {
                        FontRenderers.biggerDef.drawString(context.getMatrices(), "x" + count, tagX + FontRenderers.biggerDef.getStringWidth(entity.getDisplayName().getString() + " "), (float) posY - 10, textcolor.getValue().getRGB());

                    }
                } else {
                    context.drawText(mc.textRenderer, entity.getDisplayName().getString(), (int) tagX, (int) (posY - 10), textcolor.getValue().getRGB(), false);
                    if (drawItemC.getValue()) {
                        context.drawText(mc.textRenderer, "x" + count, (int) (tagX + mc.textRenderer.getWidth(entity.getDisplayName().getString() + " ")), (int) posY - 10, countColor.getValue().getRGB(), false);

                    }
                }*/
                context.drawText(mc.textRenderer, entity.getDisplayName().getString(), (int) tagX, (int) (posY - 10), textcolor.getValue().getRGB(), false);
                if (drawItemC.getValue()) {
                    context.drawText(mc.textRenderer, "x" + count, (int) (tagX + mc.textRenderer.getWidth(entity.getDisplayName().getString() + " ")), (int) posY - 10, countColor.getValue().getRGB(), false);

                }

            }
            if (ent instanceof PlayerEntity player && renderArmor.getValue()) {
                double height = (endPosY - posY) / 4;
                ArrayList<ItemStack> stacks = new ArrayList<>();
                stacks.add(player.getInventory().armor.get(3));
                stacks.add(player.getInventory().armor.get(2));
                stacks.add(player.getInventory().armor.get(1));
                stacks.add(player.getInventory().armor.get(0));

                int i = -1;
                for (ItemStack armor : stacks) {
                    ++i;
                    if (!armor.isEmpty()) {

                        float durability = armor.getMaxDamage() - armor.getDamage();
                        int percent = (int) ((durability / (float) armor.getMaxDamage()) * 100F);
                        double finalH = height * (percent / 100);
                        context.drawItem(armor, (int) (endPosX + 4), (int) (posY + height * i + 1.2 * (i + 1) +finalH/2) );

                    }
                }

            }
        }

    }



    public static float getRotations(Vec2f vec) {
        if (mc.player == null) return 0;
        double x = vec.x - mc.player.getPos().x;
        double z = vec.y - mc.player.getPos().z;
        return (float) -(Math.atan2(x, z) * (180 / Math.PI));
    }
    public Color getcolor(float health){
        if(health>=20){
            return hHealth.getValue();
        }
        else if(20>health && health>10){
            return mHealth.getValue();
        }
        else{
            return lHealth.getValue();
        }
    }

    public enum Page{
        Setting,Target,Color
    }
}