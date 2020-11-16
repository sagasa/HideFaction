package hide.capture;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;

import hide.core.FactionUtil;
import hide.region.RegionHolder;
import hide.region.RegionRect;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.relauncher.Side;

public class CaptureManager {
	protected Map<CapEntry, CapState> capState = new HashMap<>();

	public CaptureManager() {

	}

	public void update(MinecraftServer server) {
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
	}

	public void clear() {
		capState.clear();
	}

	public void register(CapEntry entry) {
		capState.put(entry, new CapState());
	}

	public enum TARGET {
		PLAYER(player -> player.getName()), TEAM(FactionUtil::getFaction);

		private TARGET(Function<EntityPlayerMP, String> func) {
			this.func = func;
		}

		private Function<EntityPlayerMP, String> func;

		public String getKey(EntityPlayerMP player) {
			return func.apply(player);
		}
	}

	public static class CapEntry {
		String tag;
		TARGET target;
	}

	public static class CapState {
		private Map<String, Integer> map = new HashMap<>();

		public void increment(String key) {
			if (!map.containsKey(key))
				map.put(key, 1);
			else
				map.put(key, map.get(key) + 1);
		}

		public void clear() {
			map.clear();
		}

		public int get(String key) {
			if (map.containsKey(key))
				return map.get(key);
			return 0;
		}
	}
}
