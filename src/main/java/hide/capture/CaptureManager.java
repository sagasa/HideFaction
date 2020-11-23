package hide.capture;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;

import hide.core.FactionUtil;
import hide.core.FixedUpdater;
import hide.region.RegionHolder;
import hide.region.RegionRect;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;

public class CaptureManager {
	protected Map<CapEntry, CountMap<String>> capState = new HashMap<>();
	protected MinecraftServer server;

	public CaptureManager(MinecraftServer server) {
		this.server = server;
	}

	FixedUpdater update = new FixedUpdater((delta) -> {
		//初期化
		capState.values().forEach(state -> state.clear());
		//プレイヤーを走査
		for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
			Iterator<RegionRect> itr = RegionHolder.getManager(player.dimension, Side.SERVER).getTagRegion(player.getPosition()).iterator();
			//レギオンを走査
			while (itr.hasNext()) {
				String[] tags = itr.next().getTag();
				//エントリを走査
				for (CapEntry entry : capState.keySet()) {
					if (ArrayUtils.contains(tags, entry.tag)) {
						//キーを作ってインクリメント
						capState.get(entry).increment(entry.target.getKey(player));
					}
				}
			}
		}
		MinecraftForge.EVENT_BUS.post(new CapUpdateEvent(delta, this));
	}, 500);

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

		private Target(Function<EntityPlayerMP, String> func) {
			this.func = func;
		}

		private Function<EntityPlayerMP, String> func;

		public String getKey(EntityPlayerMP player) {
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

		public int get(String key) {
			if (containsKey(key))
				return get(key);
			return 0;
		}
	}
}
