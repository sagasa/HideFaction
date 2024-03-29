package hide.capture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.base.Strings;

import hide.core.FactionUtil;
import hide.core.FixedUpdater;
import hide.region.RegionHolder;
import hide.region.RegionRect;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;

public class CaptureManager {
	protected Map<CapEntry, CountMap<String>> capState = new HashMap<>();
	protected MinecraftServer server;

	public CaptureManager(MinecraftServer server, int interval) {
		this.server = server;
		update = new FixedUpdater((delta) -> {
			// 初期化
			capState.values().forEach(state -> state.clear());
			// プレイヤーを走査
			for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
				Iterator<RegionRect> itr = RegionHolder.getManager(player.dimension, Side.SERVER)
						.getTagRegion(player.getPosition()).iterator();
				// レギオンを走査
				while (itr.hasNext()) {
					RegionRect rect = itr.next();

					// エントリを走査
					for (CapEntry entry : capState.keySet()) {
						// System.out.println(player.getName()+" "+ArrayUtils.toString(rect.getTag())+"
						// "+entry.tag);
						if (rect.haveTag(entry.tag)) {
							// キーを作ってインクリメント
							String key = entry.target.getKey(player);
							if (!Strings.isNullOrEmpty(key))
								capState.get(entry).increment(key);
						}
					}
				}
			}
			// System.out.println(capState);
			MinecraftForge.EVENT_BUS.post(new CapUpdateEvent(delta, this));
		}, interval);
	}

	FixedUpdater update;

	public void update() {
		update.run();
	}

	public void clear() {
		capState.clear();
	}

	public void register(CapEntry entry) {
		capState.put(entry, new CountMap());
	}

	public CountMap<String> get(CapEntry entry) {
		return capState.get(entry);
	}

	public enum Target {
		PLAYER(player -> player.getName()), TEAM(FactionUtil::getFaction);

		private Target(Function<EntityPlayer, String> func) {
			this.func = func;
		}

		private Function<EntityPlayer, String> func;

		public String getKey(EntityPlayer player) {
			return func.apply(player);
		}
	}

	public static class CapEntry {
		public String tag;
		public Target target;
	}

	public static class CountMap<T> extends HashMap<T, Integer> {

		public void increment(T key) {
			increment(key, 1);
		}

		public void increment(T key, int i) {
			if (!containsKey(key))
				put(key, i);
			else
				put(key, get(key) + i);
		}

		/** 最大を取得 2つあればnull */
		public Entry<T, Integer> biggest() {
			int max = 0;
			Entry<T, Integer> res = null;
			for (Entry<T, Integer> pair : entrySet()) {
				if (max < pair.getValue()) {
					max = pair.getValue();
					res = pair;
				} else if (max == pair.getValue()) {
					res = null;
				}
			}
			return res;
		}

		/** 最大を取得 2つあればnull */
		public List<Entry<T, Integer>> biggestList() {
			int max = 0;
			List<Entry<T, Integer>> res = new ArrayList<>();
			for (Entry<T, Integer> pair : entrySet()) {
				if (max < pair.getValue()) {
					max = pair.getValue();
					res.clear();
					res.add(pair);
				} else if (max == pair.getValue()) {
					res.add(pair);
				}
			}
			return res;
		}

		public int getNum(T key) {
			if (containsKey(key))
				return get(key);
			return 0;
		}
	}
}
