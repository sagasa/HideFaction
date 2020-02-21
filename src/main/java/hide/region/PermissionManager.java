package hide.region;

import hide.core.HideFaction;
import hide.region.network.PacketRegionData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

/**パーミッション系の同期等*/
public class PermissionManager {
	/**サーバー側からファクションデータを配信*/
	public static void provideRegionData(EntityPlayer player) {
		EntityPlayerMP playermp = (EntityPlayerMP) player;
		RegionManager rm = RegionManager.getManager(player.world);
		HideFaction.NETWORK.sendTo(PacketRegionData.defaultRule(rm), playermp);
		HideFaction.NETWORK.sendTo(PacketRegionData.ruleMap(rm), playermp);
		HideFaction.NETWORK.sendTo(PacketRegionData.regionList(rm), playermp);
	}


	@SubscribeEvent
	public void onEvent(PlayerLoggedInEvent event) {
		//鯖からレギオンデータを配信する
		System.out.println("Login");

	}

	@SubscribeEvent
	public void onLoadWorld(WorldEvent.Load event) {

	}


	public static void provideRegionData(WorldServer world) {
		world.playerEntities.forEach(PermissionManager::provideRegionData);
	}
}