package hide.faction.command;

import java.util.Arrays;

import hide.core.HideFaction;
import hide.core.gui.FactionGUIHandler;
import hide.core.gui.FactionGUIHandler.HideGuiProvider;
import hide.faction.data.FactionData;
import hide.faction.data.FactionWorldSave;
import hide.faction.gui.FactionContainer;
import hide.faction.gui.FactionGuiContainer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class Faction extends CommandBase {

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getName() {
		return "faction";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "";
	}

	static FactionData data = new FactionData();

	private int GUI_ID = FactionGUIHandler.register(new HideGuiProvider() {

		@Override
		public Object getServerGuiElement(EntityPlayer player, World world, int x, int y, int z) {
			return new FactionContainer(player,data);
		}

		@Override
		public Object getClientGuiElement(EntityPlayer player, World world, int x, int y, int z) {
			return new FactionGuiContainer(player,data);
		}
	});

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		System.out.println("exc" + Arrays.toString(args));
		((EntityPlayer) sender.getCommandSenderEntity()).openGui(HideFaction.INSTANCE, GUI_ID,
				sender.getEntityWorld(), 0, 0, 0);
		// ((EntityPlayer) sender.getCommandSenderEntity())
		// .addItemStackToInventory(new ItemStack(Block.getBlockById(7), 120));
		System.out.println(sender.getEntityWorld().getScoreboard().getPlayersTeam(sender.getName()));
		System.out.println(sender.getEntityWorld().getSaveHandler().getWorldDirectory().getAbsolutePath());
		sender.getEntityWorld().getMapStorage().setData("hideFaction", new FactionWorldSave().test());
	}
}