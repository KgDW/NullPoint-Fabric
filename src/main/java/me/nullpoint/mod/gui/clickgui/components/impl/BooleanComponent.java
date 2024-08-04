package me.nullpoint.mod.gui.clickgui.components.impl;

import me.nullpoint.api.managers.GuiManager;
import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.api.utils.render.TextUtil;
import me.nullpoint.mod.gui.clickgui.components.Component;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.gui.clickgui.tabs.ClickGuiTab;
import me.nullpoint.mod.modules.impl.client.UIModule;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class BooleanComponent extends Component {

	final BooleanSetting setting;

	public BooleanComponent(ClickGuiTab parent, BooleanSetting setting) {
		super();
		this.parent = parent;
		this.setting = setting;
	}

	@Override
	public boolean isVisible() {
		if (setting.visibility != null) {
			return setting.visibility.test(null);
		}
		return true;
	}

	boolean hover = false;
	@Override
	public void update(int offset, double mouseX, double mouseY, boolean mouseClicked) {
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();
		if (GuiManager.currentGrabbed == null && isVisible() && (mouseX >= ((parentX + 1)) && mouseX <= (((parentX)) + parentWidth - 1)) && (mouseY >= (((parentY + offset))) && mouseY <= ((parentY + offset) + defaultHeight - 2))) {
			hover = true;
			if (mouseClicked) {
				ClickGuiScreen.clicked = false;
				setting.toggleValue();
			}
			if (ClickGuiScreen.rightClicked) {
				ClickGuiScreen.rightClicked = false;
				setting.popped = !setting.popped;
			}
		} else {
			hover = false;
		}
	}

	public double currentWidth = 0;
	@Override
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		currentOffset = animate(currentOffset, offset);
		if (back && Math.abs(currentOffset - offset) <= 0.5) {
			currentWidth = 0;
			return false;
		}
		int x = parent.getX();
		int y = (int) (parent.getY() + currentOffset - 2);
		int width = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();

		Render2DUtil.drawRect(matrixStack, (float) x + 1, (float) y + 1, (float) width - 2, (float) defaultHeight - 1, hover ? UIModule.INSTANCE.shColor.getValue() : UIModule.INSTANCE.sbgColor.getValue());

		currentWidth = animate(currentWidth, setting.getValue() ? (width - 2D) : 0D, UIModule.INSTANCE.booleanSpeed.getValue());
		Render2DUtil.drawRect(matrixStack, (float) x + 1, (float) y + 1, (float) currentWidth, (float) defaultHeight - 1, hover ? UIModule.INSTANCE.mainHover.getValue() : color);
		TextUtil.drawString(drawContext, setting.getName(), x + 4, y + getTextOffsetY(), new Color(-1).getRGB());
		if (setting.parent) {
			TextUtil.drawString(drawContext, setting.popped ? "-" : "+", x + width - 11,
					y + getTextOffsetY(), new Color(255, 255, 255).getRGB());
		}
		return true;
	}
}
