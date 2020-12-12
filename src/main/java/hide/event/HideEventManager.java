package hide.event;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hide.capture.CaptureManager;
import hide.event.CaptureWar.CapPointEntry;
import hide.types.base.DataBase;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;

public class HideEventManager {

	File eventDir = new File(Loader.instance().getConfigDir().getParent(), "Hide/event");

	List<HideEvent> eventList = new ArrayList<>();
	Map<String, HideEvent> map = new HashMap<>();

	public CaptureManager CapManager;
	public MinecraftServer server;

	public void load(final MinecraftServer server) {
		this.server = server;
		// 削除
		eventList.forEach(event -> {
			MinecraftForge.EVENT_BUS.unregister(event);
		});
		eventList.clear();
		map.clear();

		// サンプル作成
		writeSample(CaptureWar.class);
		writeSample(CapPointEntry.class);

		CapManager = new CaptureManager(server, 500);

		// 読み込み
		eventDir.mkdirs();
		for (File file : eventDir.listFiles(file -> file.isFile() && file.getName().endsWith(".json"))) {
			try {
				if (!file.getName().startsWith("-"))
					eventList.add(HideEvent.fromFile(file));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		// 親子関係
		HideEvent.resolvParent(eventList);

		// 登録
		eventList.forEach(event -> {
			MinecraftForge.EVENT_BUS.register(event);
		});
		eventList.forEach(event -> {
			map.put(event.getName(), event);
			event.init(this);
		});
	}

	public void update() {
		CapManager.update();
	}

	public boolean eventStart(String name) {
		if (map.containsKey(name)) {
			map.get(name).start();
			return true;
		}
		return false;
	}

	public boolean eventUpdate(String name) {
		if (map.containsKey(name)) {
			map.get(name).update();
			return true;
		}
		return false;
	}

	public boolean eventEnd(String name) {
		if (map.containsKey(name)) {
			map.get(name).end();
			return true;
		}
		return false;
	}

	private void writeSample(Class<? extends DataBase> clazz) {
		File file = new File(eventDir.getPath(), "-" + clazz.getSimpleName() + ".json");
		try {
			file.createNewFile();
			Files.write(file.toPath(), DataBase.getSample(clazz).getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
}
