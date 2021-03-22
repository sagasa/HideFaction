package hide.core;

import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.relauncher.Side;

/**イベントバスに登録される*/
public interface IHideSubSystem {
	/**登録時に呼ばれる*/
	void init(Side side);

	default void serverStart(FMLServerStartingEvent event) {}
	default void serverStop(FMLServerStoppedEvent event) {}
}
