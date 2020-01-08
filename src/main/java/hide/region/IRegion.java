package hide.region;

import java.util.List;
import java.util.Map;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;

public interface IRegion {
	/**レギオン内に含むならtrue*/
	public boolean contain(Vec3i vec);

	public void register(Map<ChunkPos, List<IRegion>> chunkMap);
}
