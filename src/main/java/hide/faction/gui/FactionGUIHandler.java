package hide.faction.gui;

import hide.core.HideFaction;
import hide.faction.data.FactionData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class FactionGUIHandler implements IGuiHandler {

	static FactionData data = new FactionData();

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == HideFaction.GUI_ID) {
			return new FactionContainer(player, data);
		}
		return null;
	}

	/* クライアント側の処理 */
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == HideFaction.GUI_ID) {
			return new FactionGuiContainer(player, data);
		}
		return null;
	}

}
