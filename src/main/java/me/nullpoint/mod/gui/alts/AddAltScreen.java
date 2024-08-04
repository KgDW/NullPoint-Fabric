package me.nullpoint.mod.gui.alts;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.alts.Alt;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AddAltScreen extends Screen {

	private final AltScreen parent;

	private TextFieldWidget textFieldAltUsername;

	public AddAltScreen(AltScreen parentScreen) {
		super(Text.of("Alt Manager"));
		this.parent = parentScreen;
	}

	public void init() {
		super.init();

		textFieldAltUsername = new TextFieldWidget(textRenderer, this.width / 2 - 100, height / 2 - 76, 200, 20,
				Text.of("Enter Name"));
		textFieldAltUsername.setText("");
		addDrawableChild(this.textFieldAltUsername);

		addDrawableChild(ButtonWidget.builder(Text.of("Add Alt"), b -> this.onButtonAltAddPressed())
				.dimensions(this.width / 2 - 100, this.height / 2 + 24, 200, 20).build());

		addDrawableChild(ButtonWidget.builder(Text.of("Cancel"), b -> this.onButtonCancelPressed())
				.dimensions(this.width / 2 - 100, this.height / 2 + 46, 200, 20).build());
	}

	@Override
	public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
		drawContext.drawCenteredTextWithShadow(textRenderer, "Add Alternate Account", this.width / 2, 20, 16777215);
		drawContext.drawCenteredTextWithShadow(textRenderer, "Username:", this.width / 2 - 100, height / 2 - 90, 16777215);
		super.render(drawContext, mouseX, mouseY, delta);
	}

	private void onButtonAltAddPressed() {
		Alt alt = new Alt(this.textFieldAltUsername.getText());
		Nullpoint.ALT.addAlt(alt);
		this.parent.refreshAltList();
	}

	public void onButtonCancelPressed() {
		client.setScreen(this.parent);
	}
}
