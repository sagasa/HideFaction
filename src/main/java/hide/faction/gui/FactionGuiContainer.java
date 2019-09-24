package hide.faction.gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import hide.faction.HideFaction;
import hide.faction.data.FactionData;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public class FactionGuiContainer extends GuiContainer {
	public FactionGuiContainer(EntityPlayer player, FactionData data) {
		super(new FactionContainer(player, data));
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(new GuiButton(0, 100, 100, "AAAAAAAA"));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		System.out.println("HelloWorld");
	}

	private static final ResourceLocation GUITEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

	public FactionGuiContainer(Container inventorySlotsIn) {
		super(inventorySlotsIn);
		ySize = 222;
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation(HideFaction.MODID,
			"textures/gui/terminal.png");

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(GUITEXTURE);
		int k = (width - xSize) / 2;
		int l = (height - ySize) / 2;
		this.drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
		// this.mc.renderEngine.bindTexture(TEXTURE);
		// drawScaledCustomSizeModalRect(guiLeft, guiTop, 0, 0, 195, 168, xSize, ySize,
		// 256f, 256f);
	}
}