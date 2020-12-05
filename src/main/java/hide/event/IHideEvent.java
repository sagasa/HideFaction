package hide.event;

import net.minecraft.server.MinecraftServer;

public interface IHideEvent {
	/**登録名*/
	String getName();

	void init(MinecraftServer server);

	void start();

	void update();

	void end();

	void load(String json);

	String save();



	/***/

}
