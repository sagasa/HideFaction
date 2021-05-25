package hide.event.entity;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTarget extends Render {

	public RenderTarget(RenderManager renderManager) {
		super(renderManager);
	}

	static FloatBuffer fb = BufferUtils.createFloatBuffer(16);

	public void render(EntityTarget aabb, double d, double d1, double d2, float f, float f1) {
		GL11.glPushMatrix();
		GL11.glTranslatef((float) d, (float) d1, (float) d2);

		GlStateManager.disableDepth();
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();

		// モデルビュー変換行列の保存
		GL11.glPushMatrix();
		{
			// モデルビュー変換行列の操作用
			fb.position(0);
			// 現在のモデルビュー変換行列を取り出す
			GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, fb);

			float[] m = new float[16];
			fb.get(m);
			// 左上 3x3 要素を単位行列にする
			m[0] = m[5] = m[10] = 1.0f;
			m[1] = m[2] = m[4] = m[6] = m[8] = m[9] = 0.0f;

			fb.position(0);
			fb.put(m);
			fb.position(0);
			// 書き換えた行列を書き戻す
			GL11.glLoadMatrix(fb);

			// オブジェクトの描画
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buf = tessellator.getBuffer();
			GL11.glLineWidth(3);
			RenderGlobal.drawBoundingBox(0, 0, 0, 1, 1, 1, aabb.r, aabb.g, aabb.b, 0.5f);
		}
		// モデルビュー変換行列の復帰
		GL11.glPopMatrix();

		// GL11.glColor4ub(1,0, 0, 0.2F);

		//buf.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		//RenderGlobal.addChainedFilledBoxVertices(buf, 0, 0, 0, aabb.aabb.maxX - aabb.aabb.minX, aabb.aabb.maxY - aabb.aabb.minY, aabb.aabb.maxZ - aabb.aabb.minZ, aabb.r, aabb.g, aabb.b, 0.2f);
		//tessellator.draw();

		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();

		GlStateManager.enableDepth();
		GL11.glPopMatrix();
	}

	@Override
	public void doRender(Entity entity, double d, double d1, double d2, float f, float f1) {
		render((EntityTarget) entity, d, d1, d2, f, f1);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}
}