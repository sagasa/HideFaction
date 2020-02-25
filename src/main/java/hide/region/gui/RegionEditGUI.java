package hide.region.gui;

import java.io.IOException;

import hide.core.HideFaction;
import hide.core.network.PacketSimpleCmd;
import hide.core.network.PacketSimpleCmd.Cmd;
import hide.region.RegionRect;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public class RegionEditGUI extends GuiScreen {

	private RegionRect region;

	public RegionEditGUI(RegionRect regionRect) {
		region = regionRect;
	}
	private static final int add = 0x00000;
	private static final int remove = 0x00000;

	private static final int x1 = 0x00000;
	private static final int x5 = 0x00000;
	private static final int x10 = 0x00000;

	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(new GuiButton(0, 10, 10, 20, 20, "test"));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		HideFaction.NETWORK.sendToServer(new PacketSimpleCmd(Cmd.S));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		drawString(fontRenderer, region.getRuleName(), 40, 40, 0);
	}

	private static final ResourceLocation GUITEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

	private static final ResourceLocation TEXTURE = new ResourceLocation(HideFaction.MODID,
			"textures/gui/terminal.png");

}
