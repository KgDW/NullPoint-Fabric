package me.nullpoint.mod.gui.alts;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.alts.Alt;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class EditAltScreen extends Screen {

	private final AltScreen parent;
	private final Alt alt;

	private ButtonWidget buttonSaveAlt;
	private CheckboxWidget toggleMicrosoft;
	private TextFieldWidget textFieldAltUsername;

	public EditAltScreen(AltScreen parentScreen, Alt alt) {
		super(Text.of("Alt Manager"));
		this.parent = parentScreen;
		this.alt = alt;
	}

	public void init() {
		super.init();
		this.textFieldAltUsername = new TextFieldWidget(textRenderer, this.width / 2 - 100, height / 2 - 76, 200, 20,
				Text.of("Enter Name"));
		this.textFieldAltUsername.setText(this.alt == null ? "" : alt.getEmail());
		this.addDrawableChild(this.textFieldAltUsername);

		addDrawableChild(this.buttonSaveAlt = ButtonWidget.builder(Text.of("Save Alt"), b -> this.onButtonAltEditPressed())
		 		.dimensions(this.width / 2 - 100, this.height / 2 + 24, 200, 20).build());
		this.addDrawableChild(ButtonWidget.builder(Text.of("Cancel"), b -> this.onButtonCancelPressed())
		 		.dimensions(this.width / 2 - 100, this.height / 2 + 46, 200, 20).build());
	}

	@Override
	public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
		drawContext.drawCenteredTextWithShadow(textRenderer, "Edit Alternate Account", this.width / 2, 20, 16777215);
		drawContext.drawTextWithShadow(textRenderer, "Username:", this.width / 2 - 100, height / 2 - 90, 16777215);
		super.render(drawContext, mouseX, mouseY, partialTicks);
	}

	private void onButtonAltEditPressed() {
		alt.setEmail(this.textFieldAltUsername.getText());
		Nullpoint.ALT.saveAlts();
		this.parent.refreshAltList();
	}

	public void onButtonCancelPressed() {
		client.setScreen(this.parent);
	}
}