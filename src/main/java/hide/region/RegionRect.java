package hide.region;

import java.util.List;
import java.util.Map;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;

/**始点と終点を指定したレギオンの最小単位*/
public class RegionRect implements IRegion {

	private Vec3i _start;
	private Vec3i _end;

	/**start<endになるように調整*/
	protected void checkPos() {
		if (_end.getX() < _start.getX()) {
			_start = new Vec3i(_end.getX(), _start.getY(), _start.getZ());
			_end = new Vec3i(_start.getX(), _end.getY(), _end.getZ());
		}
		if (_end.getY() < _start.getY()) {
			_start = new Vec3i(_start.getX(), _end.getY(), _start.getZ());
			_end = new Vec3i(_end.getX(), _start.getY(), _end.getZ());
		}
		if (_end.getZ() < _start.getZ()) {
			_start = new Vec3i(_start.getX(), _start.getY(), _end.getZ());
			_end = new Vec3i(_end.getX(), _end.getY(), _start.getZ());
		}
	}

	@Override
	public boolean contain(Vec3i vec) {
		return _start.getX() < vec.getX() && vec.getX() < _end.getX() &&
				_start.getY() < vec.getY() && vec.getY() < _end.getY() &&
				_start.getZ() < vec.getZ() && vec.getZ() < _end.getZ();
	}

	@Override
	public void register(Map<ChunkPos, List<IRegion>> chunkMap) {
		int maxX = _end.getX() >> 4;
		int minX = _start.getX() >> 4;
		int maxZ = _end.getX() >> 4;
		int minZ = _start.getX() >> 4;
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				new ChunkPos(x,z);
			}
		}

	}

}
