package hide.region;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

import hide.region.RegionManager.ChunkRegingMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;

/**始点と終点を指定したレギオンの最小単位*/
public class RegionRect {

	@SerializedName("StartPos")
	private Vec3i _start;

	private Vec3i _end;

	/**大きいほうが優先*/
	private int _priority = 0;

	private String _target;

	private Map<EnumRegionPermission, EnumPermissionState> _permission = new HashMap<>();

	/**2点を設定 チェック掛けるから制約はなし*/
	public RegionRect setPos(Vec3i start, Vec3i end) {
		_start = start;
		_end = end;
		checkPos();
		return this;
	}

	/**start<endになるように調整*/
	protected void checkPos() {
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

	public void setPriority(int value) {
		_priority = value;
	}

	public int getPriority() {
		return _priority;
	}

	private static String empty = "";

	public EnumPermissionState checkPermission(EnumRegionPermission regionPermission, EntityPlayer player) {
		if (_target.length() != 0 && player.world != null && player.world.getScoreboard().getPlayersTeam(player.getName()) != null && player.world.getScoreboard().getPlayersTeam(player.getName()).getName().equals(_target))
			return _permission.getOrDefault(regionPermission, EnumPermissionState.NONE);
		return EnumPermissionState.NONE;
	}

	/*
	@SideOnly(Side.CLIENT)
	static class Deserializer implements JsonDeserializer<RegionRect>, JsonSerializer<RegionRect> {
		public RegionRect deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
			JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
			ResourceLocation resourcelocation = new ResourceLocation(JsonUtils.getString(jsonobject, "model"));
			Map<ResourceLocation, Float> map = this.makeMapResourceValues(jsonobject);
			return new RegionRect(resourcelocation, map);
		}

		protected Map<ResourceLocation, Float> makeMapResourceValues(JsonObject p_188025_1_) {
			Map<ResourceLocation, Float> map = Maps.<ResourceLocation, Float> newLinkedHashMap();
			JsonObject jsonobject = JsonUtils.getJsonObject(p_188025_1_, "predicate");

			for (Entry<String, JsonElement> entry : jsonobject.entrySet()) {
				map.put(new ResourceLocation(entry.getKey()), Float.valueOf(JsonUtils.getFloat(entry.getValue(), entry.getKey())));
			}

			return map;
		}
	}//*/
}
