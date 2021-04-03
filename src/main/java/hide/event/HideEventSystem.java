package hide.event;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hide.capture.CaptureManager;
import hide.core.HideFaction;
import hide.core.IHideSubSystem;
import hide.core.network.DataSync;
import hide.core.network.DataSync.ISyncInterface;
import hide.core.network.DataSync.SyncEntry;
import hide.event.CaptureWar.CapPointData;
import hide.schedule.ScheduleManager;
import hide.types.base.DataBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HideEventSystem implements IHideSubSystem {

	private HideEventSystem() {
	}

	File eventDir = new File(Loader.instance().getConfigDir().getParent(), "Hide/event");

	public static SyncEntry<List<HideEvent>> HideEventSync = DataSync.register(ArrayList::new,
			new ISyncInterface<List<HideEvent>>() {
				@Override
				public void fromBytes(List<HideEvent> obj, ByteBuf buf) {
					obj.clear();
					int count = buf.readInt();
					for (int i = 0; i < count; i++) {
						obj.add(HideEvent.fromJson(ByteBufUtils.readUTF8String(buf)));
					}
				}

				@Override
				public void toBytes(List<HideEvent> obj, ByteBuf buf) {
					buf.writeInt(obj.size());
					for (HideEvent hideEvent : obj) {
						ByteBufUtils.writeUTF8String(buf, hideEvent.toJson());
					}
				}
			});
	public static HideEventSystem INSTANCE = new HideEventSystem();

	static {
		HideEventSync.addListener(list -> list.forEach(e -> e.initClient()));
	}

	List<HideEvent> eventList = HideEventSync.ServerData;

	Map<String, HideEvent> map = new HashMap<>();

	public CaptureManager CapManager;
	public MinecraftServer server;

	@Override
	public void init(Side side) {
		// サンプル作成
		writeSample(CaptureWar.class);
		writeSample(CapPointData.class);
		HideFaction.registerNetMsg(NetHandler.class, NetMsg.class, side);
		ScheduleManager.load();
	}

	@SubscribeEvent()
	@SideOnly(Side.CLIENT)
	public void drawGui(RenderGameOverlayEvent event) {
		if (event.getType() == ElementType.HOTBAR) {
			HideEventSync.ClientData.forEach(e -> e.drawOverlay(event.getResolution()));
		}
	}

	@SubscribeEvent()
	public void serverTick(ServerTickEvent event) {
		if (event.phase == Phase.END) {
			ScheduleManager.update();
			CapManager.update();
		}
	}

	File SaveDir;

	@Override
	public void serverStart(FMLServerStartingEvent event) {

		event.registerServerCommand(new EventCommand());
		this.server = event.getServer();
		SaveDir = server.getActiveAnvilConverter().getFile(server.getFolderName(), "/hide/event/");
		SaveDir.mkdirs();
		CapManager = new CaptureManager(server, 500);
		load();
		ScheduleManager.start(event.getServer(), 1603837200000l);
	}

	public void load() {
		// 削除
		eventList.forEach(arg -> {
			MinecraftForge.EVENT_BUS.unregister(arg);
		});
		eventList.clear();
		map.clear();

		// 読み込み
		eventDir.mkdirs();
		for (File file : eventDir.listFiles(file -> file.isFile() && file.getName().endsWith(".json"))) {
			try {
				if (!file.getName().startsWith("-")) {
					HideEvent arg = HideEvent.fromFile(file);
					// index付与
					arg.index = (byte) eventList.size();
					// load
					loadState(arg);
					eventList.add(arg);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		// 親子関係
		HideEvent.resolvParent(eventList);

		// 登録
		eventList.forEach(arg -> {
			MinecraftForge.EVENT_BUS.register(arg);
		});
		eventList.forEach(arg -> {
			map.put(arg.getName(), arg);
			arg.initServer(this);
		});

	}

	private void loadState(HideEvent event) {
		File file = new File(SaveDir, event.getSaveName());
		try {
			if (file.exists()) {
				event.fromSave(new String(Files.readAllBytes(Paths.get(file.getPath())), StandardCharsets.UTF_8));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	/** サンプル書き込み */
	private void writeSample(Class<? extends DataBase> clazz) {

		File file = new File(eventDir.getPath(), "-" + clazz.getSimpleName() + ".json");
		try {
			file.createNewFile();
			Files.write(file.toPath(), DataBase.getSample(clazz, false).getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public static class NetMsg implements IMessage {

		HideEvent event;

		byte index;
		ByteBuf buffer;

		public NetMsg() {

		}

		public NetMsg(HideEvent event) {
			this.event = event;
			index = event.index;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			index = buf.readByte();
			buffer = buf.copy();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeByte(index);
			event.toBytes(buf);
		}

	}

	public static class NetHandler implements IMessageHandler<NetMsg, IMessage> {
		@Override
		public IMessage onMessage(NetMsg msg, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				INSTANCE.HideEventSync.ClientData.get(msg.index).fromServer(msg.buffer);
				msg.buffer.release();
			});
			return null;
		}
	}
}
