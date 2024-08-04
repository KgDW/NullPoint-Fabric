/**
 * A class to represent a ClickGui Tab that contains different Components.
 */

package me.nullpoint.mod.gui.clickgui.tabs;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.GuiManager;
import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.api.utils.render.TextUtil;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.gui.clickgui.components.Component;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.UIModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.ArrayList;


public class ClickGuiTab extends Tab {
	protected String title;
	protected final boolean drawBorder = true;
	private Module.Category category = null;
	protected final ArrayList<Component> children = new ArrayList<>();

	public ClickGuiTab(String title, int x, int y) {
		this.title = title;
		this.x = Nullpoint.CONFIG.getInt(title + "_x", x);
		this.y = Nullpoint.CONFIG.getInt(title + "_y", y);
		this.width = 100;
		this.mc = MinecraftClient.getInstance();
	}

	public ClickGuiTab(Module.Category category, int x, int y) {
		this(category.name(), x, y);
		this.category = category;
	}
	public ArrayList<Component> getChildren() {
		return children;
	}

	public final String getTitle() {
		return title;
	}

	public final void setTitle(String title) {

		this.title = title;
	}

	public final int getX() {
		return x;
	}

	public final void setX(int x) {
		this.x = x;
	}

	public final int getY() {
		return y;
	}

	public final void setY(int y) {
		this.y = y;
	}

	public final int getWidth() {
		return width;
	}

	public final void setWidth(int width) {
		this.width = width;
	}

	public final int getHeight() {
		return height;
	}

	public final void setHeight(int height) {
		this.height = height;
	}

	public final boolean isGrabbed() {
		return (GuiManager.currentGrabbed == this);
	}

	public final void addChild(Component component) {
		this.children.add(component);
	}

	boolean popped = true;

	@Override
	public void update(double mouseX, double mouseY, boolean mouseClicked) {
		onMouseClick(mouseX, mouseY, mouseClicked);
		if (popped) {
			int tempHeight = 1;
			for (Component child : children) {
				tempHeight += (child.getHeight());
			}
			this.height = tempHeight;
			int i = defaultHeight;
			for (Component child : this.children) {
				child.update(i, mouseX, mouseY, mouseClicked);
				i += child.getHeight();
			}
		}
	}

	public void onMouseClick(double mouseX, double mouseY, boolean mouseClicked) {
		if (GuiManager.currentGrabbed == null) {
			if (mouseX >= (x) && mouseX <= (x + width)) {
				if (mouseY >= (y) && mouseY <= (y + 13)) {
					if (mouseClicked) {
						GuiManager.currentGrabbed = this;
					}
					else if (ClickGuiScreen.rightClicked) {
						popped = !popped;
						ClickGuiScreen.rightClicked = false;
					}
				}
			}
		}
	}

	public double currentHeight = 0;

	@Override
	public void draw(DrawContext drawContext, float partialTicks, Color color) {
		int tempHeight = 1;
		for (Component child : children) {
			tempHeight += (child.getHeight());
		}
		this.height = tempHeight;

		MatrixStack matrixStack = drawContext.getMatrices();
		currentHeight = Component.animate(currentHeight, height);
		if (drawBorder) {
			if (UIModule.INSTANCE.categoryEnd.booleanValue) {
				Render2DUtil.drawRectVertical(matrixStack, x, y - 2, width, 15, color, UIModule.INSTANCE.categoryEnd.getValue());
			} else {
				Render2DUtil.drawRect(matrixStack, x, y - 2, width, 15, color.getRGB());
			}
			Render2DUtil.drawRect(matrixStack, x, y - 2 + 15, width, 1, new Color(38, 38, 38));
			if (popped) Render2DUtil.drawRect(matrixStack, x, y + 14, width, (int) currentHeight, UIModule.INSTANCE.bgColor.getValue());
		}
		if (popped) {
			int i = defaultHeight;
			for (Component child : children) {
				if (child.isVisible()) {
					child.draw(i, drawContext, partialTicks, color, false);
					i += child.getHeight();
				} else {
					child.currentOffset = i - defaultHeight;
				}
			}
		}
		//TextUtil.drawCustomText(drawContext, this.title, x + (float) width / 2 - (float) mc.textRenderer.getWidth(title), y + 4, new Color(255, 255, 255));
		TextUtil.drawString(drawContext, this.title, x + width / 2d - TextUtil.getWidth(title) / 2, y + 2, new Color(255, 255, 255));
		if (category != null) {
			String text = "[" + Nullpoint.MODULE.categoryModules.get(category) + "]";
			TextUtil.drawStringWithScale(drawContext, text, x + width - 4 - TextUtil.getWidth(text) * 0.5F, y + 2, new Color(255, 255, 255), 0.5f);
		}
	}
}
