package hide.core;

import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import hide.core.gui.FactionGUIHandler;
import hide.core.network.PacketSimpleCmd;
import hide.faction.command.Faction;
import hide.region.EnumRegionPermission;
import hide.region.RegionCommand;
import hide.region.RegionManager;
import hide.region.network.PacketRegionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
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
		//ネットワーク系
		/*
		 * IMesssageHandlerクラスとMessageクラスの登録。
		 * 第三引数：MessageクラスのMOD内での登録ID。256個登録できる
		 * 第四引数：送り先指定。クライアントかサーバーか、Side.CLIENT Side.SERVER
		 */
		NETWORK.registerMessage(PacketSimpleCmd.class, PacketSimpleCmd.class, 0, Side.SERVER);
		NETWORK.registerMessage(PacketSimpleCmd.class, PacketSimpleCmd.class, 1, Side.CLIENT);

		NETWORK.registerMessage(PacketRegionData.class, PacketRegionData.class, 2, Side.SERVER);
		NETWORK.registerMessage(PacketRegionData.class, PacketRegionData.class, 3, Side.CLIENT);
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

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void registerSound(LeftClickBlock event) {
		System.out.println(event.getSide() + " " + event.getPos() + " " + event.getPhase());
		if (event.getSide() == Side.SERVER) {

		}
		if (!RegionManager.getManager(event.getWorld()).permission(event.getPos(), event.getEntityPlayer(), EnumRegionPermission.BlockDestroy)) {
			event.setCanceled(true);
			System.out.println("No!!");
		}

	}


	@SubscribeEvent
	public void onEvent(PlayerLoggedInEvent event) {
		NETWORK.sendTo(PacketRegionData.regionList(RegionManager.getManager(event.player.world)), (EntityPlayerMP) event.player);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent()
	public void onEvent(RenderGameOverlayEvent event) {
		if (event.isCancelable() && event.getType() == ElementType.PLAYER_LIST) {

		}
	}

	private static double larp(double min, double max, float value) {
		return min + (max - min) * value;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent()
	public void onEvent(RenderWorldLastEvent event) {

		Minecraft mc = Minecraft.getMinecraft();
		double x = larp(mc.player.prevPosX, mc.player.posX, event.getPartialTicks());
		double y = larp(mc.player.prevPosY, mc.player.posY, event.getPartialTicks());
		double z = larp(mc.player.prevPosZ, mc.player.posZ, event.getPartialTicks());

		GlStateManager.translate(-x, -y, -z);

		GlStateManager.pushMatrix();

		GL11.glDisable(GL11.GL_DEPTH_TEST);

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		//GL11.glColor4ub(1,0, 0, 0.2F);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();

		RenderGlobal.drawBoundingBox(-1, -1, -1, 120, 300, 1, 0.8f, 1f, 0f, 0.2f);

		buf.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

		RenderGlobal.addChainedFilledBoxVertices(buf, -1, -1, -1, 120, 300, 1, 0.8f, 1f, 0f, 0.15f);
		tessellator.draw();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.enableCull();

		GL11.glEnable(GL11.GL_DEPTH_TEST);

		GlStateManager.popMatrix();

		//	GlStateManager.enableDepth();
	}
}
