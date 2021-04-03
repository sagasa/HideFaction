package hide.event.gui;

import hide.capture.CaptureManager.CountMap;
import hide.event.CaptureWar;
import hide.event.CaptureWar.CapPointData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCapState {

	private class PointState {
		public PointState(String name) {
			this.name = name;
		}

		String name;
		int color = 0xFF666666;
	}

	PointState[] state;

	public GuiCapState(CaptureWar war) {
		CapPointData[] array = war.get(CaptureWar.CapRegion);
		state = new PointState[array.length];
		for (int i = 0; i < array.length; i++) {
			state[i] = new PointState(array[i].get(CapPointData.Name));
		}
	}

	public void draw(ScaledResolution resolution) {
		final int width = resolution.getScaledWidth();
		final int height = resolution.getScaledHeight();

		final int xCenter = width / 2;

		drawCapPointState(width / 2, 40);

		drawRect2Color(xCenter/2, 20, xCenter/2, 26, 0xFFFF4444, 0xFF444444);
	}

	private void drawRect2Color(int centerX, int centerY, int width, int height, int inColor, int outColor) {
		int left = centerX - width / 2;
		int right = centerX + width / 2;
		int top = centerY - height / 2;
		int bottom = centerY + height / 2;
		Gui.drawRect(left, top, right, bottom, outColor);
		Gui.drawRect(left + 2, top + 2, right + 2, bottom + 2, inColor);
	}

	private static final int WarCenterGap = 80;

	private static final int StateWidth = 80;
	private static final int StateGap = 10;

	private void drawCapPointState(int x, int y) {
		Minecraft mc = Minecraft.getMinecraft();
		int up = y + mc.fontRenderer.FONT_HEIGHT / 2;
		int down = y - mc.fontRenderer.FONT_HEIGHT / 2;

		int left = x - state.length * (StateWidth + StateGap) / 2;
		left += StateGap / 2;
		for (PointState pointState : state) {
			Gui.drawRect(left, up + 4, left + StateWidth, down - 4, 0xFF444444);
			Gui.drawRect(left, up + 2, left + StateWidth, down - 2, pointState.color);
			drawStringCenter(mc.fontRenderer, pointState.name, left + StateWidth / 2, y, 0xFF000000);
			left += StateGap;
		}
	}

	public static void drawProgress(int left, int top, int right, int bottom, int color_base, int color_over, float progress) {
		Gui.drawRect(left, top, right, bottom, color_base);
		Gui.drawRect(left, top, (int) (left + (right - left) * progress), bottom, color_over);
	}

	private static void drawStringCenter(FontRenderer render, String str, int x, int y, int color) {
		int width = render.getStringWidth(str);
		render.drawString(str, x - width / 2, y - render.FONT_HEIGHT / 2, color);
	}


	public void updateState(String myTeam,CountMap<String> a) {

	}
}
