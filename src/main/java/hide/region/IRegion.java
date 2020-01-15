package hide.region;

import net.minecraft.util.math.Vec3i;

public interface IRegion {
	/**レギオン内に含むならtrue*/
	public boolean contain(Vec3i vec);

	public void register(ChunkRegingMap chunkMap);

	public int getPriority();

	public Boolean checkPermission(EnumRegionPermission regionPermission);
}
