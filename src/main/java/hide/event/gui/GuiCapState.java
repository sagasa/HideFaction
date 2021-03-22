package hide.event.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCapState {

	private class PointState {
		String name;
		int color;
	}

	PointState[] state;

	public void draw(RenderGameOverlayEvent event) {
		final int width = event.getResolution().getScaledWidth();
		final int height = event.getResolution().getScaledHeight();

	}

	private static final int StateWidth = 40;
	private static final int StateGap = 10;

	private void drawCapPointState(int x, int y) {
		Minecraft mc = Minecraft.getMinecraft();
		int up = y + mc.fontRenderer.FONT_HEIGHT / 2;

		int down = y - mc.fontRenderer.FONT_HEIGHT / 2;
		int left = x - state.length * (StateWidth + StateGap) / 2;
		left += StateGap / 2;
		for (PointState pointState : state) {
			Gui.drawRect(left, up, left + StateWidth, down, pointState.color);
			drawStringCenter(mc.fontRenderer, pointState.name, left,y , color);
			left += StateGap;
		}
	}

	private void drawStringCenter(FontRenderer render, String str, int x, int y, int color) {
		int width = render.getStringWidth(str);
		render.drawString(str, x - width / 2, y, color);
	}
}
