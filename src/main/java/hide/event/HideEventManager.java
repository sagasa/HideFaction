package hide.event;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import hide.types.base.DataBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;

public class HideEventManager {

	List<IHideEvent> eventList = new ArrayList<>();

	public void load() {
		//削除
		eventList.forEach(event -> {
			MinecraftForge.EVENT_BUS.unregister(event);
		});
		eventList.clear();

		;
		//読み込み
		File dir = new File(Loader.instance().getConfigDir().getParent(), "Hide/event");
		dir.mkdirs();
		for (File file : dir.listFiles(file -> file.isFile() && file.getName().endsWith(".json"))) {
			try {
				eventList.add(DataBase.fromJson(new String(Files.readAllBytes(Paths.get(file.getPath())), StandardCharsets.UTF_8)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		//登録
		eventList.forEach(event -> {
			MinecraftForge.EVENT_BUS.register(event);
		});
	}
}
