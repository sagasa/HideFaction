package hide.region;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import hide.core.HideFaction;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** ワールド紐づけ ブロックからレギオンへの高速検索システム サーバー側のみ */
public class RegionHolder {

	/** セッション毎の保護無視プレイヤーリスト サーバーサイドでのみ機能 */
	public static final Set<UUID> OPPlayers = new HashSet<>();

	public List<RegionRect> RegionList = new ArrayList<>();

	private RuleChunkRegingMap _ruleRegionMap = new RuleChunkRegingMap();
	private ChunkRegingMap _tagRegionMap = new ChunkRegingMap();

	public EnumMap<EnumRegionPermission, EnumPermissionState> DefaultPermission = new EnumMap(EnumRegionPermission.class);

	public RegionHolder() {

	}

	public void setTestData() {
		RegionRule rule = new RegionRule();
		rule.getMap().put(EnumRegionPermission.BlockDestroy, EnumPermissionState.DENY);
		RuleMap.put("test", rule);

		RegionRect region = new RegionRect();
		region.setPos(new Vec3i(-8, 20, -8), new Vec3i(8, 30, 8));
		region.setRuleName("test");
		RegionList.add(region);
		region = new RegionRect();
		region.setPos(new Vec3i(-20, 20, -8), new Vec3i(-10, 30, 8));
		region.setRuleName("test");
		RegionList.add(region);
		System.out.println("NEW INSTANCE");
		registerRegionMap();

		for (EnumRegionPermission p : EnumRegionPermission.values()) {
			DefaultPermission.put(p, EnumPermissionState.NONE);
		}
	}

	/** レジストリに登録 */
	public void registerRegionMap() {
		System.out.println("registr " + RegionList);
		//通知
		listenerList.forEach(r -> r.run());
		RegionList.forEach(rg -> rg.checkValue());
		_ruleRegionMap.clear();
		_tagRegionMap.clear();
		for (RegionRect region : RegionList) {
			if (region.haveRule())
				region.register(_ruleRegionMap);
			if (region.haveTag())
				region.register(_tagRegionMap);
		}
		_ruleRegionMap.sort();
	}

	/** レギオンを検索して許可されるか返す */
	public boolean permission(BlockPos pos, EntityPlayer player, EnumRegionPermission permission) {
		if (permission.ArrowFromOP && OPPlayers.contains(player.getUniqueID())) {
			System.out.println("op skip " + OPPlayers + " " + permission);
			return true;
		}
		// player.world.getScoreboard().getTeam(player.getName()).getName();
		boolean state = _ruleRegionMap.permission(pos, player, permission);
		//System.out.println(state + " " + RegionList + " " + _ruleRegionMap.getRegionOr(pos));
		return state;
	}

	public Stream<RegionRect> getTagRegion(BlockPos pos) {
		return _tagRegionMap.getRegions(pos);
	}

	/**タグ全一致のみ*/
	public static Stream<RegionRect> andFilter(Stream<RegionRect> stream, String... tag) {
		return stream.filter(rg -> andSerch(tag, rg.getTag()));
	}

	/**タグのどれかが一致*/
	public static Stream<RegionRect> orFilter(Stream<RegionRect> stream, String... tag) {
		return stream.filter(rg -> orSerch(tag, rg.getTag()));
	}

	/** チャンク-レギオンリストのMap */
	protected class ChunkRegingMap {
		protected Map<ChunkPos, List<RegionRect>> chunkMap = new HashMap<>();

		void clear() {
			chunkMap.clear();
		}

		void addToMap(ChunkPos pos, RegionRect rect) {
			if (!chunkMap.containsKey(pos))
				chunkMap.put(pos, new ArrayList());
			chunkMap.get(pos).add(rect);
		}

		Stream<RegionRect> getRegions(BlockPos pos) {
			ChunkPos cPos = new ChunkPos(pos);
			if (!chunkMap.containsKey(cPos))
				return Collections.EMPTY_LIST.stream();
			return chunkMap.get(cPos).stream().filter(rg -> rg.contain(pos));
		}

		/**タグ全一致のみ*/
		RegionRect getRegionAnd(BlockPos pos, String... tag) {
			ChunkPos cPos = new ChunkPos(pos);
			if (!chunkMap.containsKey(cPos))
				return null;
			return chunkMap.get(cPos).stream().filter(rg -> rg.contain(pos) && (tag == null || andSerch(tag, rg.getTag())))
					.findFirst().orElse(null);
		}

		/**タグのどれかが一致*/
		RegionRect getRegionOr(BlockPos pos, String... tag) {
			ChunkPos cPos = new ChunkPos(pos);
			if (!chunkMap.containsKey(cPos))
				return null;
			return chunkMap.get(cPos).stream().filter(rg -> rg.contain(pos) && (tag == null || orSerch(tag, rg.getTag())))
					.findFirst().orElse(null);
		}
	}

