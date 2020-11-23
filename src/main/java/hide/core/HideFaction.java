package hide.core;

import java.io.IOException;

import org.apache.logging.log4j.Logger;

import hide.capture.CaptureManager;
import hide.chat.CommandChat;
import hide.chat.HideChatManager;
import hide.chat.HideChatManager.ServerChatData;
import hide.chat.PacketChat;
import hide.chat.PacketChatState;
import hide.core.asm.HideCoreHook;
import hide.core.gui.FactionGUIHandler;
import hide.core.gui.FactionGUIHandler.HideGuiProvider;
import hide.core.gui.GuiHideChat;
import hide.core.gui.GuiHideNewChat;
import hide.core.network.PacketSimpleCmd;
import hide.faction.CommandFaction;
import hide.faction.data.FactionData;
import hide.faction.gui.FactionContainer;
import hide.faction.gui.FactionGuiContainer;
import hide.region.ItemRegionEdit;
import hide.region.PermissionManager;
import hide.region.RegionCommand;
import hide.region.gui.RegionEditor;
import hide.region.network.PacketRegionData;
import hide.region.network.PacketRegionEdit;
import hide.schedule.ScheduleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	public void preInit(FMLPreInitializationEvent event) {
		log = event.getModLog();
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

		NETWORK.registerMessage(PacketChat.class, PacketChat.class, 6, Side.SERVER);
		NETWORK.registerMessage(PacketChat.class, PacketChat.class, 7, Side.CLIENT);

		NETWORK.registerMessage(PacketChatState.class, PacketChatState.class, 8, Side.SERVER);
		NETWORK.registerMessage(PacketChatState.class, PacketChatState.class, 9, Side.CLIENT);

		HidePlayerDataManager.register(ServerChatData.class, Side.SERVER);
		if (event.getSide() == Side.CLIENT) {
			Minecraft.getMinecraft().getFramebuffer().enableStencil();
			HideCoreHook.GuiNewChat = GuiHideNewChat::new;

		}
	}

	@EventHandler
	public void construct(FMLConstructionEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new PermissionManager());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new FactionGUIHandler());
		ScheduleManager.load();
		// some example code
		log.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());

	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event) throws IOException {
		HideFactionDB.start();
		event.getServer().getCommandManager().executeCommand(event.getServer(), "/errCommand");
		//		ICommandSender icommandsender = CommandSenderWrapper.create(sender).withEntity(entity, new Vec3d(d0, d1, d2)).withSendCommandFeedback(server.worlds[0].getGameRules().getBoolean("commandBlockOutput"));

		event.registerServerCommand(new CommandFaction());
		event.registerServerCommand(new RegionCommand());
		event.registerServerCommand(new CommandChat());
		ScheduleManager.start(event.getServer(), 1603837200000l);
		CaptureManager cm = new CaptureManager(event.getServer());
	}

	@EventHandler
	public void serverStop(FMLServerStoppedEvent event) {
		HideFactionDB.end();
	}

	@SubscribeEvent()
	public void serverTick(ServerTickEvent event) {
		if (event.phase == Phase.END) {
			ScheduleManager.update();
		}
	}

	@SubscribeEvent()
	public void onEvent(ServerChatEvent event) {
		HideChatManager.onChat(event);
	}

	@SubscribeEvent()
	public void onEvent(PlayerLoggedInEvent event) {
		HideChatManager.onLogin((EntityPlayerMP) event.player);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent()
	public void onEvent(PlaySoundAtEntityEvent event) {
		if (event.getEntity() instanceof EntityPlayer) {

		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent()
	public void onEvent(GuiOpenEvent event) {
		//System.out.println(event.getGui());
		if (event.getGui() instanceof GuiChat) {
			event.setGui(new GuiHideChat(((GuiChat) event.getGui()).defaultInputFieldText));
		}
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
		//RegionManager.getManager(Minecraft.getMinecraft().world).RegionList
		//		.forEach(rg -> rg.drawRegionRect(true,event.getPartialTicks(),0.8f,1f,0));
		RegionEditor.draw(event.getPartialTicks());
		// GlStateManager.enableDepth();
	}

	@ObjectHolder(MODID)
	public static class ITEMS {
		public static final Item edit_region = null;
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(new ItemRegionEdit());
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(ITEMS.edit_region, 0, new ModelResourceLocation(ITEMS.edit_region.getRegistryName(), "inventory"));
	}

	static FactionData data = new FactionData();
	public static int FACTION_GUI_ID = FactionGUIHandler.register(new HideGuiProvider() {

		@Override
		public Object getServerGuiElement(EntityPlayer player, World world, int x, int y, int z) {
			return new FactionContainer(player, data);
		}

		@Override
		public Object getClientGuiElement(EntityPlayer player, World world, int x, int y, int z) {
			return new FactionGuiContainer(player, data);
		}
	});
}
