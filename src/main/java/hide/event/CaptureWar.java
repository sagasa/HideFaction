package hide.event;

import java.util.Map.Entry;

import hide.capture.CapUpdateEvent;
import hide.capture.CaptureManager;
import hide.capture.CaptureManager.CapEntry;
import hide.capture.CaptureManager.CountMap;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CaptureWar implements IHideEvent {

	//== 本体設定 ==
	public CaptureManager.Target target;
	public CapPointEntry[] capPoint;

	public VictoryConditions victoryType;
	public PointType capWinType;
	public PointType pointGainType;
	public int interval;
	public boolean keepCap;
	public boolean exclusiveCap;

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

	protected MinecraftServer server;

	//== セーブ ==
	public CaptureManager cm = new CaptureManager();
	public CountMap<String> point = new CountMap<String>();

	/**1で占領*/
	float[] capState;
	String[] capCurrent;

	boolean isStart;

	public CaptureWar(MinecraftServer server, String json) {
		//登録
		for (CapPointEntry entry : capPoint) {
			entry.entry.tag = entry.tag;
			entry.entry.target = target;
			cm.register(entry.entry);
		}
	}

	@Override
	public String getName() {
		return null;
	}

	/**間隔(ms)*/
	public int interval() {
		return 500;
	}

	@SubscribeEvent()
	public void serverTick(CapUpdateEvent event) {
		if (!isStart)
			return;
		int delta = event.getDeltaTime();
		for (int i = 0; i < capPoint.length; i++) {
			CapPointEntry entry = capPoint[i];
			//更新

			int max = 0;
			String res = null;
			for (Entry<String, Integer> pair : cm.get(entry.entry).entrySet()) {
				if (0 < pair.getValue() && 0 < max && exclusiveCap)
					res = null;
				if (max < pair.getValue() && (!exclusiveCap || max == 0)) {
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
				} else if (!keepCap) {
					capState[i] -= delta / (float) entry.neutralizeTime;
					if (capState[i] <= 0)
						capCurrent[i] = null;
				}
			}

		}
	}

	public void start() {

	}

	transient private CountMap<String> countCash = new CountMap();

	public void update() {
		if (victoryType != VictoryConditions.LastCap) {
			countCash.clear();
			for (int i = 0; i < capPoint.length; i++) {
				CapPointEntry entry = capPoint[i];
				if (capCurrent[i] != null)
					if (pointGainType == PointType.All)
						point.increment(capCurrent[i], entry.pointGain);
					else
						countCash.increment(capCurrent[i], entry.pointGain);
			}
			if (pointGainType == PointType.Majority) {
				Entry<String, Integer> big = countCash.biggest();
				point.increment(big.getKey(), big.getValue());
			}
		}
	}

	public void end() {

	}

	public void load(String json) {

	}

	public String save() {
		return null;
	}

}
