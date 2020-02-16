package hide.core;

import org.apache.logging.log4j.Logger;

import hide.core.gui.FactionGUIHandler;
import hide.core.network.PacketSimpleCmd;
import hide.faction.command.Faction;
import hide.region.EnumRegionPermission;
import hide.region.RegionCommand;
import hide.region.RegionManager;
import hide.region.network.PacketRegionData;
import hide.region.network.PacketRegionEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = HideFaction.MODID, name = HideFaction.NAME, version = HideFaction.VERSION)
public class HideFaction {
	public static final String MODID = "hidefaction";
	public static final String NAME = "HideFaction";
	public static final String VERSION = "1.0";

	@Mod.Instance(MODID)
	public static HideFaction INSTANCE;
	public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("HideFaction");

	private static Logger logger;

	public static Logger getLog() {
		return logger;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		// ネットワーク系
		/*
		 * IMesssageHandlerクラスとMessageクラスの登録。 第三引数：MessageクラスのMOD内での登録ID。256個登録できる
		 * 第四引数：送り先指定。クライアントかサーバーか、Side.CLIENT Side.SERVER
		 */
		NETWORK.registerMessage(PacketSimpleCmd.class, PacketSimpleCmd.class, 0, Side.SERVER);
		NETWORK.registerMessage(PacketSimpleCmd.class, PacketSimpleCmd.class, 1, Side.CLIENT);

		NETWORK.registerMessage(PacketRegionData.class, PacketRegionData.class, 3, Side.CLIENT);

		NETWORK.registerMessage(PacketRegionEdit.class, PacketRegionEdit.class, 4, Side.SERVER);
		NETWORK.registerMessage(PacketRegionEdit.class, PacketRegionEdit.class, 5, Side.CLIENT);
	}

	@EventHandler
	public void construct(FMLConstructionEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new FactionGUIHandler());
		// some example code
		logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
	}

	@EventHandler
	public void start(FMLServerStartingEvent event) {

		event.registerServerCommand(new Faction());

		event.registerServerCommand(new RegionCommand());
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent()
	public void onEvent(PlaySoundAtEntityEvent event) {
		if (event.getEntity() instanceof EntityPlayer) {

		}
	}

	/** クライアントのアニメーションをキャンセル */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void leftClick(LeftClickBlock event) {
		if (event.getSide() == Side.CLIENT) {
			if (!RegionManager.getManager(event.getWorld()).permission(event.getPos(), event.getEntityPlayer(),
					EnumRegionPermission.BlockDestroy)) {
				event.setCanceled(true);
			}
		}

	}

	/** サーバー側で破壊をキャンセル */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void leftClick(BreakEvent event) {
		if (!RegionManager.getManager(event.getWorld()).permission(event.getPos(), event.getPlayer(),
				EnumRegionPermission.BlockDestroy)) {
			event.setCanceled(true);
		}

	}

	@SubscribeEvent
	public void onEvent(PlayerLoggedInEvent event) {

	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent()
	public void onEvent(RenderGameOverlayEvent event) {
		if (event.isCancelable() && event.getType() == ElementType.PLAYER_LIST) {

		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent()
	public void onEvent(RenderWorldLastEvent event) {
		RegionManager.getManager(Minecraft.getMinecraft().world).RegionList
				.forEach(rg -> rg.drawRegionRect(event.getPartialTicks()));

		// GlStateManager.enableDepth();
	}
}
