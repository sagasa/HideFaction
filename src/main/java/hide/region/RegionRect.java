package hide.region;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.util.Strings;
import org.lwjgl.opengl.GL11;

import com.google.gson.annotations.SerializedName;

import hide.core.HideFaction;
import hide.region.RegionHolder.ChunkRegingMap;
import hide.util.DrawUtil;
import hide.util.HideByteBufUtil;
import hide.util.HideMath;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 始点と終点を指定したレギオンの最小単位 */
public class RegionRect implements IMessage {

	@SerializedName("StartPos")
	private Vec3i _start = Vec3i.NULL_VECTOR;
	@SerializedName("EndPos")
	private Vec3i _end = Vec3i.NULL_VECTOR;

	@SerializedName("RuleName")
	private String _ruleName = Strings.EMPTY;

	private transient RegionRule _rule;

	@SerializedName("Tag")
	private String[] _tag = ArrayUtils.EMPTY_STRING_ARRAY;

	/** 2点を設定 チェック掛けるから制約はなし */
	public RegionRect setPos(Vec3i start, Vec3i end) {
		_start = start;
		_end = end;
		checkValue();
		return this;
	}

	public Vec3i getStartPos() {
		return _start;
	}

	public Vec3i getEndPos() {
		return _end;
	}

	public RegionRect setTag(String... name) {
		_tag = name;
		return this;
	}

	public String[] getTag() {
		return _tag;
	}

	public RegionRect setRuleName(String name) {
		_ruleName = name;
		checkValue();
		// 定義があっても代入されていなければ警告
		if (Strings.isNotBlank(_ruleName) && _rule == null)
			HideFaction.getLog().warn("rule [" + _ruleName + "] does not exist");
		return this;
	}

	public String getRuleName() {
		return _ruleName;
	}

	public RegionRule getRule() {
		return _rule;
	}

	public boolean haveRule() {
		return _rule != null;
	}

	/** start<endになるように調整+Ruleの実態を代入 */
	public void checkValue() {
		if (_end.getX() < _start.getX()) {
			Vec3i start = new Vec3i(_end.getX(), _start.getY(), _start.getZ());
			_end = new Vec3i(_start.getX(), _end.getY(), _end.getZ());
			_start = start;
		}
		if (_end.getY() < _start.getY()) {
			Vec3i start = new Vec3i(_start.getX(), _end.getY(), _start.getZ());
			_end = new Vec3i(_end.getX(), _start.getY(), _end.getZ());
			_start = start;
		}
		if (_end.getZ() < _start.getZ()) {
			Vec3i start = new Vec3i(_start.getX(), _start.getY(), _end.getZ());
			_end = new Vec3i(_end.getX(), _end.getY(), _start.getZ());
			_start = start;
		}
		// ルール名があったら探す
		if (Strings.isNotBlank(_ruleName))
			_rule = RegionHolder.RuleMap.get(_ruleName);
		//コリジョン作成
		collision = new AxisAlignedBB(new BlockPos(_start), new BlockPos(_end));
	}

	public boolean contain(Vec3i vec) {
		return _start.getX() <= vec.getX() && vec.getX() < _end.getX() && _start.getY() <= vec.getY()
				&& vec.getY() < _end.getY() && _start.getZ() <= vec.getZ() && vec.getZ() < _end.getZ();
	}

