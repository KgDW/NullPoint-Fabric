package me.nullpoint.mod.gui.alts;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.alts.Alt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AltScreen extends Screen {
	private final Screen parentScreen;
	private ButtonWidget editButton;
	private ButtonWidget deleteButton;
	private AltSelectionList altListSelector;

	public AltScreen(Screen parentScreen) {
		super(Text.of("Alt Manager"));
		this.parentScreen = parentScreen;
	}

	public void init() {
		super.init();
		this.altListSelector = new AltSelectionList(this, this.client, this.width, this.height, 32, this.height - 64);
		this.altListSelector.updateAlts();
		this.addDrawableChild(this.altListSelector);

		this.deleteButton = ButtonWidget.builder(Text.of("Delete Alt"), b -> this.deleteSelected())
				.dimensions(this.width / 2 - 154, this.height - 28, 100, 20).build();
		this.deleteButton.active = false;
		this.addDrawableChild(this.deleteButton);

		this.addDrawableChild(ButtonWidget.builder(Text.of("Token Login"), b -> client.setScreen(new TokenLoginScreen(this)))
				.dimensions(this.width / 2 - 154, this.height - 52, 100, 20).build());

		this.addDrawableChild(ButtonWidget.builder(Text.of("Direct Login"), b -> client.setScreen(new DirectLoginAltScreen(this)))
				.dimensions(this.width / 2 - 50, this.height - 52, 100, 20).build());
		
		this.addDrawableChild(ButtonWidget.builder(Text.of("Add Alt"), b -> client.setScreen(new AddAltScreen(this)))
				.dimensions(this.width / 2 + 54, this.height - 52, 100, 20).build());

		this.addDrawableChild(ButtonWidget.builder(Text.of("Cancel"), b -> client.setScreen(this.parentScreen))
		 		.dimensions(this.width / 2 + 54, this.height - 28, 100, 20).build());
		
		this.editButton = ButtonWidget.builder(Text.of("Edit Alt"), b -> this.editSelected())
				.dimensions(this.width / 2 - 50, this.height - 28, 100, 20).build();
		this.editButton.active = false;
		this.addDrawableChild(this.editButton);
	}
	
	@Override
	public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
		this.altListSelector.render(drawContext, mouseX, mouseY, partialTicks);

		super.render(drawContext, mouseX, mouseY, partialTicks);
		drawContext.drawCenteredTextWithShadow(textRenderer,"Currently Logged Into: " + MinecraftClient.getInstance().getSession().getUsername(),
				this.width / 2, 20, 16777215);
	}

	@Override
	public void close() {
		this.client.setScreen(parentScreen);
	}

	public void refreshAltList() {
		this.client.setScreen(new AltScreen(this.parentScreen));
	}

	public void setSelected(AltSelectionList.Entry selected) {
		this.altListSelector.setSelected(selected);
		this.setEdittable();
	}
	
	protected void setEdittable() {
		this.editButton.active = true;
		this.deleteButton.active = true;
	}
	
	public void loginToSelected() {
		AltSelectionList.Entry altselectionlist$entry = this.altListSelector.getSelectedOrNull();
		if (altselectionlist$entry == null) {
			return;
		}
			
		Alt alt = ((AltSelectionList.NormalEntry) altselectionlist$entry).getAltData();
		Nullpoint.ALT.loginCracked(alt.getEmail());
	}
	
	public void editSelected() {
		Alt alt = ((AltSelectionList.NormalEntry)this.altListSelector.getSelectedOrNull()).getAltData();
		if (alt == null) {
			return;
		}
		client.setScreen(new EditAltScreen(this, alt));
	}
	
	public void deleteSelected() {
		Alt alt = ((AltSelectionList.NormalEntry)this.altListSelector.getSelectedOrNull()).getAltData();
		if (alt == null) {
			return;
		}
		Nullpoint.ALT.removeAlt(alt);
		this.refreshAltList();
	}
}
