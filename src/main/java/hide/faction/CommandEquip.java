package hide.faction;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.server.command.TextComponentHelper;

public class CommandEquip extends CommandBase {

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getName() {
		return "hideequip";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.hideequip.usage";
	}

	public static void sendCmdRes(ICommandSender sender, String msg, Object... obj) {
		sender.sendMessage(TextComponentHelper.createComponentTranslation(sender, msg, obj));
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 3) {
			throw new WrongUsageException("commands.hideequip.usage");
		} else {
			EntityPlayerMP player;
			BlockPos blockpos;
			if (4 <= args.length) {
				player = getPlayer(server, sender, args[0]);
				blockpos = parseBlockPos(sender, args, 1, false);
			} else {
				player = getCommandSenderAsPlayer(sender);
				blockpos = parseBlockPos(sender, args, 0, false);
			}
			if (player == null)
				throw new WrongUsageException("commands.region.usage");

			TileEntity e = player.world.getTileEntity(blockpos);
			if (e != null && e instanceof TileEntityLockableLoot) {
				TileEntityLockableLoot loot = (TileEntityLockableLoot) e;
				final int size = loot.getSizeInventory();
				//メインインベントリ
				for (int i = 0; i < size - 5; i++) {
					player.replaceItemInInventory(i, loot.getStackInSlot(i).copy());
				}
				//最後の５スロットをオフハンドと防具に
				if (4 < size) {
					for (int i = 0; i < 4; i++) {
						player.replaceItemInInventory(100 + i, loot.getStackInSlot(size - 1 - i).copy());
					}
					player.replaceItemInInventory(99, loot.getStackInSlot(size - 5).copy());
				}
				sendCmdRes(sender, "commands.hideequip.success");
			} else {
				sendCmdRes(sender, "commands.hideequip.nocontainer");
			}
		}

	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
		} else {
			return getTabCompletionCoordinate(args, 1, targetPos);
		}
	}

	public boolean isUsernameIndex(String[] args, int index) {
		return 4 <= args.length && index == 0;
	}
}
