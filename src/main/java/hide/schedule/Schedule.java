package hide.schedule;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.server.MinecraftServer;

public class Schedule {
	public long startDay;
	public long endDay;
	public long cycleDay;

	public enum SectionMode {
		START, END, NONE;

		static SectionMode getMode(String name) {
			return valueOf(name.toUpperCase());
		}
	}

	public Schedule() {
		entryList = new ArrayList<>();
		entryList.addAll(entryMap.values());
		entryList.sort((a, b) -> a.time - b.time);
		startDay = 518400000;
		endDay = 518400000;
		cycleDay = 604800000;
	}

	private List<ScheduleEntryData> rawEntry = new ArrayList<>();

	private List<ScheduleEntry> onStart = new ArrayList<>();
	private List<ScheduleEntry> onEnd = new ArrayList<>();
	private List<ScheduleEntry> entryList = new ArrayList<>();
	private Multimap<Integer, ScheduleEntry> entryMap = MultimapBuilder.hashKeys().arrayListValues().build();

	/**指定した時間が含まれるか*/
	public boolean inSchedule(long date) {
		if (cycleDay != 0) {
			date %= cycleDay;
			return startDay % cycleDay <= date && date <= endDay % cycleDay;
		}
		return startDay < date && date < endDay;
	}

	public void start(MinecraftServer server, long lastUptime) {
		final int offset = TimeZone.getDefault().getRawOffset();
		long lastDate = lastUptime - (lastUptime + offset) % 86400000 + offset;
		long lastTime = lastUptime % 86400000;
		long uptime = System.currentTimeMillis();
		long date = uptime - (uptime + offset) % 86400000 + offset;
		long time = uptime % 86400000;
		long length = endDay < startDay ? endDay + cycleDay - startDay : endDay - startDay;

		//やり残した終了処理回収
		if (inSchedule(lastDate)) {
			long startDate = cycleDay == 0 ? startDay : lastDate - lastDate % cycleDay + startDay % cycleDay;
			long endDate = startDate + length;
			//entryListを走査
			HashSet<String> sections = new HashSet<>();
			for (int i = 0; i < entryList.size(); i++) {
				ScheduleEntry entry = entryList.get(i);
				//実行済orセクション区分なし
				if (entry.time <= lastTime || entry.mode == SectionMode.NONE)
					continue;
				//１日経ってないなら現在時間でbreak
				if (lastDate == date && time < entry.time)
					break;
				if (entry.mode == SectionMode.START)
					sections.add(entry.section);
				else if (!sections.contains(entry.section)) {
					//終了処理
					entry.run(server);
				}
			}
			//同じスケジュール外ならイベント終了処理
			if (endDate < date)
				onEnd.forEach(entry -> entry.run(server));
		}
		//やり残した開始処理回収
		if (inSchedule(date)) {
			long startDate = cycleDay == 0 ? startDay : date - date % cycleDay + startDay % cycleDay;
			long endDate = startDate + length;
			//新しいスケジュールならイベント開始処理
			if (lastDate < startDate)
				onStart.forEach(entry -> entry.run(server));

			//entryListを走査
			HashSet<String> sections = new HashSet<>();
			List<ScheduleEntry> tmp = new ArrayList<>();
			for (int i = entryList.size() - 1; 0 <= i; i--) {
				ScheduleEntry entry = entryList.get(i);
				//未到達orセクション区分なし
				if (lastTime <= entry.time || entry.mode == SectionMode.NONE)
					continue;
				//１日経ってないなら前回タイムでbreak
				if (lastDate == date && entry.time < lastTime)
					break;
				if (entry.mode == SectionMode.END)
					sections.add(entry.section);
				else if (!sections.contains(entry.section)) {
					//開始処理
					tmp.add(entry);
				}
			}
			//順序を守って実行
			Lists.reverse(tmp).forEach(entry -> entry.run(server));
		}
	}

	/**時間と分のみのmill*/
	public void update(MinecraftServer server, int time) {
		//Multimaps.newListMultimap(map, ArrayList::new);
		entryMap.get(time).forEach(entry -> {
			entry.run(server);
		});
	}

