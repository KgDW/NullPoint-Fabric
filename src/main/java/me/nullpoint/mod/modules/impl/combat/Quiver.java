package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;

import java.util.List;

public class Quiver extends Module {
	public static Quiver INSTANCE;
	private final BooleanSetting smart = add(new BooleanSetting("Smart", true));
	public Quiver() {
		super("Quiver", Category.Combat);
		INSTANCE = this;
	}

	@EventHandler(priority = -101)
	public void onRotate(RotateEvent event) {
		if (mc.player.isUsingItem() && mc.player.getActiveItem().getItem() instanceof BowItem) {
			if (!smart.getValue()) {
				event.setPitch(-90);
			} else {
				boolean rotate = false;
				for (int i = 9; i < 45; ++i) {
					ItemStack stack = mc.player.getInventory().getStack(i);
					if (stack.getItem() == Items.ARROW) {
						rotate = false;
						continue;
					}
					if (stack.getItem() == Items.SPECTRAL_ARROW) {
						rotate = false;
						continue;
					}
					if (stack.getItem() == Items.TIPPED_ARROW) {
						boolean good = false;
						List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(stack);
						for (StatusEffectInstance effect : effects) {
							if (effect.getEffectType() == StatusEffects.SPEED || effect.getEffectType() == StatusEffects.STRENGTH) {
								good = true;
							}
						}
						rotate = good;
					}
				}
				if (rotate) {
					event.setPitch(-90);
				}
			}
		}
	}
}