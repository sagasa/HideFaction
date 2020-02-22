package hide.region;

import hide.core.HideFaction;
import hide.core.network.PacketSimpleCmd;
import hide.core.network.PacketSimpleCmd.Cmd;
import hide.region.network.PacketRegionData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onEvent(WorldEvent.Load event) {
		if(event.getWorld().isRemote)
			HideFaction.NETWORK.sendToServer(new PacketSimpleCmd(Cmd.RegionDataReq));
	}


	public static void provideRegionData(WorldServer world) {
		world.playerEntities.forEach(PermissionManager::provideRegionData);
	}
}