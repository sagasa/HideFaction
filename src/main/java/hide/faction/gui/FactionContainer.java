package hide.faction.gui;

import hide.faction.data.FactionData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class FactionContainer extends Container {
	int xCoord, yCoord, zCoord;

	FactionData factionData;
	/** アルミニウムチェストのインベントリの第一スロットの番号 */
	private static final int index0 = 0;
	/** プレイヤーのインベントリの第一スロットの番号 */
	private static final int index1 = 54;
	/** クイックスロットの第一スロットの番号 */
	private static final int index2 = 81;
	/** このコンテナの全体のスロット数 */
	private static final int index3 = 90;

	public FactionContainer(EntityPlayer player, FactionData data) {
		factionData = data;

		for (int iy = 0; iy < 6; iy++) {
			for (int ix = 0; ix < 9; ix++) {
				this.addSlotToContainer(new Slot(data.inventory, ix + (iy * 9), 8 + (ix * 18), 18 + (iy * 18)));

			}
		}
		for (int iy = 0; iy < 3; iy++) {
			for (int ix = 0; ix < 9; ix++) {
				this.addSlotToContainer(new Slot(player.inventory, ix + (iy * 9) + 9, 8 + (ix * 18), 140 + (iy * 18)));
			}
		}
		for (int ix = 0; ix < 9; ix++) {
			this.addSlotToContainer(new Slot(player.inventory, ix, 8 + (ix * 18), 198));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		System.out.println(playerIn + " " + index);
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index < this.index1) {
				if (!this.mergeItemStack(itemstack1, index1, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 0, index1, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		// TODO 自動生成されたメソッド・スタブ
		ItemStack res = super.slotClick(slotId, dragType, clickTypeIn, player);
		if (player instanceof EntityPlayerMP)
			System.out.println(slotId + " " + dragType + " " + clickTypeIn + " " + player + " " + res);

		return res;
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {

		return true;
	}
}
