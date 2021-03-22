package hide.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import hide.chat.HideChatSystem;
import hide.core.gui.FactionGUIHandler;
import hide.core.network.DataSync;
import hide.core.network.DataSync.SyncMsg;
import hide.core.network.PacketSimpleCmd;
import hide.event.HideEventSystem;
import hide.faction.FactionSystem;
import hide.region.RegionSystem;
import hide.resource.HideLootSystem;
import hide.schedule.ScheduleManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = HideFaction.MODID, name = HideFaction.NAME)
public class HideFaction {
	public static final String MODID = "hidefaction";
	public static final String NAME = "HideFaction";

	@Mod.Instance(MODID)
	public static HideFaction INSTANCE;
	public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("HideFaction");

	public static Logger log;

	public static Logger getLog() {
		return log;
	}

	@EventHandler
	public void construct(FMLConstructionEvent event) {
		//サブシステム登録
		registerSubSystem(event, new HideChatSystem());
		registerSubSystem(event, new RegionSystem());
		registerSubSystem(event, new FactionSystem());
		registerSubSystem(event, new HideLootSystem());
		registerSubSystem(event, HideEventSystem.INSTANCE);

		MinecraftForge.EVENT_BUS.register(this);
	}

	private List<IHideSubSystem> subSystems = new ArrayList<>();

	private void registerSubSystem(FMLConstructionEvent event, IHideSubSystem system) {
		MinecraftForge.EVENT_BUS.register(system);
		system.init(event.getSide());
		subSystems.add(system);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		log = event.getModLog();
		// ネットワーク系
		/*
		 * IMesssageHandlerクラスとMessageクラスの登録。 第三引数：MessageクラスのMOD内での登録ID。256個登録できる
		 * 第四引数：送り先指定。クライアントかサーバーか、Side.CLIENT Side.SERVER
		 */
		registerNetMsg(PacketSimpleCmd.class, PacketSimpleCmd.class, Side.SERVER);
		registerNetMsg(PacketSimpleCmd.class, PacketSimpleCmd.class, Side.CLIENT);

		registerNetMsg(DataSync.class, SyncMsg.class, Side.CLIENT);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new FactionGUIHandler());
		ScheduleManager.load();
		// some example code
		log.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		subSystems.forEach(sys -> sys.serverStart(event));
	}

	@EventHandler
	public void serverStop(FMLServerStoppedEvent event) {
		subSystems.forEach(sys -> sys.serverStop(event));
	}

	@SubscribeEvent()
	public void onEvent(PlayerLoggedInEvent event) {
		DataSync.update((EntityPlayerMP) event.player);
	}

	/**Mod内ネットID*/
	private static int netID;

	/**自動でIDを割り振る登録ラッパー*/
	public static <REQ extends IMessage, REPLY extends IMessage> void registerNetMsg(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side side) {
		NETWORK.registerMessage(messageHandler, requestMessageType, netID, side);
		netID++;
	}
}
