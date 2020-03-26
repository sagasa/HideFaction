package hide.region.gui;

import hide.region.RegionManager;
import hide.region.RegionRect;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RegionEditor {

	static {
		RegionManager.addListener(RegionEditor::checkIndex);
	}

	public static void checkIndex() {
		if (RegionManager.getManager().RegionList.size() < index + 1)
			index = -1;
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.currentScreen != null && mc.currentScreen instanceof RegionEditGUI) {
			if (index == -1)
				mc.displayGuiScreen(null);
			else
				mc.displayGuiScreen(new RegionEditGUI((byte) index));
		}
	}

	public static boolean EditMode = true;

	private static int index = -1;

	public static void draw(float partialTicks) {
		RegionManager rm = RegionManager.getManager();
		for (int i = 0; i < rm.RegionList.size(); i++) {
			// 選択中なら
			if (i == index)
				rm.RegionList.get(i).drawRegionRect(true, partialTicks, 0.8f, 1f, 0);
			else
				rm.RegionList.get(i).drawRegionRect(false, partialTicks, 0.5f, 1f, 0.5f);
		}
	}

	/** indexの値を変更する */
	public static void select() {
		if (!EditMode)
			return;
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player == null)
			return;
		Vec3d start = player.getPositionVector().addVector(0, player.eyeHeight, 0);
		Vec3d end = start.add(player.getLook(1.0F).scale(40));
		RegionManager rm = RegionManager.getManager();
		// 最初に当たったレギオン
		int minHit = -1;
		for (int i = 0; i < rm.RegionList.size(); i++) {
			RegionRect rg = rm.RegionList.get(i);
			if (rg.isHit(start, end) && index != i) {
				// 現在の選択よりも前にあったら
				if (minHit == -1 && i < index)
					minHit = i;
				// 現在の選択よりも後にあったら処理を終了
				if (index < i) {
					index = i;
					return;
				}
			}
		}
		// 現在の選択が最後なら
		index = minHit;
	}

	public static void edit() {
		if (!EditMode || index == -1)
			return;
		System.out.println("EDIT");
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player == null)
			return;
		Vec3d start = player.getPositionVector().addVector(0, player.eyeHeight, 0);
		Vec3d end = start.add(player.getLook(1.0F).scale(40));
		RegionManager rm = RegionManager.getManager();
		// 選択したレギオンを見ているなら
		if (rm.RegionList.get(index).isHit(start, end))
			Minecraft.getMinecraft().displayGuiScreen(new RegionEditGUI((byte) index));
	}
}
