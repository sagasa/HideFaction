package hide.resource;

import hide.core.IHideSubSystem;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

public class HideLootSystem implements IHideSubSystem {

	@Override
	public void init(Side side) {


	}
	public static final Block spawner = new SpawnerBlock();

	@SubscribeEvent
	public void registerBlock(RegistryEvent.Register<Block> event) {
		event.getRegistry().registerAll(spawner);
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(new ItemBlock(spawner).setRegistryName(spawner.getRegistryName()));
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		//ModelLoader.setCustomModelResourceLocation(ITEMS.edit_region, 0, new ModelResourceLocation(ITEMS.edit_region.getRegistryName(), "inventory"));
	}

}
