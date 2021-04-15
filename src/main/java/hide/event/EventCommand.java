package hide.event;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.server.command.TextComponentHelper;

public class EventCommand extends CommandBase {

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getName() {
		return "hideevent";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.hideevent.usage";
	}

	public static void sendCmdRes(ICommandSender sender, String msg, Object... obj) {
		sender.sendMessage(TextComponentHelper.createComponentTranslation(sender, msg, obj));
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 2) {
			throw new WrongUsageException("commands.hideevent.usage");
		} else {
			if (args[0].equalsIgnoreCase("start")) {
				if (HideEventSystem.INSTANCE.eventStart(args[1]))
					sendCmdRes(sender, "commands.hideevent.start.success", args[1]);
				else
					throw new WrongUsageException("commands.hideevent.eventnotfound", args[1]);
			} else if (args[0].equalsIgnoreCase("stop")) {
				if (HideEventSystem.INSTANCE.eventStop(args[1]))
					sendCmdRes(sender, "commands.hideevent.stop.success", args[1]);
				else
					throw new WrongUsageException("commands.hideevent.eventnotfound", args[1]);
			} else if (args[0].equalsIgnoreCase("update")) {
				if (HideEventSystem.INSTANCE.eventUpdate(args[1]))
					sendCmdRes(sender, "commands.hideevent.update.success", args[1]);
				else
					throw new WrongUsageException("commands.hideevent.eventnotfound", args[1]);
			} else if (args[0].equalsIgnoreCase("end")) {
				if (HideEventSystem.INSTANCE.eventEnd(args[1]))
					sendCmdRes(sender, "commands.hideevent.end.success", args[1]);
				else
					throw new WrongUsageException("commands.hideevent.eventnotfound", args[1]);
			} else if (args[0].equalsIgnoreCase("clear")) {
				if (HideEventSystem.INSTANCE.eventClear(args[1]))
					sendCmdRes(sender, "commands.hideevent.clear.success", args[1]);
				else
					throw new WrongUsageException("commands.hideevent.eventnotfound", args[1]);
			} else {
				throw new WrongUsageException("commands.hideevent.operatornotfound", args[0]);
			}
		}
	}

	private static final List<String> operator = ImmutableList.of("start", "stop", "update", "end", "clear");

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, operator);
		} else if (args.length == 2) {
			return getListOfStringsMatchingLastWord(args, HideEventSystem.INSTANCE.map.keySet());
		}
		return Collections.emptyList();
	}
}