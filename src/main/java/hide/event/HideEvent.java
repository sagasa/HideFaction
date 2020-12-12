package hide.event;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import hide.types.base.DataBase;

public abstract class HideEvent extends DataBase {

	/** 親の登録名 String */
	public static final DataEntry<String> ParentName = of("");

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

	abstract void init(HideEventManager manager);

	abstract void start();

	abstract void update();

	abstract void end();

	abstract void load(String json);

	abstract String save();

	/***/

}
