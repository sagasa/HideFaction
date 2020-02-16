package hide.region;

import hide.core.HideFaction;
import hide.region.network.PacketRegionData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

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

	public static void provideRegionData(WorldServer world) {
		world.playerEntities.forEach(PermissionManager::provideRegionData);
	}
}