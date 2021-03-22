package hide.region.gui;

import hide.core.HideFaction;
import hide.region.RegionHolder;
import hide.region.RegionRect;
import hide.region.RegionSystem;
import hide.region.network.PacketRegionEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RegionEditor {

	static {
		RegionHolder.addListener(RegionEditor::checkIndex);
	}

	private static boolean lastRight, lastLeft;

	public static void update() {
		Minecraft mc = Minecraft.getMinecraft();

		if (mc.player == null || Minecraft.getMinecraft().currentScreen != null || mc.player.getHeldItemMainhand().getItem() != RegionSystem.edit_region)
			return;

		boolean flag = Minecraft.getMinecraft().player.isSneaking();

		Vec3i vec = null;
		if (mc.objectMouseOver.typeOfHit == Type.BLOCK) {
			vec = mc.objectMouseOver.getBlockPos();
		}

		boolean right = GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem);
		boolean left = GameSettings.isKeyDown(mc.gameSettings.keyBindAttack);

		//編集モード
		if (EditMode) {
			if (right && !lastRight) {
				if (flag)
					//編集解除
					if (index == -1)
						EditMode = false;
					else
						setStartPos(vec);
				else
					RegionEditor.edit();
			}
			if (left && !lastLeft) {
				if (flag)
					setEndPos(vec);
				else
					RegionEditor.select();
			}
		} else if (flag && right && !lastRight && mc.player.isCreative()) {
			EditMode = true;
		}

		lastLeft = left;
		lastRight = right;
	}

	public static void checkIndex() {
		if (Minecraft.getMinecraft().player == null)
			return;
		if (RegionHolder.getManager().RegionList.size() < index + 1)
			index = -1;
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.currentScreen != null && mc.currentScreen instanceof RegionEditGUI) {
			if (index == -1)
				mc.displayGuiScreen(null);
			else
				mc.displayGuiScreen(new RegionEditGUI((byte) index));
		}
	}

	public static boolean EditMode = false;//TODO

	private static int index = -1;

	public static void draw(float partialTicks) {
		if (!EditMode)
			return;
		RegionHolder rm = RegionHolder.getManager();
		for (int i = 0; i < rm.RegionList.size(); i++) {
			// 選択中なら
			if (i == index)
				rm.RegionList.get(i).drawRegionRect(true, partialTicks, 0.8f, 1f, 0);
			else
				rm.RegionList.get(i).drawRegionRect(false, partialTicks, 0.5f, 1f, 0.5f);
		}
	}

	/** indexの値を変更する */
	private static void select() {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player == null)
			return;
		Vec3d start = player.getPositionVector().addVector(0, player.eyeHeight, 0);
		Vec3d end = start.add(player.getLook(1.0F).scale(40));
		RegionHolder rm = RegionHolder.getManager();
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

	private static void setStartPos(Vec3i pos) {
		if (index == -1 || pos == null)
			return;
		RegionRect rg = RegionHolder.getManager().RegionList.get(index);
		boolean needRegister = !isChunkEquals(rg.getStartPos(), pos);
		rg.setPos(pos, rg.getEndPos());

		HideFaction.NETWORK.sendToServer(PacketRegionEdit.editRegion((byte) index, rg));
		// チャンク単位の変更があれば
		if (needRegister)
			HideFaction.NETWORK.sendToServer(PacketRegionEdit.register());

	}

	private static void setEndPos(Vec3i pos) {
		if (index == -1 || pos == null)
			return;
		pos = new Vec3i(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
		RegionRect rg = RegionHolder.getManager().RegionList.get(index);
		boolean needRegister = !isChunkEquals(rg.getEndPos(), pos);
		rg.setPos(rg.getStartPos(), pos);

		HideFaction.NETWORK.sendToServer(PacketRegionEdit.editRegion((byte) index, rg));
		// チャンク単位の変更があれば
		if (needRegister)
			HideFaction.NETWORK.sendToServer(PacketRegionEdit.register());
	}

	private static boolean isChunkEquals(Vec3i pos0, Vec3i pos1) {
		return pos0.getX() >> 4 == pos1.getX() >> 4 && pos0.getZ() >> 4 == pos1.getZ() >> 4;
	}

	private static void edit() {
		if (index != -1) {
			System.out.println("EDIT");
			EntityPlayer player = Minecraft.getMinecraft().player;
			if (player == null)
				return;
			Vec3d start = player.getPositionVector().addVector(0, player.eyeHeight, 0);
			Vec3d end = start.add(player.getLook(1.0F).scale(40));
			RegionHolder rm = RegionHolder.getManager();
			// 選択したレギオンを見ているなら
			if (rm.RegionList.get(index).isHit(start, end)) {
				Minecraft.getMinecraft().displayGuiScreen(new RegionEditGUI((byte) index));
				return;
			}
		}
		Minecraft.getMinecraft().displayGuiScreen(new RegionAddGUI());
	}
}
