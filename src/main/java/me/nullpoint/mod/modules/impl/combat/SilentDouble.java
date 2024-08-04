package me.nullpoint.mod.modules.impl.combat;



import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class SilentDouble extends Module {

    public static int slotMain;
    public static int swithc2;

    public static SilentDouble INSTANCE;
    public SilentDouble(){
        super("SilentDouble", Category.Combat);
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        update();
    }

    public void update() {
        if (!SpeedMine.INSTANCE.isOn()) {
            CommandManager.sendChatMessage("\u00a7e[?] \u00a7c\u00a7oAutoMine?");
            this.disable();
            return;
        }
        if (SpeedMine.secondPos != null && !SpeedMine.INSTANCE.secondTimer.passed(SpeedMine.INSTANCE.getBreakTime(SpeedMine.secondPos, SpeedMine.INSTANCE.getTool(SpeedMine.secondPos) == -1 ? mc.player.getInventory().selectedSlot : SpeedMine.INSTANCE.getTool(SpeedMine.secondPos), 0.89))) {
            slotMain = mc.player.getInventory().selectedSlot;
        }
        if (SpeedMine.secondPos != null) {
            if (SpeedMine.INSTANCE.secondTimer.passed(SpeedMine.INSTANCE.getBreakTime(SpeedMine.secondPos, SpeedMine.INSTANCE.getTool(SpeedMine.secondPos), 0.90))) {
                if (mc.player.getMainHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                    if (!mc.options.useKey.isPressed()) {
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(SpeedMine.INSTANCE.getTool(SpeedMine.secondPos)));
                        swithc2 = 1;
                    } else {
                        if (swithc2 == 1){
                            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slotMain));
                            EntityUtil.syncInventory();
                        }
                    }
                } else {
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(SpeedMine.INSTANCE.getTool(SpeedMine.secondPos)));
                    swithc2 = 1;
                }
            }
        }
        if (SpeedMine.secondPos != null && SpeedMine.INSTANCE.secondTimer.passed(SpeedMine.INSTANCE.getBreakTime(SpeedMine.secondPos, SpeedMine.INSTANCE.getTool(SpeedMine.secondPos), 1.2))) {
            if (swithc2 == 1) {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slotMain));
                EntityUtil.syncInventory();
            }
        }
    }
}
