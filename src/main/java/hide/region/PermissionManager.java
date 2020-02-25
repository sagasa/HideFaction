package hide.region;

import hide.core.HideFaction;
import hide.region.gui.RegionEditor;
import hide.region.network.PacketRegionData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;

/**パーミッション系の同期等*/
public class PermissionManager {
	/**サーバー側からファクションデータを配信*/
	public static void provideRegionData(EntityPlayer player) {
		EntityPlayerMP playermp = (EntityPlayerMP) player;
		RegionManager rm = RegionManager.getManager(player.dimension, Side.SERVER);
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
		world.playerEntities.forEach(PermissionManager::provideRegionData);
	}

	//========= GUI ===========
	@SubscribeEvent()
	public void guiEdit(MouseEvent event) {
		if (event.isButtonstate()) {
			if (event.getButton() == 0)
				RegionEditor.select();
			if (event.getButton() == 1)
				RegionEditor.edit();
		}
	}
	//========= キャンセル系 ===========

	//-- 左クリック --
	/** クライアントのアニメーションをキャンセル */
	@SubscribeEvent()
	public void leftClick(LeftClickBlock event) {
		if (event.getSide() == Side.CLIENT) {
			if (!RegionManager.getManager().permission(event.getPos(), event.getEntityPlayer(),
					EnumRegionPermission.BlockDestroy)) {
				event.setCanceled(true);
			}
		}
	}

	/** サーバー側で破壊をキャンセル */
	@SubscribeEvent()
	public void leftBreak(BreakEvent event) {
		if (!RegionManager.getManager(event.getPlayer().dimension, Side.SERVER).permission(event.getPos(), event.getPlayer(),
				EnumRegionPermission.BlockDestroy)) {
			event.setCanceled(true);
		}
	}

	//-- 設置 --

	@SubscribeEvent()
	public void place(PlaceEvent event) {
		if (!RegionManager.getManager(event.getPlayer().dimension, Side.SERVER).permission(event.getPos(), event.getPlayer(),
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
			RegionManager rm = RegionManager.getManager(event.getEntityPlayer().dimension, event.getSide());
			// チェスト
			if (block instanceof BlockChest && rm.permission(event.getPos(), event.getEntityPlayer(), EnumRegionPermission.ChestInteract)) {
				event.setCanceled(true);
			}
			// チェスト
			if (block instanceof BlockEnderChest && rm.permission(event.getPos(), event.getEntityPlayer(), EnumRegionPermission.EnderChestInteract)) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent()
	public void rightClick(PlayerContainerEvent.Open event) {

	}
}