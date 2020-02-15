package hide.region;

import org.apache.logging.log4j.util.Strings;

import com.google.gson.annotations.SerializedName;

import hide.core.HideFaction;
import hide.region.RegionManager.ChunkRegingMap;
import hide.util.HideByteBufUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**始点と終点を指定したレギオンの最小単位*/
public class RegionRect implements IMessage {

	@SerializedName("StartPos")
	private Vec3i _start = Vec3i.NULL_VECTOR;
	@SerializedName("EndPos")
	private Vec3i _end = Vec3i.NULL_VECTOR;

	@SerializedName("RuleName")
	private String _ruleName = Strings.EMPTY;

	private transient RegionRule _rule;

	@SerializedName("Tag")
	private String _tag = Strings.EMPTY;

	/**2点を設定 チェック掛けるから制約はなし*/
	public RegionRect setPos(Vec3i start, Vec3i end) {
		_start = start;
		_end = end;
		checkValue();
		return this;
	}

	public RegionRect setTag(String name) {
		_tag = name;
		return this;
	}

	public String getTag() {
		return _tag;
	}

	public RegionRect setRuleName(String name) {
		_ruleName = name;
		checkValue();
		//定義があっても代入されていなければ警告
		if (Strings.isNotBlank(_ruleName) && _rule == null)
			HideFaction.getLog().warn("rule [" + _ruleName + "] does not exist");
		return this;
	}

	public String getRuleName() {
		return _ruleName;
	}

	public RegionRule getRule() {
		return _rule;
	}

	public boolean haveRule() {
		return _rule != null;
	}

	/**start<endになるように調整+Ruleの実態を代入*/
	public void checkValue() {
		if (_end.getX() < _start.getX()) {
			Vec3i start = new Vec3i(_end.getX(), _start.getY(), _start.getZ());
			_end = new Vec3i(_start.getX(), _end.getY(), _end.getZ());
			_start = start;
		}
		if (_end.getY() < _start.getY()) {
			Vec3i start = new Vec3i(_start.getX(), _end.getY(), _start.getZ());
			_end = new Vec3i(_end.getX(), _start.getY(), _end.getZ());
			_start = start;
		}
		if (_end.getZ() < _start.getZ()) {
			Vec3i start = new Vec3i(_start.getX(), _start.getY(), _end.getZ());
			_end = new Vec3i(_end.getX(), _end.getY(), _start.getZ());
			_start = start;
		}
		//ルール名があったら探す
		if (Strings.isNotBlank(_ruleName))
			_rule = RegionManager.RuleMap.get(_ruleName);
	}

	public boolean contain(Vec3i vec) {
		return _start.getX() < vec.getX() && vec.getX() < _end.getX() &&
				_start.getY() < vec.getY() && vec.getY() < _end.getY() &&
				_start.getZ() < vec.getZ() && vec.getZ() < _end.getZ();
	}

	public void register(ChunkRegingMap chunkMap) {
		int maxX = _end.getX() >> 4;
		int minX = _start.getX() >> 4;
		int maxZ = _end.getX() >> 4;
		int minZ = _start.getX() >> 4;
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				chunkMap.addToMap(new ChunkPos(x, z), this);
			}
		}
	}

	public EnumPermissionState checkPermission(EnumRegionPermission regionPermission, EntityPlayer player) {
		return _rule == null ? EnumPermissionState.NONE : _rule.checkPermission(regionPermission, player);
	}

	@Override
	public String toString() {
		return "[from : " + _start + ", to : " + _end + ", tag : " + _tag + ", rule : " + _ruleName + "]";
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		_start = HideByteBufUtil.readVec3i(buf);
		_end = HideByteBufUtil.readVec3i(buf);
		_ruleName = ByteBufUtils.readUTF8String(buf);
		_tag = ByteBufUtils.readUTF8String(buf);
		checkValue();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		HideByteBufUtil.writeVec3i(buf, _start);
		HideByteBufUtil.writeVec3i(buf, _end);
		ByteBufUtils.writeUTF8String(buf, _ruleName);
		ByteBufUtils.writeUTF8String(buf, _tag);
	}
}
