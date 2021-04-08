package hide.event.gui;

import static hide.event.gui.CapWarGuiUtil.*;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import hide.capture.CaptureManager.CountMap;
import hide.event.CaptureWar;
import hide.event.CaptureWar.CapPointData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCapState {

	private class PointState {
		public PointState(String name) {
			this.name = name;
		}

		String name;
		String current;
		float progress;
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

	public void draw(ScaledResolution resolution, int guiCount) {
		final int width = resolution.getScaledWidth();
		final int height = resolution.getScaledHeight();

		final int xCenter = width / 2;
		final int yCenter = 12 + guiCount * 30;

		drawCapPointState(width / 2, yCenter + 15);

		drawStringProgress(xCenter / 2, yCenter, xCenter / 2, FRIEND, myPoint, myString);
		drawStringProgress(xCenter / 2 * 3, yCenter, xCenter / 2, ENEMY, enemyPoint, enemyString);
	}

	private void drawStringProgress(int centerX, int centerY, int width, int color, float progress, String str) {
		int left = centerX - width / 2;
		int right = centerX + width / 2;
		Minecraft mc = Minecraft.getMinecraft();
		int top = centerY - mc.fontRenderer.FONT_HEIGHT / 2 - 1;
		int bottom = centerY + mc.fontRenderer.FONT_HEIGHT / 2 + 1;
		Gui.drawRect(left - 2, top - 2, right + 2, bottom + 2, 0xFF444444);
		drawProgress(left, top, right, bottom, NORMAL, color, progress);
		drawStringCenter(mc.fontRenderer, str, centerX, centerY, 0xFFFFFFFF);
	}

	private static final int WarCenterGap = 80;

	private static final int StateWidth = 60;
	private static final int StateGap = 10;

	private void drawCapPointState(int x, int y) {
		Minecraft mc = Minecraft.getMinecraft();
		int up = y + mc.fontRenderer.FONT_HEIGHT / 2;
		int down = y - mc.fontRenderer.FONT_HEIGHT / 2;

		int left = x - state.length * (StateWidth + StateGap) / 2;
		left += StateGap / 2;
		for (PointState pointState : state) {
			Gui.drawRect(left, up + 1, left + StateWidth, down - 1, 0xFF444444);
			drawProgress(left, up, left + StateWidth, down, NORMAL, pointState.color, pointState.progress);
			drawStringCenter(mc.fontRenderer, pointState.name, left + StateWidth / 2, y, 0xFFFFFFFF);
			left += StateGap + StateWidth;
		}
	}

	private static void drawStringCenter(FontRenderer render, String str, int x, int y, int color) {
		int width = render.getStringWidth(str);
		render.drawString(str, x - width / 2, y - render.FONT_HEIGHT / 2, color);
	}

	private static final String ViewKey = "hide.gui.capstate.view";
	private static final String ViewNone = "hide.gui.capstate.viewnone";

	private float myPoint;
	private String myString;

	private float enemyPoint;
	private String enemyString;

	CountMap<String> pointMap;

	public void updateScoreState(CountMap<String> map) {
		pointMap = map;
	}

	public void updateScoreState(String myTeam) {
		if (pointMap.size() < 2) {
			// 1件or0件
			if (pointMap.containsKey(myTeam)) {
				myString = I18n.format(ViewKey, myTeam, pointMap.get(myTeam), 1);
				myPoint = 1;
				enemyString = I18n.format(ViewNone);
				enemyPoint = 0;
			} else {
				myString = I18n.format(ViewKey, myTeam, 0, 2);
				myPoint = 0;

				Entry<String, Integer> big = pointMap.biggest();
				if (big != null) {
					enemyString = I18n.format(I18n.format(ViewKey, big.getKey(), big.getValue(), 1));
					enemyPoint = 1;
				} else {
					enemyString = I18n.format(ViewNone);
					enemyPoint = 0;
				}
			}
		} else {
			List<String> list = pointMap.entrySet().stream().sorted((e0, e1) -> e1.getValue() - e0.getValue())
					.map(entry -> entry.getKey()).collect(Collectors.toList());
			float total = pointMap.values().stream().collect(Collectors.summingInt(count -> count));

			if (pointMap.containsKey(myTeam)) {
				myString = I18n.format(I18n.format(ViewKey, myTeam, pointMap.get(myTeam), list.indexOf(myTeam) + 1));
				myPoint = pointMap.getNum(myTeam) / total;
			} else {
				myString = I18n.format(ViewKey, myTeam, 0, pointMap.size() + 1);
				myPoint = 0;
			}

			String enemy;
			// 1位ならenemyに2ndを
			if (list.indexOf(myTeam) == 0)
				enemy = list.get(1);
			else
				enemy = list.get(0);

			enemyString = I18n.format(I18n.format(ViewKey, enemy, pointMap.get(enemy), list.indexOf(enemy) + 1));
			enemyPoint = pointMap.getNum(enemy) / total;
		}
	}

	public void updateState(String myteam) {
		for (PointState pointState : state)
			pointState.color = getColor(pointState.current, myteam);
	}

	public void updateState(int index, float progress, String current, String myteam) {
		state[index].progress = progress;
		state[index].current = current;
		state[index].color = getColor(current, myteam);
	}

}
