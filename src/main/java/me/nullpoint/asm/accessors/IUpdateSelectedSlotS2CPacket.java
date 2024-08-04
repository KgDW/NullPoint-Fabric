package me.nullpoint.asm.accessors;

import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(UpdateSelectedSlotS2CPacket.class)
public interface IUpdateSelectedSlotS2CPacket {
    @Mutable
    @Accessor("slot")
    void setslot(int selectedSlot);
}
