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
	/**大きいほうが優先*/
	@SerializedName("Priority")
	private int _priority = 0;

	@SerializedName("TargerTeam")
	private String _targetName = "";

	/**この階級以上に適応*/
	@SerializedName("TargerRank")
	private FactionRank _targetRank = FactionRank.Temporary;

	@SerializedName("Permission")
	private Map<EnumRegionPermission, EnumPermissionState> _permission = new EnumMap(EnumRegionPermission.class);

	public void setPriority(int value) {
		_priority = value;
	}

	public int getPriority() {
		return _priority;
	}

	public Map<EnumRegionPermission, EnumPermissionState> getMap() {
		return _permission;
	}

	private static String empty = "";

	private boolean isTarget(EntityPlayer player) {
		return false;
	}

	public EnumPermissionState checkPermission(EnumRegionPermission regionPermission, EntityPlayer player) {
		ScorePlayerTeam team;
		if (Strings.isBlank(_targetName) ||
				(player.world != null &&
						((team = player.world.getScoreboard().getPlayersTeam(player.getName())) == null ||
								team.getName().equals(_targetName)))) {//TODO パーミッションの条件が必要
			return _permission.getOrDefault(regionPermission, EnumPermissionState.NONE);
		}
		return EnumPermissionState.NONE;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		_priority = buf.readInt();
		_targetName = ByteBufUtils.readUTF8String(buf);
		_targetRank = FactionRank.values()[buf.readByte()];
		byte size = buf.readByte();
		_permission = new EnumMap<>(EnumRegionPermission.class);
		for (int i = 0; i < size; i++) {
			_permission.put(EnumRegionPermission.values()[buf.readByte()], EnumPermissionState.values()[buf.readByte()]);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(_priority);
		ByteBufUtils.writeUTF8String(buf, _targetName);
		buf.writeByte(_targetRank.getIndex());
		buf.writeByte(_permission.size());
		for (Entry<EnumRegionPermission, EnumPermissionState> entry : _permission.entrySet()) {
			buf.writeByte(entry.getKey().getIndex());
			buf.writeByte(entry.getValue().getIndex());
		}
	}

}
