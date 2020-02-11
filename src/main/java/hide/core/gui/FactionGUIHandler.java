package hide.core.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class FactionGUIHandler implements IGuiHandler {

	private static int pointer = -1;

	private static List<HideGuiProvider> guiProviders = new ArrayList<>();

	/**プロバイダーを追加してIDを得る*/
	public static int register(HideGuiProvider provider) {
		if(provider==null)
			throw new NullPointerException("null is not allowed to register");
		guiProviders.add(provider);
		pointer++;
		return pointer;
	}

	public interface HideGuiProvider{
		public Object getServerGuiElement(EntityPlayer player, World world, int x, int y, int z) ;
		public Object getClientGuiElement(EntityPlayer player, World world, int x, int y, int z) ;
	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		System.out.println("id = "+id +" list = "+guiProviders);
		if (0<=id&&id<=pointer) {
			return guiProviders.get(id).getServerGuiElement(player, world, x, y, z);
		}
		return null;
	}

	/* クライアント側の処理 */
	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		System.out.println("id = "+id +" list = "+guiProviders);
		if (0<=id&&id<=pointer) {
			return guiProviders.get(id).getClientGuiElement(player, world, x, y, z);
		}
		return null;
	}
}
