package hide.region.gui;

import java.io.IOException;

import hide.core.HideFaction;
import hide.core.network.PacketSimpleCmd;
import hide.core.network.PacketSimpleCmd.Cmd;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
public class RegionEditGUI extends GuiScreen{


	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(new GuiButton(0, 10, 10,20,20, "test"));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		HideFaction.NETWORK.sendToServer(new PacketSimpleCmd(Cmd.S));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		drawString(fontRenderer, "true", 40, 40, 0);
	}

	private static final ResourceLocation GUITEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

	private static final ResourceLocation TEXTURE = new ResourceLocation(HideFaction.MODID,
			"textures/gui/terminal.png");

}
