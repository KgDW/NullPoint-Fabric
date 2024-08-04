package me.nullpoint.mod.gui.alts;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.alts.Alt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AltSelectionList extends ElementListWidget<AltSelectionList.Entry> {
	private final AltScreen owner;
	private final List<AltSelectionList.NormalEntry> altList = new ArrayList<>();

	public AltSelectionList(AltScreen ownerIn, MinecraftClient minecraftClient, int i, int j, int k, int l) {
		super(minecraftClient, i, j, k, l);
		this.owner = ownerIn;
	}

	public void updateAlts() {
		this.clearEntries();
		for (Alt alt : Nullpoint.ALT.getAlts()) {
			AltSelectionList.NormalEntry entry = new AltSelectionList.NormalEntry(this.owner, alt);
			altList.add(entry);
		}
		this.setList();
	}

	private void setList() {
		this.altList.forEach(this::addEntry);
	}

	public void setSelected(@Nullable AltSelectionList.Entry entry) {
		super.setSelected(entry);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		AltSelectionList.Entry AltSelectionList$entry = this.getSelectedOrNull();
		return AltSelectionList$entry != null && AltSelectionList$entry.keyPressed(keyCode, scanCode, modifiers)
				|| super.keyPressed(keyCode, scanCode, modifiers);
	}

	public abstract static class Entry extends ElementListWidget.Entry<AltSelectionList.Entry> {
	}

	public class NormalEntry extends AltSelectionList.Entry {
		private final AltScreen owner;
		private final MinecraftClient mc;
		private final Alt alt;
		private long lastClickTime;

		protected NormalEntry(AltScreen ownerIn, Alt alt) {
			this.owner = ownerIn;
			this.alt = alt;
			this.mc = MinecraftClient.getInstance();
		}

		public Alt getAltData() {
			return this.alt;
		}

		@Override
		public void render(DrawContext drawContext, int index, int y, int x, int entryWidth, int entryHeight,
				int mouseX, int mouseY, boolean hovered, float tickDelta) {

			if (hovered) drawContext.fill(x, y, x + entryWidth, y+ entryHeight, new Color(255, 255, 255, 100).getRGB());
			// Draws the strings onto the screen.
			TextRenderer textRenderer = this.mc.textRenderer;
			drawContext.drawTextWithShadow(textRenderer, "Username: " + this.alt.getEmail(), (x + 32 + 3),
					(y + 2), 16777215);
			drawContext.drawTextWithShadow(textRenderer, "Username: " + this.alt.getEmail(), (x + 32 + 3),
					(y + 2), 16777215);
		}

		@Override
		public List<? extends Element> children() {
			return Collections.emptyList();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return Collections.emptyList();
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			double d0 = mouseX - (double) AltSelectionList.this.getRowLeft();

			if (d0 <= 32.0D) {
				if (d0 < 32.0D && d0 > 16.0D) {
					this.owner.setSelected(this);
					this.owner.loginToSelected();
					return true;
				}
			}
			this.owner.setSelected(this);
			if (Util.getMeasuringTimeMs() - this.lastClickTime < 250L) {
				this.owner.loginToSelected();
			}
			this.lastClickTime = Util.getMeasuringTimeMs();
			return false;
		}
	}
}
