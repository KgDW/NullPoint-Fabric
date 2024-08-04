package me.nullpoint.asm.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.mod.modules.impl.miscellaneous.ShulkerViewer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static me.nullpoint.api.utils.Wrapper.mc;
@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {

    protected MixinHandledScreen(Text title) {
        super(title);
    }

    @Shadow
    @Nullable
    protected Slot focusedSlot;
    @Shadow
    protected int x;
    @Shadow
    protected int y;

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (focusedSlot != null && !focusedSlot.getStack().isEmpty() && client.player.playerScreenHandler.getCursorStack().isEmpty()) {
            if (hasItems(focusedSlot.getStack()) && ShulkerViewer.INSTANCE.isOn()) {
                renderShulkerToolTip(context, mouseX, mouseY, focusedSlot.getStack());
            }
        }
    }

    public void renderShulkerToolTip(DrawContext context, int mouseX, int mouseY, ItemStack stack) {
        try {
            NbtCompound compoundTag = stack.getSubNbt("BlockEntityTag");
            DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(27, ItemStack.EMPTY);
            Inventories.readNbt(compoundTag, itemStacks);
            draw(context, itemStacks, mouseX, mouseY);
        } catch (Exception ignore) {
        }
    }

    private void draw(DrawContext context, DefaultedList<ItemStack> itemStacks, int mouseX, int mouseY) {
        RenderSystem.disableDepthTest();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        mouseX += 8;
        mouseY -= 82;

        drawBackground(context, mouseX, mouseY);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        DiffuseLighting.enableGuiDepthLighting();
        int row = 0;
        int i = 0;
        for (ItemStack itemStack : itemStacks) {
            context.drawItem(itemStack, mouseX + 8 + i * 18, mouseY + 7 + row * 18);
            context.drawItemInSlot(mc.textRenderer, itemStack, mouseX + 8 + i * 18, mouseY + 7 + row * 18);
            i++;
            if (i >= 9) {
                i = 0;
                row++;
            }
        }
        DiffuseLighting.disableGuiDepthLighting();
        RenderSystem.enableDepthTest();
    }

    private void drawBackground(DrawContext context, int x, int y) {
       Render2DUtil.drawRect(context.getMatrices(), x, y, 176, 67, new Color(0, 0,0, 120));
    }
    private boolean hasItems(ItemStack itemStack) {
        NbtCompound compoundTag = itemStack.getSubNbt("BlockEntityTag");
        return compoundTag != null && compoundTag.contains("Items", 9);
    }
}
