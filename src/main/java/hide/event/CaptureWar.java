package hide.event;

import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;

import hide.capture.CapUpdateEvent;
import hide.capture.CaptureManager;
import hide.capture.CaptureManager.CapEntry;
import hide.capture.CaptureManager.CountMap;
import hide.types.base.DataBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CaptureWar extends HideEvent  {

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

	public static class CapPointEntry extends DataBase{
		transient final CapEntry entry = new CapEntry();

		public static final DataEntry<String> Tag = of("regionName");
		public static final DataEntry<Float> IncreaseFactor = of(1f);
		public static final DataEntry<Integer> CapTime = of(1);
		public static final DataEntry<Integer> NeutralizeTime = of(1);
		public static final DataEntry<Integer> PointGain = of(1);
	}

	//== セーブ ==
	public CountMap<String> point = new CountMap<String>();

	/**1で占領*/
	float[] capState;
	String[] capCurrent;

	boolean isStart = true;

	@Override
	public void init(HideEventManager manager) {
		//登録
		CapPointEntry[] array = get(CapRegion);
		for (CapPointEntry entry : array) {
			entry.entry.tag = entry.get(CapPointEntry.Tag);
			entry.entry.target = get(Target);
			manager.CapManager.register(entry.entry);
		}
		capState = new float[array.length];
		capCurrent = new String[array.length];
	}

	@SubscribeEvent()
	public void serverTick(CapUpdateEvent event) {
		System.out.println("update "+getName()+" "+event.getDeltaTime());
		if (!isStart)
			return;
		int delta = event.getDeltaTime();
		CapPointEntry[] capPoint = get(CapRegion);
		for (int i = 0; i < capPoint.length; i++) {
			CapPointEntry entry = capPoint[i];
			//更新
			System.out.println(event.getMap(entry.entry));
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
						capState[i] -= delta / (float) entry.get(CapPointEntry.NeutralizeTime) * max * entry.get(CapPointEntry.IncreaseFactor);
						if (capState[i] <= 0) {
							capCurrent[i] = null;
							capState[i] = 0;
						}
					}
				} else {
					if (capState[i] < 1) {
						capState[i] += delta / (float) entry.get(CapPointEntry.CapTime) * max * entry.get(CapPointEntry.IncreaseFactor);
					} else {
						capCurrent[i] = res;
					}
				}
			}
			//誰も入っていない場合は設定に応じて減衰
			if (max == 0) {
				if (capCurrent[i] == null) {
					if (0 < capState[i])
						capState[i] -= delta / (float) entry.get(CapPointEntry.NeutralizeTime);
				} else if (!get(KeepCap)) {
					capState[i] -= delta / (float) entry.get(CapPointEntry.NeutralizeTime);
					if (capState[i] <= 0)
						capCurrent[i] = null;
				}
			}
		}
		System.out.println("state "+ArrayUtils.toString(capState));
		System.out.println("value "+ArrayUtils.toString(capCurrent));
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
						point.increment(capCurrent[i], entry.get(CapPointEntry.PointGain));
					else
						countCash.increment(capCurrent[i], entry.get(CapPointEntry.PointGain));
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
