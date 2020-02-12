package hide.core;

import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import hide.core.gui.FactionGUIHandler;
import hide.core.network.PacketSimpleCmd;
import hide.faction.command.Faction;
import hide.region.RegionCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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



















	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void registerSound(LeftClickBlock event) {
		System.out.println(event.getSide() + " " + event.getPos() + " " + event.getPhase());
		if (event.getSide() == Side.SERVER) {
		}
		event.setCanceled(true);
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

		Minecraft mc = Minecraft.getMinecraft();
		double x = mc.player.posX - 0.5;
		double y = mc.player.posY + 0.1;
		double z = mc.player.posZ - 0.5;

		GlStateManager.disableTexture2D();
		//GL11.glDisable(GL11.GL_DEPTH_TEST);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GL11.glColor4f(1,0, 0, 0.2F);
		GL11.glPushMatrix();

		GlStateManager.translate(-x, -y, -z);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		buf.pos(0, 0, 0).endVertex();
		buf.pos(0, 100, 0).endVertex();
		buf.pos(100, 100, 0).endVertex();
		buf.pos(100, 0, 0).endVertex();
		tessellator.draw();

	    GL11.glPopMatrix();
	    GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.enableCull();
	//	GlStateManager.enableDepth();
	}
}
