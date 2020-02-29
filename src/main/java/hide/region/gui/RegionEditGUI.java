package hide.region.gui;

import java.io.IOException;

import hide.core.HideFaction;
import hide.region.RegionManager;
import hide.region.RegionRect;
import hide.region.network.PacketRegionEdit;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;

public class RegionEditGUI extends GuiScreen {

	private RegionRect region;
	private byte index;

	public RegionEditGUI(byte index) {
		this.index = index;
		region = RegionManager.getManager().RegionList.get(index);

	}

	private static final int TARGET = 0xF;
	private static final int startX = 0x1;
	private static final int startY = 0x2;
	private static final int startZ = 0x3;
	private static final int endX = 0x4;
	private static final int endY = 0x5;
	private static final int endZ = 0x6;

	private static final int OPERATOR = 0xF0;
	private static final int add = 0x00;
	private static final int remove = 0x10;

	private static final int MAGNIFICATION = 0xF00;
	private static final int x1 = 0x000;
	private static final int x5 = 0x100;
	private static final int x10 = 0x200;

	private GuiTextField ruleName;

	@Override
	public void initGui() {
		super.initGui();
		addButton(startX, width / 2, 50);
		this.ruleName = new GuiTextField(0, this.fontRenderer, 50, 50, 80, this.fontRenderer.FONT_HEIGHT);
		this.ruleName.setFocused(true);
		this.ruleName.setText("");
		this.ruleName.setMaxStringLength(50);

	}

	private void addButton(int id, int x, int y) {
		buttonList.add(new GuiButton(id | add | x1, x - 30, y, 20, 20, "+1"));
		buttonList.add(new GuiButton(id | add | x5, x - 50, y, 20, 20, "+5"));
		buttonList.add(new GuiButton(id | add | x10, x - 70, y, 20, 20, "+10"));
		buttonList.add(new GuiButton(id | remove | x1, x + 30, y, 20, 20, "-1"));
		buttonList.add(new GuiButton(id | remove | x5, x + 50, y, 20, 20, "-5"));
		buttonList.add(new GuiButton(id | remove | x10, x + 70, y, 20, 20, "-10"));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		int id = button.id;
		int change = 0;
		if ((id & OPERATOR) == add)
			change = 1;
		else if ((id & OPERATOR) == remove)
			change = -1;

		if ((id & MAGNIFICATION) == x5)
			change *= 5;
		else if ((id & MAGNIFICATION) == x10)
			change *= 10;

		Vec3i start = region.getStartPos();
		Vec3i end = region.getEndPos();
		if ((id & TARGET) == startX)
			start = new Vec3i(start.getX() + change, start.getY(), start.getZ());

		region.setPos(start, end);
		HideFaction.NETWORK.sendToServer(PacketRegionEdit.editRegion(index, region));
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		ruleName.textboxKeyTyped(typedChar, keyCode);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, Integer.toString(region.getStartPos().getX()), width / 2, 50, 0xFFFFFF);
		ruleName.drawTextBox();
	}

	private static final ResourceLocation GUITEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

	private static final ResourceLocation TEXTURE = new ResourceLocation(HideFaction.MODID,
			"textures/gui/terminal.png");

}
