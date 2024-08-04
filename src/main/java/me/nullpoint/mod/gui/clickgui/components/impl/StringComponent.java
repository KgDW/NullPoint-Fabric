package me.nullpoint.mod.gui.clickgui.components.impl;

import me.nullpoint.api.managers.GuiManager;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.api.utils.render.TextUtil;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.gui.clickgui.components.Component;
import me.nullpoint.mod.gui.clickgui.tabs.ClickGuiTab;
import me.nullpoint.mod.modules.impl.client.UIModule;
import me.nullpoint.mod.modules.settings.impl.StringSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class StringComponent extends Component {
	private final StringSetting setting;

	public StringComponent(ClickGuiTab parent, StringSetting setting) {
		super();
		this.setting = setting;
		this.parent = parent;
	}
	@Override
	public boolean isVisible() {
		if (setting.visibility != null) {
			return setting.visibility.test(null);
		}
		return true;
	}

	boolean hover = false;

	public void update(int offset, double mouseX, double mouseY, boolean mouseClicked) {
		if (GuiManager.currentGrabbed == null && isVisible()) {
			int parentX = parent.getX();
			int parentY = parent.getY();
			int parentWidth = parent.getWidth();
			if ((mouseX >= ((parentX + 1)) && mouseX <= (((parentX)) + parentWidth - 1)) && (mouseY >= (((parentY + offset))) && mouseY <= ((parentY + offset) + defaultHeight - 2))) {
				hover = true;
				if (mouseClicked) {
					ClickGuiScreen.clicked = false;
					setting.setListening(!setting.isListening());
				}
			} else {
				if(mouseClicked && setting.isListening()) {
					setting.setListening(false);
				}
				hover = false;
			}
		} else {
			if (setting.isListening()) {
				setting.setListening(false);
			}
			hover = false;
		}
	}

	private final Timer timer = new Timer();
	boolean b;
	@Override
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		if (timer.passed(1000)) {
			b = !b;
			timer.reset();
		}
		if (back) {
			setting.setListening(false);
		}
		int parentX = this.parent.getX();
		int parentY = this.parent.getY();
		currentOffset = animate(currentOffset, offset);
		if (back && Math.abs(currentOffset - offset) <= 0.5) {
			return false;
		}
		int y = (int) (parent.getY() + currentOffset - 2);
		int width = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();
		String text = setting.getValue();
		if (setting.isListening() && b) {
			text = text + "_";
		}
		String name = setting.isListening() ? "[E]" : setting.getName();
		if (hover)
			Render2DUtil.drawRect(matrixStack, (float) parentX + 1, (float) y + 1, (float) width - 3, (float) defaultHeight - 1, UIModule.INSTANCE.shColor.getValue());
		TextUtil.drawString(drawContext, text, (float) (parentX + 4 + TextUtil.getWidth(name) / 2),
				(float) (parentY + getTextOffsetY() + currentOffset) - 2, 0xFFFFFF);
		TextUtil.drawStringWithScale(drawContext, name, (float) (parentX + 4),
				(float) (parentY + getTextOffsetY() + currentOffset - 2), -1, 0.5f);
		return true;
	}
}