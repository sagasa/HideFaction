package hide.region.gui;

import hide.core.HideFaction;
import hide.region.RegionRect;
import hide.region.network.PacketRegionEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class RegionAddGUI extends GuiScreen {

	private static final int addRegion = 0x0;

	@Override
	public void initGui() {
		super.initGui();
		int w = width / 2, h = height / 2;

		buttonList.add(new GuiButton(addRegion, w + 38, h + 20, 32, 20, "Add"));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		int w, h;

		w = width / 2;
		h = height / 2;

		drawRect(w - 80, h - 115, w + 80, h + 70, 0xFFaeaeae);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		int id = button.id;

		if (id == addRegion) {

			RegionRect rg = new RegionRect();
			rg.setPos(mc.player.getPosition(), mc.player.getPosition().add(1, 1, 1));
			HideFaction.NETWORK.sendToServer(PacketRegionEdit.addRegion(rg));
			HideFaction.NETWORK.sendToServer(PacketRegionEdit.register());
		}
	}

	@Override
	public void onResize(Minecraft mcIn, int w, int h) {
		super.onResize(mcIn, w, h);
		w = this.width / 2;
		h = this.height / 2;

	}
}
