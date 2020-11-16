package hide.event;

import java.util.HashMap;
import java.util.Map;

import hide.capture.CaptureManager;
import net.minecraft.server.MinecraftServer;

public class CaptureWar implements IHideEvent {

	//== 本体設定 ==
	public CaptureManager.TARGET target;
	public String[] CapPoint;

	public PointType capWinType;
	public PointType pointGainType;

	enum PointType {
		All, Majority
	}

	public enum VictoryConditions {
		Point, LastCap, Both
	}

	 protected MinecraftServer server;

	//== セーブ ==
	public CaptureManager cm = new CaptureManager();
	public Map<String, Integer> point = new HashMap<>();

	public CaptureWar(MinecraftServer server, String json) {
	}

	@Override
	public String getName() {
		return null;
	}

	public void start() {

	}

	public void update() {
		cm.update(server);
	}

	public void end() {

	}

	public void load(String json) {

	}

	public String save() {
		return null;
	}

}
