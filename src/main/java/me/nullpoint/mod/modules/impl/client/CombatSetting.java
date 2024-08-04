package me.nullpoint.mod.modules.impl.client;

import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.Placement;
import me.nullpoint.mod.modules.settings.SwingSide;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class CombatSetting extends Module {
    public static CombatSetting INSTANCE;
    public final BooleanSetting invSwapBypass = add(new BooleanSetting("InvSwapBypass", true));
    public final BooleanSetting lowVersion = add(new BooleanSetting("1.12", false));
    public final BooleanSetting rotateSync = add(new BooleanSetting("RotateSync", true));
    public final BooleanSetting packetPlace = add(new BooleanSetting("PacketPlace", true));
    public final BooleanSetting randomPitch = add(new BooleanSetting("RandomPitch", false));
    public final BooleanSetting rotations = add(new BooleanSetting("ShowRotations", true));
    public final BooleanSetting attackRotate = add(new BooleanSetting("AttackRotate", false));
    public final EnumSetting<Placement> placement = add(new EnumSetting<>("Placement", Placement.Vanilla));
    public final SliderSetting rotateTime = add(new SliderSetting("RotateTime", 0.5, 0, 1, 0.01));
    public final SliderSetting attackDelay = add(new SliderSetting("AttackDelay", 0.2, 0, 1, 0.01));
    public final SliderSetting tp = add(new SliderSetting("TP", 50, 0, 300, 0.01));

    public final BooleanSetting test = add(new BooleanSetting("Test", true));

    public final SliderSetting boxSize = add(new SliderSetting("BoxSize", 0.6, 0, 1, 0.01));
    public final BooleanSetting inventorySync = add(new BooleanSetting("InventorySync", false));
    public final EnumSetting<SwingSide> swingMode = add(new EnumSetting<>("SwingMode", SwingSide.Server));
    public final BooleanSetting obsMode = add(new BooleanSetting("OBSServer", false));
    public CombatSetting() {
        super("CombatSetting", Category.Client);
        INSTANCE = this;
    }

    public static double getOffset() {
        if (INSTANCE != null) return INSTANCE.boxSize.getValue() / 2;
        return 0.3;
    }
    @Override
    public void enable() {
        this.state = true;
    }

    @Override
    public void disable() {
        this.state = true;
    }

    @Override
    public boolean isOn() {
        return true;
    }
}
