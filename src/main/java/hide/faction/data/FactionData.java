package hide.faction.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import hide.faction.gui.FactionGUIHandler;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.INBTSerializable;

public class FactionData implements INBTSerializable<NBTTagCompound> {

	public FactionInventory inventory = new FactionInventory();
	List<ItemStack> commonInventory = new ArrayList<>();
	List<ItemStack> leaderInventory = new ArrayList<>();
	Map<UUID, Post> postMap = new HashMap<>();
	int currency = 0;

	private static final String COMMON_INVENTORY = "commonInventory";
	private static final String LEADER_INVENTORY = "leaderInventory";
	private static final String POST_MAP = "postMap";
	private static final String UUID_MOST = "uuid_most";
	private static final String UUID_LEAST = "uuid_least";
	private static final String POST = "post";

	public FactionData() {

	}

	public FactionData(NBTTagCompound compoundTag) {
		deserializeNBT(compoundTag);
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound root = new NBTTagCompound();
		NBTTagList commonInv = new NBTTagList();
		for (ItemStack stack : commonInventory)
			commonInv.appendTag(stack.serializeNBT());
		root.setTag(COMMON_INVENTORY, commonInv);
		NBTTagList leaderInv = new NBTTagList();
		for (ItemStack stack : leaderInventory)
			leaderInv.appendTag(stack.serializeNBT());
		root.setTag(LEADER_INVENTORY, leaderInv);
		NBTTagList posts = new NBTTagList();
		for (Entry<UUID, Post> entry : postMap.entrySet()) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setLong(UUID_MOST, entry.getKey().getMostSignificantBits());
			tag.setLong(UUID_LEAST, entry.getKey().getLeastSignificantBits());
			tag.setString(POST, entry.getValue().toString());
			posts.appendTag(tag);
		}
		root.setTag(POST_MAP, posts);
		return root;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		NBTTagList commonInv = nbt.getTagList(COMMON_INVENTORY, 10);
		commonInv.forEach(tag -> commonInventory.add(new ItemStack((NBTTagCompound) tag)));
		NBTTagList leaderInv = nbt.getTagList(LEADER_INVENTORY, 10);
		leaderInv.forEach(tag -> leaderInventory.add(new ItemStack((NBTTagCompound) tag)));
		NBTTagList posts = nbt.getTagList(POST_MAP, 10);
		posts.forEach(t -> {
			NBTTagCompound tag = (NBTTagCompound) t;
			postMap.put(new UUID(tag.getLong(UUID_MOST), tag.getLong(UUID_LEAST)), Post.valueOf(tag.getString(POST)));
		});
	}

	public static class FactionInventory implements IInventory {

		@Override
		public String getName() {
			// TODO 自動生成されたメソッド・スタブ
			return null;
		}

		@Override
		public boolean hasCustomName() {
			// TODO 自動生成されたメソッド・スタブ
			return false;
		}

		@Override
		public ITextComponent getDisplayName() {
			// TODO 自動生成されたメソッド・スタブ
			return null;
		}

		@Override
		public int getSizeInventory() {
			// TODO 自動生成されたメソッド・スタブ
			return 0;
		}

		@Override
		public boolean isEmpty() {
			// TODO 自動生成されたメソッド・スタブ
			return false;
		}

		@Override
		public ItemStack getStackInSlot(int index) {
			// TODO 自動生成されたメソッド・スタブ
			return null;
		}

		@Override
		public ItemStack decrStackSize(int index, int count) {
			// TODO 自動生成されたメソッド・スタブ
			return null;
		}

		@Override
		public ItemStack removeStackFromSlot(int index) {
			// TODO 自動生成されたメソッド・スタブ
			return null;
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack) {
			// TODO 自動生成されたメソッド・スタブ

		}

		@Override
		public int getInventoryStackLimit() {
			// TODO 自動生成されたメソッド・スタブ
			return 0;
		}

		@Override
		public void markDirty() {
			// TODO 自動生成されたメソッド・スタブ

		}

		@Override
		public boolean isUsableByPlayer(EntityPlayer player) {
			// TODO 自動生成されたメソッド・スタブ
			return false;
		}

		@Override
		public void openInventory(EntityPlayer player) {
			// TODO 自動生成されたメソッド・スタブ

		}

		@Override
		public void closeInventory(EntityPlayer player) {
			// TODO 自動生成されたメソッド・スタブ

		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
			// TODO 自動生成されたメソッド・スタブ
			return false;
		}

		@Override
		public int getField(int id) {
			// TODO 自動生成されたメソッド・スタブ
			return 0;
		}

		@Override
		public void setField(int id, int value) {
			// TODO 自動生成されたメソッド・スタブ

		}

		@Override
		public int getFieldCount() {
			// TODO 自動生成されたメソッド・スタブ
			return 0;
		}

		@Override
		public void clear() {
			// TODO 自動生成されたメソッド・スタブ

		}

	}

	public enum Post {
		Leader, Officer, Member
	}
}
