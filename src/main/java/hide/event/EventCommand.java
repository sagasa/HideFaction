package hide.event;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import hide.core.HideFaction;
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
		return "commands.region.usage";
	}

	public static void sendCmdRes(ICommandSender sender, String msg, Object... obj) {
		sender.sendMessage(TextComponentHelper.createComponentTranslation(sender, msg, obj));
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 2) {
			throw new WrongUsageException("commands.region.usage", new Object[0]);
		} else {
			if (args[0].equalsIgnoreCase("start")) {
				if (HideFaction.EventManager.eventStart(args[1]))
					;
				else
					;
				sendCmdRes(sender, "");
			} else if (args[0].equalsIgnoreCase("update")) {
				sendCmdRes(sender, "");
				if (HideFaction.EventManager.eventUpdate(args[1]))
					;
				else
					;
			} else if (args[0].equalsIgnoreCase("end")) {
				sendCmdRes(sender, "");
				if (HideFaction.EventManager.eventEnd(args[1]))
					;
				else
					;
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