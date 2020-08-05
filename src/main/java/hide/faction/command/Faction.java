package hide.faction.command;

import hide.core.HideFaction;
import hide.region.RegionManager;
import hide.region.network.PacketRegionData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

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
		return "commands.faction.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		// TODO 自動生成されたメソッド・スタブ
		if (args.length <= 0) {
			throw new WrongUsageException("commands.chat.usage", new Object[0]);
		} else {
			EntityPlayerMP player = args.length >= 2 ? getPlayer(server, sender, args[1]) : getCommandSenderAsPlayer(sender);
			if (args[0].equalsIgnoreCase("op")) {
				notifyCommandListener(sender, this, 1, "commands.faction.op.success", new Object[] { player.getName() });
				HideFaction.NETWORK.sendTo(PacketRegionData.addOP(player.getUniqueID()), player);
				RegionManager.OPPlayers.add(player.getUniqueID());
			} else if (args[0].equalsIgnoreCase("deop")) {
				notifyCommandListener(sender, this, 1, "commands.faction.deop.success", new Object[] { player.getName() });
				HideFaction.NETWORK.sendTo(PacketRegionData.removeOP(player.getUniqueID()), player);
				RegionManager.OPPlayers.remove(player.getUniqueID());
			} else if (args[0].equalsIgnoreCase("gui")) {
				player.openGui(HideFaction.INSTANCE, HideFaction.FACTION_GUI_ID,
						sender.getEntityWorld(), 0, 0, 0);
				// ((EntityPlayer) sender.getCommandSenderEntity())
				// .addItemStackToInventory(new ItemStack(Block.getBlockById(7), 120));
				/*
				System.out.println(sender.getEntityWorld().getScoreboard().getPlayersTeam(sender.getName()));
				System.out.println(sender.getEntityWorld().getSaveHandler().getWorldDirectory().getAbsolutePath());
				sender.getEntityWorld().getMapStorage().setData("hideFaction", new FactionWorldSave().test());
				//*/
			}
		}
	}
}