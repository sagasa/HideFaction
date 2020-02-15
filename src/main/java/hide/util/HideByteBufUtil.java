package hide.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3i;

public class HideByteBufUtil {

	public static void writeVec3i(ByteBuf buf, Vec3i vec) {
		buf.writeInt(vec.getX());
		buf.writeInt(vec.getY());
		buf.writeInt(vec.getZ());
	}

	public static Vec3i readVec3i(ByteBuf buf) {
		return new Vec3i(buf.readInt(), buf.readInt(), buf.readInt());
	}
}
