package hide.region;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.util.Strings;

import com.google.gson.annotations.SerializedName;

import hide.faction.FactionRank;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class RegionRule implements IMessage {
	/** 大きいほうが優先 */
	@SerializedName("Priority")
	public int priority = 0;

	@SerializedName("TargerTeam")
	public String targetName = "";

	/** この階級以上に適応 */
	@SerializedName("TargerRank")
	public FactionRank targetRank = FactionRank.Temporary;

	@SerializedName("Permission")
	private Map<EnumRegionPermission, EnumPermissionState> _permission = new EnumMap(EnumRegionPermission.class);

	public Map<EnumRegionPermission, EnumPermissionState> getMap() {
		return _permission;
	}

	private static String empty = "";

	private boolean isTarget(EntityPlayer player) {
		return false;
	}

	public EnumPermissionState checkPermission(EnumRegionPermission regionPermission, EntityPlayer player) {
		ScorePlayerTeam team;
		if (Strings.isBlank(targetName) || (player.world != null
				&& ((team = player.world.getScoreboard().getPlayersTeam(player.getName())) == null
						|| team.getName().equals(targetName)))) {// TODO パーミッションの条件が必要
			return _permission.getOrDefault(regionPermission, EnumPermissionState.NONE);
		}
		return EnumPermissionState.NONE;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		priority = buf.readInt();
		targetName = ByteBufUtils.readUTF8String(buf);
		targetRank = FactionRank.values()[buf.readByte()];
		byte size = buf.readByte();
		_permission = new EnumMap<>(EnumRegionPermission.class);
		for (int i = 0; i < size; i++) {
			_permission.put(EnumRegionPermission.values()[buf.readByte()],
					EnumPermissionState.values()[buf.readByte()]);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(priority);
		ByteBufUtils.writeUTF8String(buf, targetName);
		buf.writeByte(targetRank.getIndex());
		buf.writeByte(_permission.size());
		for (Entry<EnumRegionPermission, EnumPermissionState> entry : _permission.entrySet()) {
			buf.writeByte(entry.getKey().getIndex());
			buf.writeByte(entry.getValue().getIndex());
		}
	}

}
