package hide.util;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class DrawUtil {

	public static void drawRectOutline() {
	}

	public static void drawRect(double x, double y, double x1, double y1, float red, float green, float blue,
			float alpha) {
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableCull();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();
		buf.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		buf.pos(x, y, 0).color(red, green, blue, alpha).endVertex();
		buf.pos(x1, y, 0).color(red, green, blue, alpha).endVertex();
		buf.pos(x, y1, 0).color(red, green, blue, alpha).endVertex();
		buf.pos(x1, y1, 0).color(red, green, blue, alpha).endVertex();
		tessellator.draw();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.enableCull();
	}
}
