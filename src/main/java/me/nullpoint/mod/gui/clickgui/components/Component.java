package me.nullpoint.mod.gui.clickgui.components;

import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.mod.gui.clickgui.tabs.ClickGuiTab;
import me.nullpoint.mod.modules.impl.client.UIModule;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public abstract class Component implements Wrapper {
	public int defaultHeight = 16;
	protected ClickGuiTab parent;
	private int height = defaultHeight;
	
	public Component() {
	}

	public boolean isVisible() {
		return true;
	}
	
	public int getHeight()
	{
		if (!isVisible()) {
			return 0;
		}
		return height;
	}
	
	public void setHeight(int height)
	{
		this.height = height;
	}
	
	public ClickGuiTab getParent()
	{
		return parent;
	}
	
	public void setParent(ClickGuiTab parent)
	{
		this.parent = parent;
	}

	public abstract void update(int offset, double mouseX, double mouseY, boolean mouseClicked);
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		this.currentOffset = offset;
		return false;
	}
	public double currentOffset = 0;

	public double getTextOffsetY() {
		return (defaultHeight - Wrapper.mc.textRenderer.fontHeight) / 2D + 1;
	}

	public static double animate(double current, double endPoint) {
		return animate(current, endPoint, UIModule.INSTANCE.animationSpeed.getValue());
	}

	public static double animate(double current, double endPoint, double speed) {
		if (speed >= 1) return endPoint;
		if (speed == 0) return current;
		return AnimateUtil.animate(current, endPoint, speed, UIModule.INSTANCE.animMode.getValue());
	}
}
