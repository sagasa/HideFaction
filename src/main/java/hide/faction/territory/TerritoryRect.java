package hide.faction.territory;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

/** 領土管理用最小単位 */
public class TerritoryRect {
	/** 常にx,y,zがendPos以上でなくてはならない */
	public Vec3i startPos;
	/** 常にx,y,zがstartPos以下でなくてはならない */
	public Vec3i endPos;

	public boolean contain(BlockPos pos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if (x > startPos.getX() || endPos.getX() > x)
			return false;
		if (y > startPos.getY() || endPos.getY() > y)
			return false;
		if (z > startPos.getZ() || endPos.getZ() > z)
			return false;
		return true;
	}
}
