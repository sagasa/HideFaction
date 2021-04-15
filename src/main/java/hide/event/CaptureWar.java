package hide.event;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import hide.capture.CapUpdateEvent;
import hide.capture.CaptureManager;
import hide.capture.CaptureManager.CapEntry;
import hide.capture.CaptureManager.CountMap;
import hide.chat.HideChatSystem;
import hide.core.FactionUtil;
import hide.core.HideEvents.TeamUpdateClient;
import hide.core.util.BufUtil;
import hide.event.gui.GuiCapProgress;
import hide.event.gui.GuiCapState;
import hide.types.base.DataBase;
import hide.util.HideByteBufUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CaptureWar extends HideEvent {

	// == 本体設定 ==
	/** 占領の単位 */
	public static final DataEntry<CaptureManager.Target> Target = of(CaptureManager.Target.TEAM);
	/** 勝利条件 */
	public static final DataEntry<VictoryConditions> VictoryType = of(VictoryConditions.Both);
	/** 占領地 */
	public static final DataEntry<CapPointData[]> CapRegion = of(new CapPointData[0]);
	/** 占領ロジック */
	public static final DataEntry<PointType> CapWinType = of(PointType.Majority);
	/** ポイントの加算対象 */
	public static final DataEntry<PointType> PointGainType = of(PointType.All);
	/** 占領が維持されるか */
	public static final DataEntry<Boolean> KeepCap = of(true);
	/** 他チームが居る場合占領を進めない */
	public static final DataEntry<Boolean> ExclusiveCap = of(false);

	enum PointType {
		All, Majority
	}

	public enum VictoryConditions {
		Point, LastCap, Both
	}

	private static byte CapStatePointer = 0;

	public enum CapState {
		None, Invade, Capture, Stop, Occupied;

		private CapState() {
			index = CapStatePointer;
			CapStatePointer++;
		}

		final byte index;
	}

	public static class CapPointData extends DataBase {
		transient final CapEntry entry = new CapEntry();

		public static final DataEntry<String> Tag = of("regionTag");
		public static final DataEntry<String> Name = of("regionName");
		public static final DataEntry<Float> IncreaseFactor = of(1f);
		public static final DataEntry<Float> CapTime = of(1f);
		public static final DataEntry<Float> NeutralizeTime = of(1f);
		public static final DataEntry<Integer> PointGain = of(1);
	}

	// == セーブ ==
	private class CapWarSave {
		public CapWarSave() {
			CapPointData[] array = get(CapRegion);
			state = new CapPoint[array.length];
			for (int i = 0; i < state.length; i++) {
				state[i] = new CapPoint();
			}
			System.out.println("init save " + array.length);
		}

		CountMap<String> point = new CountMap<String>();
		transient boolean point_change = false;
		CapPoint[] state;
		boolean isStart = false;
	}

	private CapWarSave save;

	private class CapPoint {
		float progress;
		/** 現在占領中 */
		String current;
		/** 占領を試みているor1つ前 */
		String tmp;
		CapState state = CapState.None;

		transient boolean dirty;

		/** 「占領した」チームを返す */
		public String getCurrent() {
			return state == CapState.Capture ? null : current;
		}

		void mark() {
			dirty = true;
		}
	}

	@Override
	public void initServer(HideEventSystem manager) {
		if (save == null)
			save = new CapWarSave();
		// 登録
		CapPointData[] array = get(CapRegion);
		for (CapPointData entry : array) {
			entry.entry.tag = entry.get(CapPointData.Tag);
			entry.entry.target = get(Target);
			manager.CapManager.register(entry.entry);
		}
	}

	@Override
	void initClient() {
		save = new CapWarSave();
		gui = new GuiCapState(this);
		System.out.println("init client Hi " + gui);
	}

	static int guiCount = 0;

	@SideOnly(Side.CLIENT)
	@Override
	void preDrawOverlay() {
		guiCount = 0;
	}

	@SideOnly(Side.CLIENT)
	private GuiCapState gui;

	@SideOnly(Side.CLIENT)
	@Override
	void drawOverlay(ScaledResolution resolution) {
		if (gui != null && save.isStart) {
			gui.draw(resolution, guiCount);
			guiCount++;
		}
	}

	@SubscribeEvent()
	@SideOnly(Side.CLIENT)
	public void teamUpdate(TeamUpdateClient event) {
		if (side == Side.SERVER)
			return;
		gui.updateScoreState(getKey());
		gui.updateState(getKey());

	}

	@SubscribeEvent()
	public void capUpdate(CapUpdateEvent event) {
		// System.out.println("update " + getName() + " " + event.getDeltaTime());
		if (!save.isStart || side == Side.CLIENT)
			return;
		// 秒数換算
		float delta = event.getDeltaTime() / 1000f;
		CapPointData[] capPointData = get(CapRegion);
		for (int i = 0; i < capPointData.length; i++) {
			CapPointData entry = capPointData[i];
			CapPoint point = save.state[i];
			// 更新
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
				// 現在取ってるチーム以外なら
				if (!res.equals(point.current)) {
					point.state = CapState.Invade;
					// 占領更新
					if (point.current == null) {
						point.current = res;
					} else {
						point.progress -= delta / entry.get(CapPointData.NeutralizeTime) * max
								* entry.get(CapPointData.IncreaseFactor);
						point.tmp = res;
						// point.tmp = res;
						if (point.progress <= 0) {
							point.current = null;
							point.progress = 0;
						}
					}
					point.mark();
				}
				if (res.equals(point.current)) {
					if (point.progress < 1) {
						point.state = CapState.Capture;
						point.progress += delta / entry.get(CapPointData.CapTime) * max
								* entry.get(CapPointData.IncreaseFactor);
						if (1 <= point.progress) {
							// System.out.println("占領完了");
							point.state = CapState.Occupied;
						}
						point.mark();
					}
				}
			} else if (max != 0) {
				// 均衡なら
				if (point.state != CapState.Stop) {
					point.mark();
					point.state = CapState.Stop;
				}
			}
			// 誰も入っていない場合は設定に応じて減衰
			if (max == 0) {
				// 侵攻中だった場合
				if (point.state == CapState.Invade && 0 < point.progress && point.progress < 1) {
					point.progress += delta / entry.get(CapPointData.CapTime);
					if (1 <= point.progress) {
						point.progress = 1;
						point.state = CapState.Occupied;
					}
					point.mark();
				} else if ((point.state == CapState.Capture && point.progress < 1 || !get(KeepCap))
						&& 0 < point.progress) {
					point.progress -= delta / entry.get(CapPointData.NeutralizeTime);
					if (point.progress <= 0) {
						point.state = CapState.None;
						point.progress = 0;
						point.current = null;
					}
					point.mark();
				}
			}

			// System.out.println(point.progress+" "+point.current+" "+point.dirty);
		}

		toClient(null);

		for (CapPoint point : save.state) {
			point.dirty = false;
		}
		// System.out.println("state " + ArrayUtils.toString(capState));
		// System.out.println("value " + ArrayUtils.toString(capCurrent));

		saveState();
	}

	@Override
	void start() {
		save.isStart = true;
	}

	@Override
	void stop() {
		save.isStart = false;
		toClient(null);
		saveState();
	}

	@Override
	void clear() {
		save.point.clear();
		save.point_change = true;
	}

	transient private CountMap<String> countCash = new CountMap();

	@Override
	void update() {
		if (!save.isStart)
			return;
		if (get(VictoryType) != VictoryConditions.LastCap) {
			countCash.clear();
			CapPointData[] capPointData = get(CapRegion);
			for (int i = 0; i < capPointData.length; i++) {
				CapPointData entry = capPointData[i];
				CapPoint point = save.state[i];
				System.out.println("update  current " + point.current + " " + point.state);

				if (point.getCurrent() != null) {
					if (get(PointGainType) == PointType.All)
						save.point.increment(point.current, entry.get(CapPointData.PointGain));
					else
						countCash.increment(point.current, entry.get(CapPointData.PointGain));
					save.point_change = true;
				}
			}
			if (get(PointGainType) == PointType.Majority) {
				List<Entry<String, Integer>> big = countCash.biggestList();
				for (Entry<String, Integer> entry : big) {
					save.point.increment(entry.getKey(), entry.getValue());
					save.point_change = true;
				}
			}
		}

		for (Entry<String, Integer> entry : save.point.entrySet()) {
			sendMsgToTeam(entry.getKey(), "your current point is " + entry.getValue());
		}
		sendMsgToTeam(null, "capwar updated ");
		System.out.println("Update event " + save.point + " " + get(VictoryType));
	}

	@Override
	void end() {
		if (!save.isStart)
			return;
		CapPointData[] capPointData = get(CapRegion);
		if (get(VictoryType) != VictoryConditions.LastCap) {
			Iterator<Entry<String, Integer>> itr = save.point.entrySet().stream()
					.sorted((e0, e1) -> e1.getValue() - e0.getValue()).iterator();
			float sum = 0;
			for (Integer integer : save.point.values())
				sum += integer;

			int i = 1;
			int max = -1;
			sendMsgToTeam(null, String.format("§e%s §5Points", getName()));
			while (itr.hasNext()) {
				Entry<String, Integer> entry = itr.next();
				String displayname = FactionUtil.getFactionDisplay(eventSystem.server, entry.getKey());
				float percent = entry.getValue() / sum * 100;
				if (max != -1 && max != entry.getValue())
					i++;
				// 1位なら
				if (i == 1) {
					sendMsgToTeam(null,
							String.format("#%s %s %sPoint %.1f%% win!!", i, displayname, entry.getValue(), percent));
				} else {
					sendMsgToTeam(null,
							String.format("#%s %s %sPoint %.1f%%", i, displayname, entry.getValue(), percent));
				}
				max = entry.getValue();
			}
		}
		if (get(VictoryType) != VictoryConditions.Point) {
			countCash.clear();
			for (int i = 0; i < capPointData.length; i++) {
				CapPointData entry = capPointData[i];
				CapPoint point = save.state[i];
				if (point.getCurrent() != null) {
					countCash.increment(point.current, entry.get(CapPointData.PointGain));
				}
			}
			if (get(CapWinType) == PointType.Majority) {
				for (Entry<String, Integer> entry : countCash.biggestList()) {
					String displayname = FactionUtil.getFactionDisplay(eventSystem.server, entry.getKey());
					sendMsgToTeam(null, String.format("LastCap %s", displayname));
				}
			} else {
				for (Entry<String, Integer> entry : countCash.entrySet()) {
					String displayname = FactionUtil.getFactionDisplay(eventSystem.server, entry.getKey());
					sendMsgToTeam(null, String.format("LastCap %s", displayname));
				}
			}
		}
		System.out.println("End event");
	}

	protected void sendMsgToTeam(String team, String msg) {
		HideChatSystem.sendToInfo(team, new TextComponentString(
				String.format("§7[%s]§5[§aCapWar§5]§r%s§r", HideChatSystem.makeTimeString(), msg)));
	}

	@Override
	public void fromSave(String json) {
		save = gson.fromJson(json, CapWarSave.class);
	}

	@Override
	public String toSave() {
		return gson.toJson(save);
	}

	/** 自分のチーム取得 */
	@SideOnly(Side.CLIENT)
	private String getKey() {
		return get(Target).getKey(Minecraft.getMinecraft().player);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void fromServer(ByteBuf buf) {
		int size = buf.readByte();
		for (int i = 0; i < size; i++) {
			int index = buf.readByte();
			CapPoint point = save.state[index];
			point.progress = buf.readFloat();
			point.current = BufUtil.readString(buf);
			point.tmp = BufUtil.readString(buf);
			point.state = CapState.values()[buf.readByte()];
			CapPointData data = get(CapRegion)[index];
			GuiCapProgress.addOrUpdate(Minecraft.getMinecraft().getToastGui(), data.get(CapPointData.Tag),
					data.get(CapPointData.Name), point.state, point.current, point.tmp,
					get(Target).getKey(Minecraft.getMinecraft().player), point.progress);
			gui.updateState(index, point.progress, point.current, getKey());

		}
		if (buf.readBoolean()) {
			HideByteBufUtil.readCountMap(buf, save.point);
			gui.updateScoreState(save.point);
			gui.updateScoreState(getKey());
		}
		save.isStart = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf, boolean all) {
		int size = 0;
		for (CapPoint point : save.state) {
			if (point.dirty || all)
				size++;
		}
		buf.writeByte(size);
		for (int i = 0; i < save.state.length; i++) {
			CapPoint point = save.state[i];
			if (point.dirty || all) {
				buf.writeByte(i);
				buf.writeFloat(point.progress);
				BufUtil.writeString(buf, point.current);
				BufUtil.writeString(buf, point.tmp);
				buf.writeByte(point.state.index);
			}
		}
		buf.writeBoolean(save.point_change || all);
		if (save.point_change || all) {
			save.point_change = false;
			HideByteBufUtil.writeCountMap(buf, save.point);
		}
		buf.writeBoolean(save.isStart);
	}
}
