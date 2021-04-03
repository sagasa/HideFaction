package hide.util;

import java.util.Map.Entry;

import hide.capture.CaptureManager.CountMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class HideByteBufUtil {

	public static void writeVec3i(ByteBuf buf, Vec3i vec) {
		buf.writeInt(vec.getX());
		buf.writeInt(vec.getY());
		buf.writeInt(vec.getZ());
	}

	public static Vec3i readVec3i(ByteBuf buf) {
		return new Vec3i(buf.readInt(), buf.readInt(), buf.readInt());
	}

	public static void writeCountMap(ByteBuf buf, CountMap<String> map) {
		buf.writeByte(map.size());
		for (Entry<String, Integer> entry : map.entrySet()) {
			ByteBufUtils.writeUTF8String(buf, entry.getKey());
			buf.writeInt(entry.getValue());
		}
	}

	public static void readCountMap(ByteBuf buf, CountMap<String> map) {
		int size = buf.readByte();
		map.clear();
		for (int i = 0; i < size; i++) {
			map.put(ByteBufUtils.readUTF8String(buf), buf.readInt());
		}
	}
}
