package hide.faction.territory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hide.faction.territory.permission.TerritoryPermissions;
import net.minecraft.util.math.BlockPos;

/** 領域確保のルール 同階層での重複の禁止 子の領域が親の領域から出てはならない */
public class Territory {
	public List<TerritoryRect> territoryRect = new ArrayList();

	public List<Territory> children = new ArrayList<>();

	public boolean contain(BlockPos pos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		for (TerritoryRect rect : territoryRect)
			if (rect.contain(pos))
				return true;
		return false;
	}

	public Territory getChild(BlockPos pos) {
		for (Territory territory : children)
			if (territory.contain(pos))
				return territory;
		return null;
	}

	public boolean getPermission(TerritoryPermissions type) {
		return true;
	}
}
