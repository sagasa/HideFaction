package hide.region.gui;

import hide.region.RegionManager;
import hide.region.RegionRect;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RegionEditor {
	public static boolean EditMode = true;

	private static int index = -1;

	public static void draw(float partialTicks) {
		RegionManager rm = RegionManager.getManager();
		for (int i = 0; i < rm.RegionList.size(); i++) {
			//選択中なら
			if (i == index)
				rm.RegionList.get(i).drawRegionRect(true, partialTicks, 0.8f, 1f, 0);
			else
				rm.RegionList.get(i).drawRegionRect(true, partialTicks, 0.5f, 1f, 0.5f);

		}
	}

	public static void select() {

		RegionManager rm = RegionManager.getManager();
		for (RegionRect rg : rm.RegionList) {

		}
	}

	public static void edit() {

	}
}