	private static boolean andSerch(String[] input, String[] target) {
		for (String in : input) {
			boolean flag = true;
			tLoop: for (String tar : target) {
				if (tar.equals(in)) {
					flag = false;
					break tLoop;
				}
			}
			//見つからなかった場合false
			if (flag)
				return false;
		}
		return true;
	}

	private static boolean orSerch(String[] input, String[] target) {
		for (String in : input) {
			for (String tar : target) {
				if (tar.equals(in)) {
					return true;
				}
			}
		}
		return false;
	}

	/** チャンク-レギオンリストのMap ルールがついてるレギオンのみ利用 */
	protected class RuleChunkRegingMap extends ChunkRegingMap {
		// フィルタリング
		@Override
		void addToMap(ChunkPos pos, RegionRect rect) {
			if (rect.haveRule())
				super.addToMap(pos, rect);
		}

		void sort() {
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
			return state != EnumPermissionState.DENY;
		}
	}
	// ====== ルール ======

	public static Map<String, RegionRule> RuleMap = new HashMap<>();

	// ====== 通知系 ======
	public static List<Runnable> listenerList = new ArrayList<>();

	public static void addListener(Runnable listener) {
		listenerList.add(listener);
	}

	// ====== マネージャの格納系 ======
	private static final Int2ObjectMap<RegionHolder> regionManager = Int2ObjectMaps.synchronize(new Int2ObjectLinkedOpenHashMap<>());

	public static void clearManager() {
		regionManager.clear();
	}

	@SideOnly(Side.CLIENT)
	public static RegionHolder getManager() {
		return getManager(Minecraft.getMinecraft().player.dimension, Side.CLIENT);
	}

	public static RegionHolder getManager(int dim, Side side) {
		// Mapにあったらそれを返す
		RegionHolder rm = regionManager.get(dim);
		if (rm != null)
			return rm;

		rm = new RegionHolder();
		regionManager.put(dim, rm);
		// リモートなら読み込まない
		if (side == Side.CLIENT) {
			return rm;
		}

		HideFaction.log.info("load region manager Dim = " + dim);
		// 読み込みトライ
		if (loadRegion(rm, DimensionManager.getWorld(dim)))
			// 欠損があればセーブ
			saveRegion(rm, DimensionManager.getWorld(dim));

		return rm;
	}

	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static final String regionList = "region.json";
	private static final String defaultRule = "defaultRule.json";
	private static final String ruleMap = "rule.json";

	public static void saveRegion(RegionHolder manager, WorldServer world) {
		writeData(new File(world.getChunkSaveLocation(), regionList), manager.RegionList);
		writeData(new File(world.getChunkSaveLocation(), defaultRule), manager.DefaultPermission);
		writeData(new File(world.getSaveHandler().getWorldDirectory(), ruleMap), RuleMap);
	}

	private static void writeData(File file, Object data) {
		try (FileWriter fileWriter = new FileWriter(file)) {
			if (file != null) {
				gson.toJson(data, fileWriter);
			}
		} catch (Exception exception1) {
			exception1.printStackTrace();
		}
	}

	/**データの欠けがあればtrue*/
	public static boolean loadRegion(RegionHolder rm, WorldServer world) {
		boolean flag = false;
		List<RegionRect> list = readData(new File(world.getChunkSaveLocation(), regionList), new TypeToken<List<RegionRect>>() {
		}.getType());
		if (list != null)
			rm.RegionList = list;
		else
			flag = true;

		Map<EnumRegionPermission, EnumPermissionState> defaultrule = readData(new File(world.getChunkSaveLocation(), defaultRule), new TypeToken<Map<EnumRegionPermission, EnumPermissionState>>() {
		}.getType());
		if (defaultrule != null)
			if (defaultrule.size() != 0)
				rm.DefaultPermission = new EnumMap<>(defaultrule);
			else
				rm.DefaultPermission = new EnumMap<>(EnumRegionPermission.class);
		else
			flag = true;

		Map<String, RegionRule> rule = readData(new File(world.getSaveHandler().getWorldDirectory(), ruleMap), new TypeToken<Map<String, RegionRule>>() {
		}.getType());
		if (rule != null)
			RuleMap = rule;
		else
			flag = true;

		rm.registerRegionMap();
		return flag;
	}

	private static <T> T readData(File file, Type type) {
		if (file == null || !file.exists()) {
			return null;
		}
		try (FileReader fileReader = new FileReader(file)) {
			return gson.fromJson(fileReader, type);
		} catch (Exception exception1) {
			exception1.printStackTrace();
			// 読み込み失敗時に名称変更
			try {
				Files.copy(file, new File(file, ".broken"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
