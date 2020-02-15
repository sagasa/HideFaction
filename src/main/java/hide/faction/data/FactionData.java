package hide.faction.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import hide.faction.FactionRank;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

public class FactionData implements INBTSerializable<NBTTagCompound> {

	public FactionInventory inventory = new FactionInventory("test");
	List<ItemStack> commonInventory = new ArrayList<>();
	List<ItemStack> leaderInventory = new ArrayList<>();
	Map<UUID, FactionRank> postMap = new HashMap<>();
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
		for (Entry<UUID, FactionRank> entry : postMap.entrySet()) {
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
			postMap.put(new UUID(tag.getLong(UUID_MOST), tag.getLong(UUID_LEAST)), FactionRank.valueOf(tag.getString(POST)));
		});
	}

	public static class FactionInventory extends InventoryBasic {

		public FactionInventory(String title) {
			super(title, false, 54);
		}

	}
}