	public static class ScheduleEntry {
		private int time;
		private String section;
		private SectionMode mode = SectionMode.NONE;
		private String cmd = "";

		public void run(MinecraftServer server) {
			server.getCommandManager().executeCommand(server, cmd);
			System.out.println("Run " + cmd);
		}

		public ScheduleEntry(int time, String cmd, SectionMode mode, String section) {
			this.time = time;
			this.cmd = cmd;
			this.mode = mode;
			this.section = section;
		}
	}

	/**json内部のデータ*/
	public static class ScheduleEntryData implements Cloneable {
		/**開始時間*/
		private int time;
		/**インターバル 0なら繰り返さない*/
		private int interval = 0;
		/**終了*/
		private int end = 0;

		private boolean onStart;
		private boolean onEnd;

		private static int defaultEnd = 86400000 + TimeZone.getDefault().getRawOffset();

		private String section;
		private SectionMode mode = SectionMode.NONE;
		private String cmd = "";

		public void register(Schedule schedule) {
			if (onStart)
				schedule.onStart.add(new ScheduleEntry(time, cmd, mode, section));
			else if (onEnd)
				schedule.onEnd.add(new ScheduleEntry(time, cmd, mode, section));
			else {
				System.out.println(time + " " + interval + " " + end);
				if (interval == 0) {
					ScheduleEntry entry = new ScheduleEntry(time, cmd, mode, section);
					schedule.entryMap.put(time, entry);
					schedule.entryList.add(entry);
				} else
					for (int i = time; i < end; i += interval) {
						int fixed = i % 86400000;
						ScheduleEntry entry = new ScheduleEntry(fixed, cmd, mode, section);
						//System.out.println("AAAA " + time + " " + fixed + " " + end);
						schedule.entryMap.put(fixed, entry);
						schedule.entryList.add(entry);
					}
			}
		}

		@Override
		public String toString() {
			return super.toString() + " " + time;
		}
	}

