package hide.region;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import hide.core.HideFaction;
import hide.faction.FactionSystem;
import hide.region.network.PacketRegionData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.server.command.TextComponentHelper;

public class RegionCommand extends CommandBase {

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getName() {
		return "region";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.region.usage";
	}

	public static void sendCmdRes(ICommandSender sender, String msg, Object... obj) {
		sender.sendMessage(TextComponentHelper.createComponentTranslation(sender, msg, obj));
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		// TODO 自動生成されたメソッド・スタブ
		if (args.length <= 0) {
			throw new WrongUsageException("commands.region.usage", new Object[0]);
		} else {
			if (args[0].equalsIgnoreCase("reload")) {
				RegionHolder.clearManager();
				sendCmdRes(sender, "commands.region.reload.success");
				return;
			}
			EntityPlayerMP player = args.length >= 2 ? getPlayer(server, sender, args[1]) : getCommandSenderAsPlayer(sender);
			if (args[0].equalsIgnoreCase("op")) {
				sendCmdRes(sender, "commands.region.op.success", player.getName());
				HideFaction.NETWORK.sendTo(PacketRegionData.addOP(player.getUniqueID()), player);
				RegionHolder.OPPlayers.add(player.getUniqueID());
			} else if (args[0].equalsIgnoreCase("deop")) {
				sendCmdRes(sender, "commands.region.deop.success", player.getName());
				HideFaction.NETWORK.sendTo(PacketRegionData.removeOP(player.getUniqueID()), player);
				RegionHolder.OPPlayers.remove(player.getUniqueID());
			} else if (args[0].equalsIgnoreCase("gui")) {
				player.openGui(HideFaction.INSTANCE, FactionSystem.FACTION_GUI_ID,
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

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		System.out.println(sender + " " + ArrayUtils.toString(args) + " " + targetPos);
		return super.getTabCompletions(server, sender, args, targetPos);
	}
}