package hide.event.gui;

import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CapWarGuiUtil {

	static final int ENEMY = 0xFFeb6101;
	static final int FRIEND = 0xFF0000FF;
	static final int NORMAL = 0xFF666666;

	static int getColor(String team, String myTeam) {
		if (team == null)
			return 0xFF333333;
		else if (team.equals(myTeam))
			return FRIEND;
		return ENEMY;
	}

	static void drawProgress(int left, int top, int right, int bottom, int color_base, int color_over, float progress) {
		Gui.drawRect(left, top, right, bottom, color_base);
		Gui.drawRect(left, top, (int) (left + (right - left) * progress), bottom, color_over);
	}
}
