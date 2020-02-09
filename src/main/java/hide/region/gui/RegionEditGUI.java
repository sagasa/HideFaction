package hide.region.gui;

import hide.core.HideFaction;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public class RegionEditGUI extends GuiScreen{

	public RegionEditGUI() {
		buttonList.add(new GuiButton(0, 10, 10, "test"));
	}

	private static final ResourceLocation GUITEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

	private static final ResourceLocation TEXTURE = new ResourceLocation(HideFaction.MODID,
			"textures/gui/terminal.png");

}
