package hide.schedule;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Loader;

public class ScheduleManager {

	static MinecraftServer server;
	static List<Schedule> list = new ArrayList<>();
	static long nextUpdate;


	public static void load() {
		File dir = new File(Loader.instance().getConfigDir().getParent(), "schedule");
		dir.mkdirs();
		for (File file : dir.listFiles(file -> file.isFile() && file.getName().endsWith(".json"))) {
			try {
				list.add(Schedule.gson.fromJson(new String(Files.readAllBytes(Paths.get(file.getPath())), StandardCharsets.UTF_8), Schedule.class));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void update() {
		long time = System.currentTimeMillis();
		time -= time % 60000;
		if (nextUpdate < time) {
			nextUpdate = time;
			int t = (int) (time % 86400000);
			list.forEach(scedule -> scedule.update(server, t));
		}
	}

	public static void start(MinecraftServer s, long lastUptime) {
		server = s;
		list.forEach(scedule -> scedule.start(server, lastUptime));
		final int offset = TimeZone.getDefault().getRawOffset();
		long lastdate = lastUptime - (lastUptime + offset) % 86400000;
		long date = System.currentTimeMillis();
		date -= (date + offset) % 86400000;

		System.out.println(date + " " + lastdate + " " + System.currentTimeMillis());
	}
}
