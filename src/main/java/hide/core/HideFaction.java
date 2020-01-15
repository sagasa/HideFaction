package hide.core;

import org.apache.logging.log4j.Logger;

import hide.faction.command.CommandTerritory;
import hide.faction.command.Faction;
import hide.faction.gui.FactionGUIHandler;
import hide.region.RegionCommand;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = HideFaction.MODID, name = HideFaction.NAME, version = HideFaction.VERSION)
public class HideFaction {
	public static final String MODID = "hidefaction";
	public static final String NAME = "HideFaction";
	public static final String VERSION = "1.0";

	@Mod.Instance("hidefaction")
	public static HideFaction INSTANCE;

	private static Logger logger;

	public static final int GUI_ID = 0;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
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
		event.registerServerCommand(new CommandTerritory());

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
}
