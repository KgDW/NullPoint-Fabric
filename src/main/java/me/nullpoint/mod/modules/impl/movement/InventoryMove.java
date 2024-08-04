package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;

public class InventoryMove extends Module {
    private final BooleanSetting sneak = add(new BooleanSetting("Sneak", false));

    public InventoryMove() {
        super("InventoryMove", Category.Movement);
        this.setDescription("Walk in inventory.");
    }

    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        update();
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
        if (!(mc.currentScreen instanceof ChatScreen)) {
            for (KeyBinding k : new KeyBinding[]{mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey}) {
                k.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(k.getBoundKeyTranslationKey()).getCode()));
            }

            mc.options.forwardKey.setPressed(AutoWalk.INSTANCE.isOn() || InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.forwardKey.getBoundKeyTranslationKey()).getCode()));
            mc.options.sprintKey.setPressed(Sprint.INSTANCE.isOn() || InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.sprintKey.getBoundKeyTranslationKey()).getCode()));

            if (sneak.getValue()) {
                mc.options.sneakKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.sneakKey.getBoundKeyTranslationKey()).getCode()));
            }
        }
    }
}
