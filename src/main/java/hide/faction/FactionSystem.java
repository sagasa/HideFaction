package hide.faction;

import java.util.Map;
import java.util.UUID;

import hide.core.IHideSubSystem;
import hide.core.gui.FactionGUIHandler;
import hide.core.gui.FactionGUIHandler.HideGuiProvider;
import hide.core.network.PacketSimpleCmd;
import hide.core.network.PacketSimpleCmd.SimpleCmd;
import hide.faction.data.FactionData;
import hide.faction.gui.FactionContainer;
import hide.faction.gui.FactionGuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FactionSystem implements IHideSubSystem {
	public static SimpleCmd OpenFactionGUI;
	private Map<UUID, FactionRank> permissonLevel;

	@Override
	public void init(Side side) {
		if (side == Side.CLIENT)
			OpenFactionGUI = PacketSimpleCmd.register((player)->openGUI());
		else
			OpenFactionGUI = PacketSimpleCmd.register(null);

	}

	@SideOnly(Side.CLIENT)
	private void openGUI() {

	}

	@Override
	public void serverStart(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandFaction());
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
