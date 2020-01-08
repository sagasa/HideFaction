package hide.faction.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import hide.core.HideFaction;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.WorldSavedData;

public class FactionSaveData extends WorldSavedData {

	public static String FACTION_SAVE = HideFaction.MODID + "_save";

	Map<String, FactionData> factionData = new HashMap<>();

	public FactionSaveData() {
		super(FACTION_SAVE);

	}

	public FactionSaveData test() {
		FactionData fData = new FactionData();
		fData.commonInventory.add(new ItemStack(Item.getItemById(7)));
		factionData.put("test", fData);
		FactionSaveData save = new FactionSaveData();
		save.readFromNBT(writeToNBT(new NBTTagCompound()));
		System.out.println(save.factionData.get("test").commonInventory.get(0));
		markDirty();
		return this;
	}

	private static final String FACTION_DATA = "factionData";

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		NBTTagCompound factionTag = nbt.getCompoundTag(FACTION_DATA);
		for (String key : factionTag.getKeySet())
			factionData.put(key, new FactionData(factionTag.getCompoundTag(key)));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound factionTag = new NBTTagCompound();
		for (Entry<String, FactionData> entry : factionData.entrySet())
			factionTag.setTag(entry.getKey(), entry.getValue().serializeNBT());
		compound.setTag(FACTION_DATA, factionTag);
		return compound;
	}

}
