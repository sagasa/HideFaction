package hide.region;

import hide.core.HideFaction;
import hide.region.network.PacketRegionData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;

/**パーミッション系の同期等*/
public class PermissionManager {
	/**サーバー側からファクションデータを配信*/
	public static void provideRegionData(EntityPlayer player) {
		EntityPlayerMP playermp = (EntityPlayerMP) player;
		RegionManager rm = RegionManager.getManager(player.dimension);
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

	//========= キャンセル系 ===========

	//-- 左クリック --
	/** クライアントのアニメーションをキャンセル */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void leftClick(LeftClickBlock event) {
		if (event.getSide() == Side.CLIENT) {
			if (!RegionManager.getManager(event.getEntityPlayer().dimension).permission(event.getPos(), event.getEntityPlayer(),
					EnumRegionPermission.BlockDestroy)) {
				event.setCanceled(true);
			}
		}
	}

	/** サーバー側で破壊をキャンセル */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void leftBreak(BreakEvent event) {
		if (!RegionManager.getManager(event.getPlayer().dimension).permission(event.getPos(), event.getPlayer(),
				EnumRegionPermission.BlockDestroy)) {
			event.setCanceled(true);
		}
	}

	//-- 設置 --
	/** クライアントのアニメーションをキャンセル */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void place(PlaceEvent event) {
		if (!RegionManager.getManager(event.getPlayer().dimension).permission(event.getPos(), event.getPlayer(),
				EnumRegionPermission.BlockPlace)) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent()
	public void rightClick(RightClickBlock event) {
		System.out.println();

	}

	@SubscribeEvent()
	public void rightClick(PlayerContainerEvent.Open event) {
		System.out.println(event);
		if(event.getContainer() instanceof ContainerChest)
			System.out.println(((ContainerChest)event.getContainer()));
	}
}