package hide.capture;

import hide.capture.CaptureManager.CapEntry;
import hide.capture.CaptureManager.CountMap;
import net.minecraftforge.fml.common.eventhandler.Event;

public class CapUpdateEvent extends Event {

	int delta;
	CaptureManager cm;

	public CapUpdateEvent(int delta, CaptureManager cm) {
		this.delta = delta;
		this.cm = cm;
	}

	public CountMap<String> getMap(CapEntry entry) {
		return cm.get(entry);
	}

	public int getDeltaTime() {
		return delta;
	}
}
