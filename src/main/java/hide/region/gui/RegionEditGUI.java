package hide.region.gui;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import hide.core.HideFaction;
import hide.region.RegionManager;
import hide.region.RegionRect;
import hide.region.network.PacketRegionEdit;
import hide.util.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;

public class RegionEditGUI extends GuiScreen {

	private byte regionIndex;

	public RegionEditGUI(byte index) {
		this.regionIndex = index;
	}

	private RegionRect getRegion() {
		return RegionManager.getManager().RegionList.get(regionIndex);
	}

	private static final int deleteRegion = 0x1;
	private static final int addRegion = 0x2;

	private static final int removeTag = 0x3;
	private static final int addTag = 0x4;

	private static final int syncTag = 0x5;

	private static final int ruleEdit = 0x5;

	private static final int TARGET = 0xF00;
	private static final int startX = 0x100;
	private static final int startY = 0x200;
	private static final int startZ = 0x300;
	private static final int endX = 0x400;
	private static final int endY = 0x500;
	private static final int endZ = 0x600;

	private static final int OPERATOR = 0xF000;
	private static final int add = 0x0000;
	private static final int remove = 0x1000;

	private static final int MAGNIFICATION = 0xF0000;
	private static final int x1 = 0x00000;
	private static final int x5 = 0x10000;
	private static final int x10 = 0x20000;

	private GuiTextField ruleName;

	private static final int space = 20;

	private ListView tagList;
	private GuiTextField tagEdit;

	@Override
	public void initGui() {
		super.initGui();
		int w = width / 2 + 55, h = height / 2 - 95;

		addButton(startX, w, h);
		h += space;
		addButton(startY, w, h);
		h += space;
		addButton(startZ, w, h);
		h += space;

		addButton(endX, w, h);
		h += space;
		addButton(endY, w, h);
		h += space;
		addButton(endZ, w, h);
		h += space;

		buttonList.add(new GuiButton(deleteRegion, w - 20, h + 5, 120, 20, "Delete"));

		w = width / 2;
		h = height / 2;

		// タグ編集
		tagEdit = new GuiTextField(0, this.fontRenderer, w - 155, h - 95, 120, this.fontRenderer.FONT_HEIGHT + 2);
		tagEdit.setFocused(true);
		tagEdit.setEnabled(false);
		tagEdit.setText("");
		tagEdit.setMaxStringLength(50);

		// ルール編集
		ruleName = new GuiTextField(0, this.fontRenderer, w - 135, h + 36, 80, 18);
		ruleName.setFocused(true);
		ruleName.setText(getRegion().getRuleName());
		ruleName.setMaxStringLength(50);

		buttonList.add(new GuiButton(ruleEdit, w - 50, h + 35, 60, 20, "EditRule"));

		tagList = new ListView();
	}

	private void addButton(int id, int x, int y) {
		buttonList.add(new GuiButton(id | add | x1, x - 40, y, 20, 20, "+1"));
		buttonList.add(new GuiButton(id | add | x5, x - 60, y, 20, 20, "+5"));
		buttonList.add(new GuiButton(id | add | x10, x - 80, y, 20, 20, "+10"));
		buttonList.add(new GuiButton(id | remove | x1, x + 40, y, 20, 20, "-1"));
		buttonList.add(new GuiButton(id | remove | x5, x + 60, y, 20, 20, "-5"));
		buttonList.add(new GuiButton(id | remove | x10, x + 80, y, 20, 20, "-10"));
	}

	private static final ResourceLocation BG = new ResourceLocation(HideFaction.MODID, "textures/gui/regionedit.png");

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		int w, h;

		w = width / 2;
		h = height / 2;

		tagEdit.drawTextBox();
		ruleName.drawTextBox();
		tagList.drawScreen(mouseX, mouseY, partialTicks);

		RegionRect region = getRegion();

		final int white = 0xFFFFFF;
		w = width / 2 + 65;
		h = height / 2 - 95 + (space - fontRenderer.FONT_HEIGHT) / 2;

		drawStringBG("start X " + (region.getStartPos().getX()), w, h, white);
		h += space;
		drawStringBG("start Y " + (region.getStartPos().getY()), w, h, white);
		h += space;
		drawStringBG("start Z " + (region.getStartPos().getZ()), w, h, white);
		h += space;
		drawStringBG("size X " + (region.getEndPos().getX() - region.getStartPos().getX()), w, h, white);
		h += space;
		drawStringBG("size Y " + (region.getEndPos().getY() - region.getStartPos().getY()), w, h, white);
		h += space;
		drawStringBG("size Z " + (region.getEndPos().getZ() - region.getStartPos().getZ()), w, h, white);
		h += space;

