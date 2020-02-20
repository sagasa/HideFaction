package hide.region.gui;

import hide.region.RegionManager;
import hide.region.RegionRect;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RegionEditor {
	public boolean EditMode = true;

	private int index = -1;

	public void draw(float partialTicks) {
		RegionManager rm = RegionManager.getManager();
		for (RegionRect rg : rm.RegionList) {

		}
	}

	public void select() {

	}

	public void edit() {

	}
}
