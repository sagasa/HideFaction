package hide.capture;

import java.util.Arrays;

import hide.core.HideFaction;
import hide.faction.data.FactionSaveData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CaptureCommand extends CommandBase {

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
		return "";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		System.out.println("exc" + Arrays.toString(args));
		((EntityPlayer) sender.getCommandSenderEntity()).openGui(HideFaction.INSTANCE, HideFaction.GUI_ID,
				sender.getEntityWorld(), 0, 0, 0);
		// ((EntityPlayer) sender.getCommandSenderEntity())
		// .addItemStackToInventory(new ItemStack(Block.getBlockById(7), 120));
		System.out.println(sender.getEntityWorld().getScoreboard().getPlayersTeam(sender.getName()));
		System.out.println(sender.getEntityWorld().getSaveHandler().getWorldDirectory().getAbsolutePath());
		sender.getEntityWorld().getMapStorage().setData("hideFaction", new FactionSaveData().test());
	}
}