	public void register(ChunkRegingMap chunkMap) {
		int maxX = _end.getX() >> 4;
		int minX = _start.getX() >> 4;
		int maxZ = _end.getX() >> 4;
		int minZ = _start.getX() >> 4;
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				chunkMap.addToMap(new ChunkPos(x, z), this);
			}
		}
	}

	public EnumPermissionState checkPermission(EnumRegionPermission regionPermission, EntityPlayer player) {
		return _rule == null ? EnumPermissionState.NONE : _rule.checkPermission(regionPermission, player);
	}

	private AxisAlignedBB collision;

	private static List<String> drawArray = new ArrayList<>();

	@SideOnly(Side.CLIENT)
	public void drawRegionRect(boolean showInfo, float partialTicks, float r, float g, float b) {
		Minecraft mc = Minecraft.getMinecraft();
		// プレイヤー座標
		double pX = HideMath.larp(mc.player.prevPosX, mc.player.posX, partialTicks) - _start.getX();
		double pY = HideMath.larp(mc.player.prevPosY, mc.player.posY, partialTicks) - _start.getY();
		double pZ = HideMath.larp(mc.player.prevPosZ, mc.player.posZ, partialTicks) - _start.getZ();
		float pYaw = HideMath.larp(mc.player.prevRotationYaw, mc.player.rotationYaw, partialTicks);
		float pPitch = HideMath.larp(mc.player.prevRotationPitch, mc.player.rotationPitch, partialTicks);
		//範囲の座標
		double vX = _end.getX() - _start.getX();
		double vY = _end.getY() - _start.getY();
		double vZ = _end.getZ() - _start.getZ();

		GlStateManager.pushMatrix();

		// startを原点とする座標に
		GlStateManager.translate(-pX, -pY, -pZ);

		// プレイヤーの視点位置に
		pY += mc.player.eyeHeight;

		GlStateManager.disableDepth();

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		// GL11.glColor4ub(1,0, 0, 0.2F);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();
		GL11.glLineWidth(3);
		RenderGlobal.drawBoundingBox(0, 0, 0, vX, vY, vZ, r, g, b, 0.5f);

		GlStateManager.enableDepth();
		buf.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		RenderGlobal.addChainedFilledBoxVertices(buf, -0.01, -0.01, -0.01, vX + 0.01, vY + 0.01, vZ + 0.01, r, g, b, 0.3f);
		tessellator.draw();
		GlStateManager.disableDepth();

		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.enableCull();

		final int textColor = 0xFFFFFF;
		if (showInfo) {
			drawString("Start "+makeVecString(_start), 0, 0, 0, pX, pY, pZ, textColor);
			drawString("End "+makeVecString(_end), vX, vY, vZ, pX, pY, pZ, textColor);

			// 中央に描画するリスト
			drawArray.clear();
			for (String str : _tag) {
				drawArray.add("tag : "+str);
			}
			drawArray.add("RuleName : " + _ruleName);
			if(haveRule()) {
				drawArray.add("Priority : " + _rule.priority);
				if (Strings.isNotBlank(_rule.targetName)) {
					drawArray.add("Target : " + _rule.targetName);
					drawArray.add("Rank : " + _rule.targetRank);
				}
				for (Entry<EnumRegionPermission, EnumPermissionState> entry : _rule.getMap().entrySet()) {
					drawArray.add(entry.getKey() + " : " + entry.getValue());
				}
			}else {
				drawArray.add("Not Set");
			}

			double x = vX / 2;
			double y = vY / 2;
			double z = vZ / 2;

			float space = distance(x, y, z, pX, pY, pZ) / 10;

			y += drawArray.size() / 2 * space;
			for (String str : drawArray) {
				drawString(str, x, y, z, pX, pY, pZ, textColor);
				y -= space;
			}
		}

		GlStateManager.enableDepth();
		GlStateManager.popMatrix();
	}

	private static String makeVecString(Vec3i vec) {
		return "(" + vec.getX() + "," + vec.getY() + "," + vec.getZ() + ")";
	}

	private static float distance(double ix, double iy, double iz, double x, double y, double z) {
		double d0 = ix - x;
		double d1 = iy - y;
		double d2 = iz - z;
		return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
	}

	@SideOnly(Side.CLIENT)
	private static void drawString(String str, double x, double y, double z, double offX, double offY, double offZ,
			int color) {
		Minecraft mc = Minecraft.getMinecraft();
		float scale = distance(x, y, z, offX, offY, offZ) / 150;
		GlStateManager.pushMatrix();

		GlStateManager.translate(x, y, z);
		GlStateManager.rotate(180, 1, 0, 0);
		GlStateManager.rotate(mc.player.rotationYaw + 180, 0, 1, 0);
		GlStateManager.rotate(mc.player.rotationPitch, -1, 0, 0);

		GlStateManager.scale(scale, scale, 1);

		GlStateManager.translate(mc.fontRenderer.getStringWidth(str) / -2, mc.fontRenderer.FONT_HEIGHT / -2, 0);

		DrawUtil.drawRect(-2, -2, mc.fontRenderer.getStringWidth(str) + 1, mc.fontRenderer.FONT_HEIGHT, 0.5f, 0.5f,
				0.5f, 0.4f);
		mc.fontRenderer.drawString(str, 0, 0, color);

		GlStateManager.popMatrix();
	}

	@SideOnly(Side.CLIENT)
	private static void dot(double x, double y, double z) {
		GlStateManager.pushMatrix();

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableCull();

		GL11.glPointSize(10F);
		GlStateManager.glBegin(GL11.GL_POINTS);
		GlStateManager.glVertex3f(0F, 0F, 0F);
		GlStateManager.glEnd();

		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.enableCull();

		GlStateManager.popMatrix();
	}

	/**コリジョン
	 * @param vecA
	 * @param vecB */
	public boolean isHit(Vec3d vecA, Vec3d vecB) {
		return collision.calculateIntercept(vecA, vecB) != null;
	}

	/**内容をコピー チェックもするよ*/
	public void writeFrom(RegionRect rg) {
		_start = rg._start;
		_end = rg._end;
		_ruleName = rg._ruleName;
		_tag = rg._tag;
		checkValue();
	}

	@Override
	public String toString() {
		return "[from : " + _start + ", to : " + _end + ", tag : " + _tag + ", rule : " + _ruleName + "]";
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		_start = HideByteBufUtil.readVec3i(buf);
		_end = HideByteBufUtil.readVec3i(buf);
		_ruleName = ByteBufUtils.readUTF8String(buf);
		int size = buf.readByte();
		if (0 < size) {
			_tag = new String[size];
			for (int i = 0; i < _tag.length; i++)
				_tag[i] = ByteBufUtils.readUTF8String(buf);
		}
		checkValue();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		HideByteBufUtil.writeVec3i(buf, _start);
		HideByteBufUtil.writeVec3i(buf, _end);
		ByteBufUtils.writeUTF8String(buf, _ruleName);
		buf.writeByte(_tag.length);
		for (String str : _tag)
			ByteBufUtils.writeUTF8String(buf, str);
	}
}