		w = width / 2;
		h = height / 2;

		// ルールの状態表示
		DrawUtil.drawRect(w - 155, h + 35, w - 135, h + 55, 0.5f, 0.5f, 0.5f, 0.4f);
		if (region.haveRule())
			DrawUtil.drawRect(w - 153, h + 37, w - 137, h + 53, 0f, 1f, 0f, 0.6f);
		else
			DrawUtil.drawRect(w - 153, h + 37, w - 137, h + 53, 1f, 0f, 0f, 0.6f);
	}

	// 半透明の背景の上に描画
	private void drawStringBG(String text, int x, int y, int color) {
		int width = fontRenderer.getStringWidth(text);
		DrawUtil.drawRect(x - width / 2 - 2, y - 2, x + width / 2 + 2, y + fontRenderer.FONT_HEIGHT, 0.5f, 0.5f, 0.5f,
				0.4f);
		drawCenteredString(fontRenderer, text, x, y, color);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		RegionRect region = getRegion();

		try {
			super.actionPerformed(button);
		} catch (IOException e) {
			e.printStackTrace();
		}

		int id = button.id;

		if (id == ruleEdit) {
			tagList.clearSelect();
			tagEdit.setEnabled(false);
			ruleName.setEnabled(true);
		} else if (id == deleteRegion) {
			System.out.println("DELETE");

		} else {
			// 数値
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
			if ((id & TARGET) == startY)
				start = new Vec3i(start.getX(), start.getY() + change, start.getZ());
			if ((id & TARGET) == startZ)
				start = new Vec3i(start.getX(), start.getY(), start.getZ() + change);

			if ((id & TARGET) == endX)
				end = new Vec3i(end.getX() + change, end.getY(), end.getZ());
			if ((id & TARGET) == endY)
				end = new Vec3i(end.getX(), end.getY() + change, end.getZ());
			if ((id & TARGET) == endZ)
				end = new Vec3i(end.getX(), end.getY(), end.getZ() + change);

			boolean needRegister = !isChunkEquals(start, region.getStartPos())
					|| !isChunkEquals(end, region.getEndPos());
			region.setPos(start, end);
			HideFaction.NETWORK.sendToServer(PacketRegionEdit.editRegion(regionIndex, region));

			// チャンク単位の変更があれば
			if (needRegister)
				HideFaction.NETWORK.sendToServer(PacketRegionEdit.register());
		}
	}

	private static boolean isChunkEquals(Vec3i pos0, Vec3i pos1) {
		return pos0.getX() >> 4 == pos1.getX() >> 4 && pos0.getZ() >> 4 == pos1.getZ() >> 4;
	}

	@Override
	public void onResize(Minecraft mcIn, int w, int h) {
		super.onResize(mcIn, w, h);
		tagList.updateSize();

		w = this.width / 2;
		h = this.height / 2;
		tagEdit.x = w - 155;
		tagEdit.y = h - 95;
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		tagList.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		tagList.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		tagList.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		RegionRect region = getRegion();
		ruleName.textboxKeyTyped(typedChar, keyCode);
		// tag
		tagEdit.textboxKeyTyped(typedChar, keyCode);
		if (tagList.isSelectTag()) {
			region.getTag()[tagList.getIndex()] = tagEdit.getText();
			HideFaction.NETWORK.sendToServer(PacketRegionEdit.editRegion(regionIndex, region));
		} else {
			region.setRuleName(ruleName.getText());
			HideFaction.NETWORK.sendToServer(PacketRegionEdit.editRegion(regionIndex, region));
			HideFaction.NETWORK.sendToServer(PacketRegionEdit.register());
		}
	}

	private static final ResourceLocation GUITEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

	private static final ResourceLocation TEXTURE = new ResourceLocation(HideFaction.MODID,
			"textures/gui/terminal.png");

	private class ListView extends GuiListExtended {
		private ArrayList<ListCell> list = new ArrayList<>();

		public ListView() {
			super(RegionEditGUI.this.mc, 115, 100, 0, 100, 22);
			setHasListHeader(false, 0);
			visible = true;
			updateSize();
		}

		/** セルのモード更新 */
		public void updateState() {
			list.forEach(e -> e.updateState());
		}

		public int getIndex() {
			return selectedElement;
		}

		public void clearSelect() {
			selectedElement = -1;
		}

		public boolean isSelectTag() {
			return 0 <= selectedElement && selectedElement < getRegion().getTag().length;
		}

		/** サイズ更新 */
		public void updateSize() {
			int w = RegionEditGUI.this.width / 2, h = RegionEditGUI.this.height / 2;
			left = w - 155;
			top = h - 75;
			right = w - 35;
			bottom = h + 25;
		}

		@Override
		protected int getScrollBarX() {
			return right - 5;
		}

		@Override
		public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
			GL11.glEnable(GL11.GL_STENCIL_TEST);
			GlStateManager.clear(GL11.GL_STENCIL_BUFFER_BIT);
			// GL11.glStencilMask(0xFF);
			GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
			GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
			GL11.glStencilMask(0xFF);
			GlStateManager.colorMask(false, false, false, false);

			drawRect(left, top, right, bottom, -1);

			GlStateManager.colorMask(true, true, true, true);
			GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
			GL11.glStencilMask(0x00);
			super.drawScreen(mouseXIn, mouseYIn, partialTicks);

			GL11.glDisable(GL11.GL_STENCIL_TEST);
		}

		@Override
		protected boolean isSelected(int slotIndex) {
			return slotIndex == selectedElement;
		}

		@Override
		protected int getSize() {
			return getRegion().getTag().length + 1;
		}

		@Override
		public int getListWidth() {
			return 115;
		}

		@Override
		protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
		}

		@Override
		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
			boolean isTag = slotIndex < getRegion().getTag().length;
			ruleName.setEnabled(false);
			tagEdit.setEnabled(isTag);
			if (isTag) {
				tagEdit.setText(getRegion().getTag()[slotIndex]);
			} else {
				tagEdit.setText("");
			}
		}

		@Override
		public void handleMouseInput() {
			super.handleMouseInput();
			// 選択に柔軟性が足りなかったので修正
			if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && this.mouseY >= this.top
					&& this.mouseY <= this.bottom) {
				int i = left;
				int j = right;
				int k = this.mouseY - this.top - this.headerPadding + (int) this.amountScrolled - 4;
				int l = k / this.slotHeight;

				if (l < this.getSize() && this.mouseX >= i && this.mouseX <= j && l >= 0 && k >= 0) {
					this.elementClicked(l, false, this.mouseX, this.mouseY);
					this.selectedElement = l;
				} else if (this.mouseX >= i && this.mouseX <= j && k < 0) {
					this.clickedHeader(this.mouseX - i, this.mouseY - this.top + (int) this.amountScrolled - 4);
				}
			}
		}

		@Override
		public IGuiListEntry getListEntry(int index) {
			if (list.size() <= index)
				list.add(new ListCell(index));
			return list.get(index);
		}
	}

	private class ListCell implements IGuiListEntry {
		private int index;
		private boolean addButton = false;
		private GuiButton button = new GuiButton(1, 0, 0, "Add");

		public ListCell(int index) {
			this.index = index;
			updateState();
		}

		public void updateState() {
			addButton = !(index < getRegion().getTag().length);
			if (addButton) {
				button.displayString = "Add";
				button.id = addTag;
				button.width = 80;
			} else {
				button.displayString = "Remove";
				button.id = removeTag;
				button.width = 40;
			}
		}

		@Override
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {

		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
				boolean isSelected, float partialTicks) {
			// 見えないなら
			if (tagList.bottom < y || y + slotHeight < tagList.top)
				return;

			if (!addButton) {
				fontRenderer.drawString(getRegion().getTag()[index], x + 2,
						y + (slotHeight - fontRenderer.FONT_HEIGHT) / 2, -1);
				button.x = x + listWidth - button.width - 2;
			} else {
				button.x = x + 15;
			}
			button.y = y - 1;
			button.drawButton(mc, mouseX, mouseY, partialTicks);
		}

		@Override
		public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX,
				int relativeY) {
			if (mouseEvent == 0) {
				if (button.mousePressed(mc, mouseX, mouseY)) {
					button.playPressSound(mc.getSoundHandler());
					RegionRect region = getRegion();
					if (addButton) {
						region.setTag(ArrayUtils.add(region.getTag(), "new Tag"));
						tagList.updateState();
					} else {
						region.setTag(ArrayUtils.remove(region.getTag(), index));
						tagList.updateState();
					}
					HideFaction.NETWORK.sendToServer(PacketRegionEdit.editRegion(regionIndex, region));
					return true;
				}
			}
			return false;
		}

		@Override
		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			button.mouseReleased(x, y);
		}

	}
}
