package hide.region;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.util.math.ChunkPos;

public class ChunkRegingMap {
	private Map<ChunkPos, List<RegionRect>> chunkMap;

	public void clear() {
		chunkMap.clear();
	}

	public void addToMap(ChunkPos pos,RegionRect rect) {
		if(!chunkMap.containsKey(pos))
			chunkMap.put(pos, new ArrayList());
		chunkMap.get(pos).add(rect);
	}
}
