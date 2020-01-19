package hide.region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/** ワールド紐づけ ブロックからレギオンへの高速検索システム */
public class RegionManager {

	private World _world;

	private List<RegionRect> _regionList = new ArrayList<>();

	private ChunkRegingMap _regionMap = new ChunkRegingMap();

	private Map<EnumRegionPermission, EnumPermissionState> _defaultPermission = new EnumMap(EnumRegionPermission.class);

	/** リスト取得 */
	public List<RegionRect> getRegionList() {
		return _regionList;
	}

	/** レジストリに登録 */
	public void registerRegionMap() {
		_regionMap.clear();
		for (RegionRect iRegion : _regionList)
			iRegion.register(_regionMap);
		_regionMap.sort();
	}

	public Boolean permission(EntityPlayer player, EnumRegionPermission permission) {
		// player.world.getScoreboard().getTeam(player.getName()).getName();
		EnumPermissionState state = _defaultPermission.getOrDefault(permission, EnumPermissionState.ALLOW);
		List<RegionRect> list = _regionMap.getRegionList(player.getPosition());
		for (RegionRect rg : list) {
			state = state.returnIfNone(rg.checkPermission(permission, player));
		}
		System.out.println(state);
		return state == EnumPermissionState.ALLOW;
	}

	/** チャンク-レギオンリストのMap */
	public static class ChunkRegingMap {
		private Map<ChunkPos, List<RegionRect>> chunkMap = new HashMap<>();

		public void clear() {
			chunkMap.clear();
		}

		public void sort() {
			chunkMap.values().forEach(
					list -> list.sort((Comparator<RegionRect>) (r0, r1) -> r0.getPriority() - r1.getPriority()));
		}

		public void addToMap(ChunkPos pos, RegionRect rect) {
			if (!chunkMap.containsKey(pos))
				chunkMap.put(pos, new ArrayList());
			chunkMap.get(pos).add(rect);
		}

		public List<RegionRect> getRegionList(BlockPos pos) {
			ChunkPos cPos = new ChunkPos(pos);
			if (!chunkMap.containsKey(cPos))
				return Collections.EMPTY_LIST;
			return chunkMap.get(cPos).stream().filter(rg->rg.contain(pos)).collect(Collectors.toList());
		}
	}
}
