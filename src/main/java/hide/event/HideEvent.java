package hide.event;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import hide.core.HideFaction;
import hide.types.base.DataBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**イベントバスにはクライアント側も登録される sideで分岐必須*/
public abstract class HideEvent extends DataBase {

	protected Gson gson = new GsonBuilder().setPrettyPrinting().create();

	/**読み込み時のindex*/
	transient byte index;

	/** 親の登録名 String */
	public static final DataEntry<String> ParentName = of("");

	protected Side side = Side.SERVER;

	protected void setParent(HideEvent data) {
		parent = data;
		initParent();
	}

	public static void resolvParent(Collection<? extends HideEvent> collection) {
		Map<String, HideEvent> map = new HashMap<>();
		collection.forEach(data -> map.put(data.getName(), data));
		collection.forEach(data -> data.setParent(map.get(data.get(HideEvent.ParentName, null))));
	}

	private String name;

	public static HideEvent fromFile(File file) throws Throwable {
		HideEvent event = DataBase
				.fromJson(new String(Files.readAllBytes(Paths.get(file.getPath())), StandardCharsets.UTF_8));
		event.name = file.getName().substring(0, file.getName().lastIndexOf('.'));
		return event;
	}

	/** 登録名 */
	public final String getName() {
		return name;
	}

	abstract void initServer(HideEventSystem manager);

	void initClient() {

	};

	@SideOnly(Side.CLIENT)
	void drawOverlay(ScaledResolution resolution) {

	};

	@SideOnly(Side.CLIENT)
	void preDrawOverlay() {

	};

	abstract void start();

	abstract void update();

	abstract void end();

	abstract void fromSave(String json);

	abstract String toSave();

	abstract void fromServer(ByteBuf buf);

	/**ログイン時等全部配信するときtrue*/
	abstract void toBytes(ByteBuf buf, boolean all);

	/**指定した場合全種送信*/
	protected void toClient(EntityPlayerMP player) {
		if (player == null)
			HideFaction.NETWORK.sendToAll(new HideEventSystem.NetMsg(this));
		else
			HideFaction.NETWORK.sendTo(new HideEventSystem.NetMsg(this, true), player);
	}

	String getSaveName() {
		return getName() + ".json";
	}

	protected void saveState() {
		File file = new File(HideEventSystem.INSTANCE.SaveDir, getSaveName());
		try {
			file.createNewFile();
			Files.write(file.toPath(), toSave().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/***/

}
