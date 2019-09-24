package hide.faction.command;

import java.util.Arrays;

import hide.faction.HideFaction;
import hide.faction.data.FactionSaveData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBed;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;

public class CommandTerritory extends CommandBase {

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getName() {
		return "territory";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		System.out.println("exc" + Arrays.toString(args));
		// ((EntityPlayer) sender.getCommandSenderEntity())
		// .addItemStackToInventory(new ItemStack(Block.getBlockById(7), 120));
		System.out.println(sender.getEntityWorld().getScoreboard().getPlayersTeam(sender.getName()));
	}
}
