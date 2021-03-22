package hide.faction;

import java.util.Arrays;

import hide.core.HideFaction;
import hide.core.network.PacketSimpleCmd;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.server.command.TextComponentHelper;

public class CommandFaction extends CommandBase {



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

	public static void sendCmdRes(ICommandSender sender, String msg, Object... obj) {
		sender.sendMessage(TextComponentHelper.createComponentTranslation(sender, msg, obj));
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
			HideFaction.NETWORK.sendTo(new PacketSimpleCmd(FactionSystem.OpenFactionGUI), (EntityPlayerMP) sender.getCommandSenderEntity());
		}
	}
}