package hide.region;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

/** ワールド紐づけ ブロックからレギオンへの高速検索システム サーバー側のみ */
public class RegionManager {

	/** セッション毎の保護無視プレイヤーリスト サーバーサイドでのみ機能 */
	public static final Set<EntityPlayer> OPPlayers = new HashSet<>();

	public List<RegionRect> RegionList = new ArrayList<>();

	private RuleChunkRegingMap _ruleRegionMap = new RuleChunkRegingMap();
	private ChunkRegingMap _areaRegionMap = new ChunkRegingMap();

	public Map<EnumRegionPermission, EnumPermissionState> DefaultPermission = new EnumMap(EnumRegionPermission.class);

	public RegionManager() {// TODO テストコード

		RegionRule rule = new RegionRule();
		rule.getMap().put(EnumRegionPermission.BlockDestroy, EnumPermissionState.DENY);
		RuleMap.put("test", rule);

		RegionRect region = new RegionRect();
		region.setPos(new Vec3i(-8, 20, -8), new Vec3i(8, 30, 8));
		region.setRuleName("test");
		RegionList.add(region);
		registerRegionMap();
	}

	/** レジストリに登録 */
	public void registerRegionMap() {
		RegionList.forEach(rg -> rg.checkValue());
		_ruleRegionMap.clear();
		_areaRegionMap.clear();
		for (RegionRect region : RegionList) {
			System.out.println("登録 " + region);
			if (region.haveRule())
				region.register(_ruleRegionMap);
			else
				region.register(_areaRegionMap);
		}
		_ruleRegionMap.sort();
	}

	/** レギオンを検索して許可されるか返す */
	public boolean permission(BlockPos pos, EntityPlayer player, EnumRegionPermission permission) {
		if (permission.ArrowFromOP && OPPlayers.contains(player))
			return true;
		// player.world.getScoreboard().getTeam(player.getName()).getName();
		boolean state = _ruleRegionMap.permission(pos, player, permission);
		// System.out.println(RegionList + " " + _ruleRegionMap.getRegion(pos, null));
		return state;
	}

	/** チャンク-レギオンリストのMap */
	public class ChunkRegingMap {
		protected Map<ChunkPos, List<RegionRect>> chunkMap = new HashMap<>();

		public void clear() {
			chunkMap.clear();
		}

		public void addToMap(ChunkPos pos, RegionRect rect) {
			if (!chunkMap.containsKey(pos))
				chunkMap.put(pos, new ArrayList());
			chunkMap.get(pos).add(rect);
		}

		public RegionRect getRegion(BlockPos pos, String tag) {
			ChunkPos cPos = new ChunkPos(pos);
			if (!chunkMap.containsKey(cPos))
				return null;
			return chunkMap.get(cPos).stream().filter(rg -> rg.contain(pos) && (tag == null || tag.equals(rg.getTag())))
					.findFirst().orElse(null);
		}
	}

	/** チャンク-レギオンリストのMap ルールがついてるレギオンのみ利用 */
	public class RuleChunkRegingMap extends ChunkRegingMap {
		// フィルタリング
		@Override
		public void addToMap(ChunkPos pos, RegionRect rect) {
			if (rect.haveRule())
				super.addToMap(pos, rect);
		}

		public void sort() {
			chunkMap.values().forEach(list -> list.sort(
					(Comparator<RegionRect>) (r0, r1) -> r0.getRule().priority - r1.getRule().priority));
		}

		/** レギオンを検索して許可されるか出力 */
		public boolean permission(BlockPos pos, EntityPlayer player, EnumRegionPermission permission) {
			// player.world.getScoreboard().getTeam(player.getName()).getName();
			EnumPermissionState state = DefaultPermission.getOrDefault(permission, EnumPermissionState.ALLOW);
			ChunkPos cPos = new ChunkPos(pos);
			if (chunkMap.containsKey(cPos))
				for (RegionRect rg : chunkMap.get(cPos).stream().filter(rg -> rg.contain(pos))
						.toArray(RegionRect[]::new)) {
					state = state.returnIfNone(rg.checkPermission(permission, player));
				}
			return state == EnumPermissionState.ALLOW;
		}
	}
	// ====== ルール ======

	public static Map<String, RegionRule> RuleMap = new HashMap<>();

	// ====== マネージャの格納系 ======
	private static Map<World, RegionManager> managerMap = new HashMap<>();

	public static RegionManager getManager(World world) {
		// Mapにあったらそれを返す
		RegionManager rm = managerMap.get(world);
		if (rm != null)
			return rm;

		System.out.println("MANAGET NOT FOUND " + world);
		// リモートならあきらめる
		if (world.isRemote) {
			rm = new RegionManager();
			managerMap.put(world, rm);
			return rm;
		}

		// 読み込みトライ
		rm = loadRegion(world);
		if (rm != null) {
			managerMap.put(world, rm);
			return rm;
		}
		// 作成してセーブ
		rm = new RegionManager();
		managerMap.put(world, rm);
		saveRegion(rm, world);
		return rm;
	}

	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static void saveRegion(RegionManager manager, World world) {
		// リモートならnull
		if (world.isRemote)
			return;
		try {
			File file = new File(world.getSaveHandler().getWorldDirectory(), "region.json");
			if (file != null) {
				FileWriter fileWriter = new FileWriter(file);
				gson.toJson(manager.RegionList, fileWriter);
				fileWriter.close();
			}
		} catch (Exception exception1) {
			exception1.printStackTrace();
		}
	}

	public static RegionManager loadRegion(World world) {
		// リモートならnull
		if (world.isRemote)
			return null;
		try {
			File file = new File(world.getSaveHandler().getWorldDirectory(), "region.json");
			if (file != null && file.exists()) {
				FileReader fileReader = new FileReader(file);
				RegionManager rm = new RegionManager();
				rm.RegionList = gson.fromJson(fileReader, new TypeToken<List<RegionRect>>() {
				}.getType());
				System.out.println(rm.RegionList + " " + file);
				fileReader.close();

				rm.registerRegionMap();
				return rm;
			}
		} catch (Exception exception1) {
			exception1.printStackTrace();
			// 読み込み失敗時に名称変更
			File file = new File(world.getSaveHandler().getWorldDirectory(), "region.json");
			if (file != null && file.exists())
				try {
					Files.copy(file, new File(world.getSaveHandler().getWorldDirectory(), "region.json.broken"));
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return null;
	}
}
