package hide.region;

import hide.core.HideFaction;
import hide.core.HideSubSystem.IHideSubSystem;
import hide.region.gui.RegionEditor;
import hide.region.network.PacketRegionData;
import hide.region.network.PacketRegionEdit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**パーミッション系の同期等*/
public class RegionSystem implements IHideSubSystem{
	@Override
	public void init(Side side) {
		HideFaction.registerNetMsg(PacketRegionData.class, PacketRegionData.class, Side.CLIENT);

		HideFaction.registerNetMsg(PacketRegionEdit.class, PacketRegionEdit.class, Side.SERVER);
		HideFaction.registerNetMsg(PacketRegionEdit.class, PacketRegionEdit.class, Side.CLIENT);
	}

	@Override
	public void serverStart(FMLServerStartingEvent event) {
		event.registerServerCommand(new RegionCommand());
	}

	/**サーバー側からファクションデータを配信*/
	public static void provideRegionData(EntityPlayer player) {
		EntityPlayerMP playermp = (EntityPlayerMP) player;
		RegionHolder rm = RegionHolder.getManager(player.dimension, Side.SERVER);
		HideFaction.NETWORK.sendTo(PacketRegionData.defaultRule(rm), playermp);
		HideFaction.NETWORK.sendTo(PacketRegionData.ruleMap(rm), playermp);
		HideFaction.NETWORK.sendTo(PacketRegionData.regionList(rm), playermp);
	}

	//ログインとワールド移動で 鯖からレギオンデータを配信する
	@SubscribeEvent
	public void onEvent(PlayerLoggedInEvent event) {
		provideRegionData(event.player);
	}

	@SubscribeEvent
	public void onEvent(PlayerChangedDimensionEvent event) {
		provideRegionData(event.player);
	}

	public static void provideRegionData(WorldServer world) {
		world.playerEntities.forEach(RegionSystem::provideRegionData);
	}
	//========= アイテム ===========
	public static final Item edit_region = new ItemRegionEdit();

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(edit_region);
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(edit_region, 0, new ModelResourceLocation(edit_region.getRegistryName(), "inventory"));
	}


	//========= GUI ===========
	@SubscribeEvent()
	public void guiEdit(ClientTickEvent event) {
		if (event.phase == Phase.END) {
			RegionEditor.update();
		}
	}
	@SideOnly(Side.CLIENT)
	@SubscribeEvent()
	public void onEvent(RenderWorldLastEvent event) {
		//RegionManager.getManager(Minecraft.getMinecraft().world).RegionList
		//		.forEach(rg -> rg.drawRegionRect(true,event.getPartialTicks(),0.8f,1f,0));
		RegionEditor.draw(event.getPartialTicks());
		// GlStateManager.enableDepth();
	}
	//========= キャンセル系 ===========

	//-- 左クリック --
	/** クライアントのアニメーションをキャンセル */
	@SubscribeEvent()
	public void leftClick(LeftClickBlock event) {
		if (event.getSide() == Side.CLIENT) {
			if (!RegionHolder.getManager().permission(event.getPos(), event.getEntityPlayer(),
					EnumRegionPermission.BlockDestroy)) {
				event.setCanceled(true);
			}
		}
	}

	/** サーバー側で破壊をキャンセル */
	@SubscribeEvent()
	public void leftBreak(BreakEvent event) {
		if (!RegionHolder.getManager(event.getPlayer().dimension, Side.SERVER).permission(event.getPos(), event.getPlayer(),
				EnumRegionPermission.BlockDestroy)) {
			event.setCanceled(true);
		}
		//編集アイテム
		if (event.getPlayer().getHeldItemMainhand().getItem() == edit_region)
			event.setCanceled(true);
	}

	//-- 設置 --

	@SubscribeEvent()
	public void place(PlaceEvent event) {
		if (!RegionHolder.getManager(event.getPlayer().dimension, Side.SERVER).permission(event.getPos(), event.getPlayer(),
				EnumRegionPermission.BlockPlace)) {
			event.setCanceled(true);
		}
	}

	//- インタラクト --
	@SubscribeEvent()
	public void rightClick(RightClickBlock event) {
		//ブロックインタラクト
		if (!(event.getItemStack().getItem() instanceof ItemBlock && event.getEntityPlayer().isSneaking())) {
			Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
			RegionHolder rm = RegionHolder.getManager(event.getEntityPlayer().dimension, event.getSide());
			// チェスト
			if (block instanceof BlockChest && !rm.permission(event.getPos(), event.getEntityPlayer(), EnumRegionPermission.ChestInteract)) {
				event.setCanceled(true);
			}
			// チェスト
			if (block instanceof BlockEnderChest && !rm.permission(event.getPos(), event.getEntityPlayer(), EnumRegionPermission.EnderChestInteract)) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent()
	public void rightClick(PlayerContainerEvent.Open event) {

	}


}