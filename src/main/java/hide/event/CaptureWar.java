package hide.event;

import java.util.Map.Entry;

import hide.capture.CapUpdateEvent;
import hide.capture.CaptureManager;
import hide.capture.CaptureManager.CapEntry;
import hide.capture.CaptureManager.CountMap;
import hide.core.HideFaction;
import hide.types.base.DataBase;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CaptureWar extends DataBase implements IHideEvent {

	//== 本体設定 ==
	public static final DataEntry<CaptureManager.Target> Target = of(CaptureManager.Target.TEAM);
	public static final DataEntry<VictoryConditions> VictoryType = of(VictoryConditions.Both);
	public static final DataEntry<CapPointEntry[]> CapRegion = of(new CapPointEntry[0]);
	public static final DataEntry<PointType> CapWinType = of(PointType.Majority);
	public static final DataEntry<PointType> PointGainType = of(PointType.All);
	public static final DataEntry<Integer> interval = of(0);
	public static final DataEntry<Boolean> KeepCap = of(true);
	public static final DataEntry<Boolean> ExclusiveCap = of(false);

	enum PointType {
		All, Majority
	}

	public enum VictoryConditions {
		Point, LastCap, Both
	}

	static class CapPointEntry {
		transient final CapEntry entry = new CapEntry();
		String tag;
		float increaseFactor = 1;
		int capTime;
		int neutralizeTime;
		int pointGain;
	}

	//== セーブ ==
	public CountMap<String> point = new CountMap<String>();

	/**1で占領*/
	float[] capState;
	String[] capCurrent;

	boolean isStart;

	@Override
	public void init(MinecraftServer server) {
		//登録
		for (CapPointEntry entry : get(CapRegion)) {
			entry.entry.tag = entry.tag;
			entry.entry.target = get(Target);
			HideFaction.CapManager.register(entry.entry);
		}
	}

	@Override
	public String getName() {
		return null;
	}

	@SubscribeEvent()
	public void serverTick(CapUpdateEvent event) {
		if (!isStart)
			return;
		int delta = event.getDeltaTime();
		CapPointEntry[] capPoint = get(CapRegion);
		for (int i = 0; i < capPoint.length; i++) {
			CapPointEntry entry = capPoint[i];
			//更新

			int max = 0;
			String res = null;
			for (Entry<String, Integer> pair : event.getMap(entry.entry).entrySet()) {
				if (0 < pair.getValue() && 0 < max && get(ExclusiveCap))
					res = null;
				if (max < pair.getValue() && (!get(ExclusiveCap) || max == 0)) {
					max = pair.getValue();
					res = pair.getKey();
				} else if (max == pair.getValue()) {
					res = null;
				}
			}
			if (res != null) {
				//現在取ってるチーム以外なら
				if (!res.equals(capCurrent[i])) {
					//占領更新
					if (capCurrent[i] == null)
						capCurrent[i] = res;
					else {
						capState[i] -= delta / (float) entry.neutralizeTime * max * entry.increaseFactor;
						if (capState[i] <= 0) {
							capCurrent[i] = null;
							capState[i] = 0;
						}
					}
				} else {
					if (capState[i] < 1) {
						capState[i] += delta / (float) entry.capTime * max * entry.increaseFactor;
					} else {
						capCurrent[i] = res;
					}
				}
			}
			//誰も入っていない場合は設定に応じて減衰
			if (max == 0) {
				if (capCurrent[i] == null) {
					if (0 < capState[i])
						capState[i] -= delta / (float) entry.neutralizeTime;
				} else if (!get(KeepCap)) {
					capState[i] -= delta / (float) entry.neutralizeTime;
					if (capState[i] <= 0)
						capCurrent[i] = null;
				}
			}

		}
	}

	@Override
	public void start() {

	}

	transient private CountMap<String> countCash = new CountMap();

	@Override
	public void update() {
		if (get(VictoryType) != VictoryConditions.LastCap) {
			countCash.clear();
			CapPointEntry[] capPoint = get(CapRegion);
			for (int i = 0; i < capPoint.length; i++) {
				CapPointEntry entry = capPoint[i];
				if (capCurrent[i] != null)
					if (get(PointGainType) == PointType.All)
						point.increment(capCurrent[i], entry.pointGain);
					else
						countCash.increment(capCurrent[i], entry.pointGain);
			}
			if (get(PointGainType) == PointType.Majority) {
				Entry<String, Integer> big = countCash.biggest();
				point.increment(big.getKey(), big.getValue());
			}
		}
	}

	@Override
	public void end() {

	}

	@Override
	public void load(String json) {

	}

	@Override
	public String save() {
		return null;
	}

}
