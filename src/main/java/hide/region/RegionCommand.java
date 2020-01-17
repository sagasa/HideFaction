package hide.region;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;

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

	static RegionManager rm = new RegionManager();

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		System.out.println("exc" + Arrays.toString(args)+" "+new ChunkPos(sender.getPosition()));
		// ((EntityPlayer) sender.getCommandSenderEntity())
		// .addItemStackToInventory(new ItemStack(Block.getBlockById(7), 120));
		//sender.getEntityWorld().getScoreboard().getTeam(teamName);
		rm.getRegionList().clear();
		RegionRect rg = new RegionRect().setPos(new Vec3i(8, 60, 8), new Vec3i(-8, 65, -8));
		rg.setPriority(10);
		rm.getRegionList().add(rg);
		rg = new RegionRect().setPos(new Vec3i(8, 60, 8), new Vec3i(0, 65, 0));
		rm.getRegionList().add(rg);
		rm.registerRegionMap();

		rm.permission((EntityPlayer) sender.getCommandSenderEntity(), EnumRegionPermission.BlockDestroy);

		//System.out.println(sender.getEntityWorld().getScoreboard().getPlayersTeam(sender.getName()).getFriendlyFlags());
		System.out.println(sender.getEntityWorld().getSaveHandler().getWorldDirectory().getAbsolutePath());

	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		System.out.println(sender+" "+ArrayUtils.toString(args)+" "+targetPos);
		return super.getTabCompletions(server, sender, args, targetPos);
	}
}