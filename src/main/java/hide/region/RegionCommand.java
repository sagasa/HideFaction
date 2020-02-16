package hide.region;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import hide.core.HideFaction;
import hide.core.network.PacketSimpleCmd;
import hide.core.network.PacketSimpleCmd.Cmd;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

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
		return "";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		System.out.println("exc" + Arrays.toString(args) + " " + new ChunkPos(sender.getPosition()));
		// ((EntityPlayer) sender.getCommandSenderEntity())
		// .addItemStackToInventory(new ItemStack(Block.getBlockById(7), 120));
		// sender.getEntityWorld().getScoreboard().getTeam(teamName);

		// System.out.println(sender.getEntityWorld().getScoreboard().getPlayersTeam(sender.getName()).getFriendlyFlags());
		System.out.println(sender.getEntityWorld().getSaveHandler().getWorldDirectory().getAbsolutePath());
		if (sender.getCommandSenderEntity() instanceof EntityPlayer) {
			HideFaction.NETWORK.sendTo(new PacketSimpleCmd(Cmd.OpenRegionGUI), (EntityPlayerMP) sender.getCommandSenderEntity());
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		System.out.println(sender + " " + ArrayUtils.toString(args) + " " + targetPos);
		return super.getTabCompletions(server, sender, args, targetPos);
	}
}