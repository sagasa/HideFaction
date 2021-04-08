package hide.event.gui;

import static hide.event.gui.CapWarGuiUtil.*;

import hide.event.CaptureWar.CapState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCapProgress implements IToast {

	private long firstDrawTime;
	private boolean hasNewOutputs = true;

	/**侵攻中のチーム*/
	String by;
	/**地名*/
	String where;
	/**識別用tag*/
	String tag;

	String tmp;

	float progress;

	CapState state;


	int color_base;
	int color_over;



	GuiCapProgress setState(String tag, String name, CapState state, String team, String tmp, String myTeam, float progress) {
		this.tag = tag;
		this.by = team;
		this.tmp = tmp;
		this.where = name;
		this.state = state;
		this.progress = progress;
		hasNewOutputs = true;

		color_base = NORMAL;
		color_over = getColor(team, myTeam);

		return this;
	}

	enum Stance {
		Good, Norm, Bad
	}



	public IToast.Visibility draw(GuiToast toastGui, long delta) {

		if (this.hasNewOutputs) {
			this.firstDrawTime = delta;
			this.hasNewOutputs = false;
		}
		Minecraft mc = toastGui.getMinecraft();
		mc.getTextureManager().bindTexture(TEXTURE_TOASTS);
		GlStateManager.color(1.0F, 1.0F, 1.0F);
		toastGui.drawTexturedModalRect(64, 0, 0, 32, 160, 32);

		drawProgress(67, 3, 160, 29, color_base, color_over, progress);

		drawStringCenter(mc.fontRenderer, by, 0, 25, 0xFFDDDDDD);
		drawStringCenter(mc.fontRenderer, tmp, 0, 45, 0xFFDDDDDD);
		drawStringCenter(mc.fontRenderer, state.toString(), 0, 5, 0xFFDDDDDD);

		drawStringCenter(mc.fontRenderer, where, 112, 5, 0xFFDDDDDD);
		drawStringCenter(mc.fontRenderer, state == CapState.Invade ? tmp : by, 112, 27 - mc.fontRenderer.FONT_HEIGHT, 0xFFDDDDDD);

		//Gui.drawRect(3, 28, 157, 29, );
		RenderHelper.enableGUIStandardItemLighting();

		return delta - this.firstDrawTime >= 1000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;

	}

	private void drawStringCenter(FontRenderer render, String str, int x, int y, int color) {
		int width = render.getStringWidth(str);
		render.drawString(str, x - width / 2, y, color);
	}

	@Override
	public Object getType() {
		return tag;
	}

	public static void addOrUpdate(GuiToast guiToast, String tag, String name, CapState state, String team, String tmp, String myTeam, float progress) {
		if (tag == null)
			return;
		GuiCapProgress gui = guiToast.getToast(GuiCapProgress.class, tag);
		if (gui == null) {
			guiToast.add(new GuiCapProgress().setState(tag, name, state, team, tmp, myTeam, progress));
		} else {
			gui.setState(tag, name, state, team, tmp, myTeam, progress);
		}
	}
}
