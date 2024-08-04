package me.nullpoint.mod.gui.alts;

import me.nullpoint.Nullpoint;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class TokenLoginScreen extends Screen {

	private final Screen parent;

	private TextFieldWidget textFieldAltName;
	private TextFieldWidget textFieldAltToken;
	private TextFieldWidget textFieldAltUID;


	protected TokenLoginScreen(Screen parent) {
		super(Text.of("Token Login"));
		this.parent = parent;
	}

	public void init() {
		super.init();

		this.textFieldAltName = new TextFieldWidget(textRenderer, this.width / 2 - 100, height / 2 + 4, 200, 20, Text.of("Enter Name"));
		this.addDrawableChild(this.textFieldAltName);

		this.textFieldAltToken = new TextFieldWidget(textRenderer, this.width / 2 - 100, height / 2 - 76, 200, 20, Text.of("Enter Token"));
		this.textFieldAltToken.setMaxLength(Integer.MAX_VALUE);
		this.addDrawableChild(this.textFieldAltToken);

		this.textFieldAltUID = new TextFieldWidget(textRenderer, this.width / 2 - 100, height / 2 - 36, 200, 20, Text.of("Enter UID"));
		this.textFieldAltUID.setMaxLength(Integer.MAX_VALUE);
		this.addDrawableChild(this.textFieldAltUID);

		addDrawableChild(ButtonWidget.builder(Text.of("Login"), b -> this.onButtonLoginPressed()).dimensions(this.width / 2 - 100, this.height / 2 + 24 + 8, 200, 20).build());

		this.addDrawableChild(ButtonWidget.builder(Text.of("Cancel"), b -> client.setScreen(this.parent))
				.dimensions(this.width / 2 - 100, this.height / 2 + 46 + 8, 200, 20).build());
	}

	private void onButtonLoginPressed() {
		Nullpoint.ALT.loginToken(textFieldAltName.getText(), textFieldAltToken.getText(), textFieldAltUID.getText());
		client.setScreen(this.parent);
	}

	@Override
	public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
		drawContext.drawCenteredTextWithShadow(textRenderer, this.title.getString(), this.width / 2, 20, 16777215);
		drawContext.drawTextWithShadow(textRenderer, "Enter Token", this.width / 2 - 100, height / 2 - 90, 16777215);
		drawContext.drawTextWithShadow(textRenderer, "Enter UUID", this.width / 2 - 100, height / 2 - 50, 16777215);
		drawContext.drawTextWithShadow(textRenderer, "Enter Name", this.width / 2 - 100, height / 2 - 10, 16777215);
		this.textFieldAltName.render(drawContext, mouseX, mouseY, partialTicks);
		this.textFieldAltToken.render(drawContext, mouseX, mouseY, partialTicks);
		this.textFieldAltUID.render(drawContext, mouseX, mouseY, partialTicks);
		super.render(drawContext, mouseX, mouseY, partialTicks);
	}
}