	static final SimpleDateFormat formatHMM = new SimpleDateFormat("h:mm");
	static final SimpleDateFormat formatHMM_GMT = new SimpleDateFormat("h:mm");
	static {
		formatHMM_GMT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**日付を無視して時間に変換 trueで時差補正*/
	static int toTime(String str, boolean fix) {
		try {
			if (fix)
				return (int) ((formatHMM.parse(str).getTime() + 86400000) % 86400000);
			else
				return (int) ((formatHMM_GMT.parse(str).getTime() + 86400000) % 86400000);
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**曜日指定のプリセット*/
	private enum PresetTime {
		MON(345600000), TUE(432000000), WED(518400000), THU(0), FRI(86400000), SAT(172800000), SUN(259200000);
		static final long WEEK = 604800000;
		static final long DAY = 86400000;
		static final SimpleDateFormat formatMMDD = new SimpleDateFormat("MM/dd");
		static final SimpleDateFormat formatYYYYMMDD = new SimpleDateFormat("yyyy/MM/dd");
		private long time;

		private PresetTime(long time) {
			this.time = time;
		}

		static boolean inPreset(String time) {
			for (PresetTime preset : values())
				if (preset.toString().equalsIgnoreCase(time))
					return false;
			return false;
		}

		static boolean inPreset(long time) {
			for (PresetTime preset : values())
				if (preset.time == time)
					return true;
			return false;
		}

		static String getTime(long time) {
			for (PresetTime preset : values()) {
				if (preset.time == time)
					return preset.toString();
			}
			return formatYYYYMMDD.format(time);
		}

		static long getTime(String time) {
			for (PresetTime preset : values()) {
				if (time.equalsIgnoreCase(preset.toString()))
					return preset.time;
			}
			String[] split = time.split("/");
			try {
				if (split.length == 2)
					return formatMMDD.parse(time).getTime();
				else if (split.length == 3)
					return formatYYYYMMDD.parse(time).getTime();
			} catch (ParseException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			throw new IllegalArgumentException("cant cast to time");
		}
	}

	public static Gson gson = new GsonBuilder().setPrettyPrinting()
			.registerTypeAdapter(Schedule.class, new JsonSerializer<Schedule>() {
				@Override
				public JsonElement serialize(Schedule src, Type typeOfSrc, JsonSerializationContext context) {
					JsonObject obj = new JsonObject();

					obj.addProperty("start", PresetTime.getTime(src.startDay));
					obj.addProperty("end", PresetTime.getTime(src.endDay));
					if (src.cycleDay != 0 && !PresetTime.inPreset(src.startDay))
						obj.addProperty("cycle", src.cycleDay / PresetTime.DAY);

					JsonArray array = new JsonArray();
					obj.add("entry", array);
					for (ScheduleEntryData entry : src.rawEntry)
						array.add(context.serialize(entry, ScheduleEntryData.class));

					return obj;
				}
			})
			.registerTypeAdapter(Schedule.class, new JsonDeserializer<Schedule>() {
				@Override
				public Schedule deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
					JsonObject obj = json.getAsJsonObject();
					JsonArray array = obj.get("entry").getAsJsonArray();
					Schedule schedule = new Schedule();
					//時間読み取り
					String start = obj.get("start").getAsString();
					schedule.cycleDay = PresetTime.inPreset(start) ? PresetTime.WEEK : obj.get("cycle").getAsInt() * PresetTime.DAY;
					schedule.startDay = PresetTime.getTime(start);
					schedule.endDay = PresetTime.getTime(obj.get("end").getAsString());

					//エントリ読み取り
					for (JsonElement element : array) {
						ScheduleEntryData entry = context.deserialize(element, ScheduleEntryData.class);
						schedule.rawEntry.add(entry);
						entry.register(schedule);
					}
					schedule.entryList.sort((a, b) -> a.time - b.time);
					return schedule;
				}
			})

			.registerTypeAdapter(ScheduleEntryData.class, new JsonSerializer<ScheduleEntryData>() {
				@Override
				public JsonElement serialize(ScheduleEntryData src, Type typeOfSrc, JsonSerializationContext context) {
					JsonObject obj = new JsonObject();
					if (src.onStart)
						obj.addProperty("time", "START");
					else if (src.onEnd)
						obj.addProperty("time", "END");
					else
						obj.addProperty("time", formatHMM.format(src.time));
					if (src.interval != 0) {
						obj.addProperty("interval", formatHMM.format(src.interval));
						if (src.end != ScheduleEntryData.defaultEnd)
							obj.addProperty("end", formatHMM.format(src.end));
					}
					if (src.mode != SectionMode.NONE) {
						obj.addProperty("mode", src.mode.toString());
						obj.addProperty("section", src.section);
					}
					obj.addProperty("cmd", src.cmd.toString());
					return obj;
				}
			})
			.registerTypeAdapter(ScheduleEntryData.class, new JsonDeserializer<ScheduleEntryData>() {
				@Override
				public ScheduleEntryData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
					JsonObject obj = json.getAsJsonObject();
					ScheduleEntryData schedule = new ScheduleEntryData();
					String time = obj.get("time").getAsString();
					if (time.equalsIgnoreCase("START"))
						schedule.onStart = true;
					else if (time.equalsIgnoreCase("END"))
						schedule.onEnd = true;
					else
						schedule.time = toTime(time, true);
					if (obj.has("interval")) {
						schedule.interval = toTime(obj.get("interval").getAsString(), false);
						if (obj.has("end"))
							schedule.end = toTime(obj.get("end").getAsString(), false) + TimeZone.getDefault().getOffset(System.currentTimeMillis());
						else
							schedule.end = ScheduleEntryData.defaultEnd;
					}
					if (obj.has("mode")) {
						SectionMode mode = SectionMode.getMode(obj.get("mode").getAsString());
						if (mode != SectionMode.NONE) {
							schedule.mode = mode;
							schedule.section = obj.get("section").getAsString();
						}
					}
					schedule.cmd = obj.get("cmd").getAsString();
					return schedule;
				}
			})//*/
			.create();
}
