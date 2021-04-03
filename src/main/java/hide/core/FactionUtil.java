package hide.core;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FactionUtil {

	@SideOnly(Side.CLIENT)
	public static ScorePlayerTeam getTeam() {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player == null || mc.player.world == null)
			return null;
		return mc.player.world.getScoreboard().getPlayersTeam(mc.player.getName());
	}

	private static ScorePlayerTeam getTeam(EntityPlayer player) {
		if (player == null || player.world == null)
			return null;
		return player.world.getScoreboard().getPlayersTeam(player.getName());
	}

	/** チームの登録名取得 */
	public static String getFaction(EntityPlayer player) {
		ScorePlayerTeam team = getTeam(player);
		return team == null ? "" : team.getName();
	}

	/** チームの表示名取得 */
	public static String getFactionDisplay(EntityPlayer player) {
		ScorePlayerTeam team = getTeam(player);
		return team == null ? "" : team.getDisplayName();
	}

	public static String getFactionDisplay(World world, String name) {
		if (world == null)
			return "";
		ScorePlayerTeam team = world.getScoreboard().getTeam(name);
		return team == null ? "" : team.getDisplayName();
	}

	public static EntityPlayer getPlayer(World world, String uuid) {
		if (world == null)
			return null;
		return world.getPlayerEntityByUUID(UUID.fromString(uuid));
	}

	public static String getPlayerDisplay(World world, String uuid) {
		EntityPlayer player = getPlayer(world, uuid);
		return player == null ? "" : player.getDisplayName().getFormattedText();
	}

	private static final String DefaultPlayer = "§7<%s>§r";

	public static String getPlayerDisplay(EntityPlayer player) {
		return player == null ? "" : String.format(DefaultPlayer, player.getName());
	}

	@SideOnly(Side.CLIENT)
	public static String getFaction() {
		return getFaction(Minecraft.getMinecraft().player);
	}
}
