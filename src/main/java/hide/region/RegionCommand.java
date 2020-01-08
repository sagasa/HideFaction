package hide.region;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
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
		System.out.println("exc" + Arrays.toString(args)+" "+new ChunkPos(sender.getPosition()));
		// ((EntityPlayer) sender.getCommandSenderEntity())
		// .addItemStackToInventory(new ItemStack(Block.getBlockById(7), 120));
		System.out.println(sender.getEntityWorld().getScoreboard().getPlayersTeam(sender.getName()));
		System.out.println(sender.getEntityWorld().getSaveHandler().getWorldDirectory().getAbsolutePath());

	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		System.out.println(sender+" "+ArrayUtils.toString(args)+" "+targetPos);
		return super.getTabCompletions(server, sender, args, targetPos);
	}
}