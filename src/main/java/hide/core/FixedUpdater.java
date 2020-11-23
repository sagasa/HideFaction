package hide.core;

import java.util.function.Consumer;

public class FixedUpdater {
	private final Consumer<Integer> func;
	private final int interval;
	private long lastUpdate;

	public FixedUpdater(Consumer<Integer> func, int interval) {
		this.func = func;
		this.interval = interval;
	}

	public void run() {
		long time = System.currentTimeMillis();
		time -= time % interval;
		if (lastUpdate < time) {
			func.accept(lastUpdate == 0 ? 0 : (int) (time - lastUpdate));
			lastUpdate = time;
		}
	}
}
