package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.eventbus.EventPriority;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class AntiWeakness extends Module {
    public AntiWeakness() {
        super("AntiWeakness", "anti weak", Category.Combat);
    }
    private final SliderSetting delay = add(new SliderSetting("Delay", 100, 0, 500).setSuffix("ms"));
    private final EnumSetting<SwapMode> swapMode =
            add(new EnumSetting<>("SwapMode", SwapMode.Inventory));
    private final BooleanSetting onlyCrystal =
            add(new BooleanSetting("OnlyCrystal", true));
    public enum SwapMode {
        Normal, Silent, Inventory
    }

    @Override
    public String getInfo() {
        return swapMode.getValue().name();
    }

    private final Timer delayTimer = new Timer();
    private PlayerInteractEntityC2SPacket lastPacket = null;
    boolean ignore = false;
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPacketSend(PacketEvent.Send event) {
        if (nullCheck()) return;
        if (event.isCancelled()) return;
        if (ignore) return;
        if (mc.player.getStatusEffect(StatusEffects.WEAKNESS) == null) return;
        if (mc.player.getMainHandStack().getItem() instanceof SwordItem)
            return;
        if (!delayTimer.passedMs(delay.getValue())) return;
        if (event.getPacket() instanceof PlayerInteractEntityC2SPacket packet && Criticals.getInteractType(packet) == Criticals.InteractType.ATTACK) {

            if (onlyCrystal.getValue() && !(Criticals.getEntity(packet) instanceof EndCrystalEntity))
                return;
            lastPacket = event.getPacket();
            delayTimer.reset();
            ignore = true;
            doAnti();
            ignore = false;
            event.cancel();
        }
    }
    private void doAnti() {
        if (lastPacket == null) return;
        int strong;
        if (swapMode.getValue() != SwapMode.Inventory) {
            strong = InventoryUtil.findClass(SwordItem.class);
        } else {
            strong = InventoryUtil.findClassInventorySlot(SwordItem.class);
        }
        if (strong == -1) return;
        int old = mc.player.getInventory().selectedSlot;
        if (swapMode.getValue() != SwapMode.Inventory) {
            InventoryUtil.switchToSlot(strong);
        } else {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, strong, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
        }
        mc.player.networkHandler.sendPacket(lastPacket);
        if (swapMode.getValue() != SwapMode.Inventory) {
            if (swapMode.getValue() != SwapMode.Normal) InventoryUtil.switchToSlot(old);
        } else {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, strong, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            EntityUtil.syncInventory();
        }
    }
}
