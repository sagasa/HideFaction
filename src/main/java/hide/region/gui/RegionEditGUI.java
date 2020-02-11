package hide.region.gui;

import hide.core.HideFaction;
import hide.core.gui.FactionGUIHandler;
import hide.core.gui.FactionGUIHandler.HideGuiProvider;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class RegionEditGUI extends GuiScreen{

	public static int ID = FactionGUIHandler.register(new HideGuiProvider() {
		@Override
		public Object getServerGuiElement(EntityPlayer player, World world, int x, int y, int z) {
			return null;
		}

		@Override
		public Object getClientGuiElement(EntityPlayer player, World world, int x, int y, int z) {
			System.out.println("make GUI");
			return new RegionEditGUI();
		}
	});

	public RegionEditGUI() {
		buttonList.add(new GuiButton(0, 10, 10, "test"));
	}

	private static final ResourceLocation GUITEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

	private static final ResourceLocation TEXTURE = new ResourceLocation(HideFaction.MODID,
			"textures/gui/terminal.png");

}
