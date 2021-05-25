package hide.event.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityTarget extends EntityLiving implements IEntityAdditionalSpawnData {

	public EntityTarget(World worldIn) {
		super(worldIn);
	}

	/** 消えるまでの時間 */
	private int life = 100;
	public float r, g, b, size;

	public EntityTarget(World w, double x, double y, double z, float r, float g, float b) {
		this(w);
		this.r = r;
		this.g = g;
		this.b = b;
		setPosition(x, y, z);
	}

	public EntityTarget setSize(float s) {
		size = s;
		setSize(size, size);
		return this;
	}

	@Override
	public boolean isInRangeToRender3d(double x, double y, double z) {
		double d0 = this.posX - x;
		double d2 = this.posZ - z;
		double d3 = d0 * d0 + d2 * d2;
		return this.isInRangeToRenderDist(d3);
	}

	@Override
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 800;
	}

	@Override
	public void onUpdate() {
		life--;
		if (life < 0)
			setDead();

		if (!this.world.isRemote) {
			WorldServer worldserver = (WorldServer) world;
			worldserver.spawnParticle(EnumParticleTypes.SMOKE_LARGE, true, posX, posY, posZ, 5, 0.0, 0.0, 0.0, 1.0);
		}

		System.out.println("UPDATE " + posX + " " + posY + " " + posZ);
	}

	@Override
	public void writeSpawnData(ByteBuf buf) {
		buf.writeFloat(r);
		buf.writeFloat(g);
		buf.writeFloat(b);
		buf.writeFloat(size);
	}

	@Override
	public void readSpawnData(ByteBuf buf) {
		r = buf.readFloat();
		g = buf.readFloat();
		b = buf.readFloat();
		size = buf.readFloat();
		this.setSize(size, size);
	}
}