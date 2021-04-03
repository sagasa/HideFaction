package hide.chat.gui;

import hide.core.HideFaction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiHideButton extends GuiButton {

	protected static final ResourceLocation HIDE_BUTTON_TEXTURES = new ResourceLocation(HideFaction.MODID,
			"textures/gui/button.png");

	public boolean isUp;

	public boolean isSelected = false;

	public boolean canClick = true;

	public GuiHideButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, boolean up) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		isUp = up;

	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		return this.enabled && this.visible && pointOver(mouseX, mouseY) && canClick;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			FontRenderer fontrenderer = mc.fontRenderer;
			mc.getTextureManager().bindTexture(HIDE_BUTTON_TEXTURES);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = isSelected || pointOver(mouseX, mouseY);
			int i = this.getHoverState(this.hovered);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			this.drawTexturedModalRect(this.x, this.y, 0, (isUp ? 0 : 60) + i * 20, this.width / 2, this.height);
			this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, (isUp ? 0 : 60) + i * 20,
					this.width / 2, this.height);
			this.mouseDragged(mc, mouseX, mouseY);
			int j = 14737632;

			if (packedFGColour != 0) {
				j = packedFGColour;
			} else if (!this.enabled) {
				j = 10526880;
			} else if (this.hovered) {
				j = 16777120;
			}

			this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2,
					this.y + (this.height - 8) / 2, j);
		}
	}

	protected boolean pointOver(int mouseX, int mouseY) {
		return mouseX >= this.x + 4 && mouseY >= this.y && mouseX < this.x + this.width - 4
				&& mouseY < this.y + this.height;
	}
}
