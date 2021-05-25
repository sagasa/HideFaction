package hide.event;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import hide.event.entity.EntityTarget;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.server.command.TextComponentHelper;

public class CommandTargetSpawn extends CommandBase {

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getName() {
		return "hidetarget";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.hidetarget.usage";
	}

	public static void sendCmdRes(ICommandSender sender, String msg, Object... obj) {
		sender.sendMessage(TextComponentHelper.createComponentTranslation(sender, msg, obj));
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 3) {
			throw new WrongUsageException("commands.hideequip.usage");
		} else {
			BlockPos blockpos = parseBlockPos(sender, args, 0, false);
			Vec3d vec3d = sender.getPositionVector();
			double x = (double) ((float) parseDouble(vec3d.x, args[0], true));
			double y = (double) ((float) parseDouble(vec3d.y, args[1], true));
			double z = (double) ((float) parseDouble(vec3d.z, args[2], true));

			World world = sender.getEntityWorld();
			System.out.println("world " + world + " " + x + " " + y + " " + z);
			world.spawnEntity(new EntityTarget(world, x, y, z, 1, 1, 1).setSize(1.5f));
			sendCmdRes(sender, "commands.hideequip.success");
		}

	}

	private static final List<String> operator = ImmutableList.of("start", "stop", "update", "end", "clear", "list", "reload");

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		return Collections.emptyList();
	}
}