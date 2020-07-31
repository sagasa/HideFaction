package hide.chat;

import hide.core.HideFaction;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandChat extends CommandBase {

	@Override
	public String getName() {
		return "chat";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		// TODO 自動生成されたメソッド・スタブ
		return "commands.chat.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		// TODO 自動生成されたメソッド・スタブ
		if (args.length <= 0) {
			throw new WrongUsageException("commands.chat.usage", new Object[0]);
		} else {
			EntityPlayerMP player = args.length >= 2 ? getPlayer(server, sender, args[1]) : getCommandSenderAsPlayer(sender);
			if (args[0].equalsIgnoreCase("reload")) {
				notifyCommandListener(sender, this, 1, "commands.chat.reload.success", new Object[] { player.getName() });
				HideFaction.NETWORK.sendTo(PacketChat.clearChat(), player);
			}
		}
	}

	public int getRequiredPermissionLevel()
    {
        return 2;
    }
}
