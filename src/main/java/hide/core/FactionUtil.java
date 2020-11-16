package hide.core;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FactionUtil {
	public static String getFaction(EntityPlayer player) {
		if (player == null || player.world == null)
			return "";
		ScorePlayerTeam team = player.world.getScoreboard().getPlayersTeam(player.getName());
		return team == null ? "" : team.getName();
	}

	@SideOnly(Side.CLIENT)
	public static String getFaction() {
		return getFaction(Minecraft.getMinecraft().player);
	}
}
