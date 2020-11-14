package hide.capture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

public class CaptureManager {
	List<CapturePoint> cpList = new ArrayList<>();

	public CaptureManager() {
		for (CapturePoint capturePoint : cpList) {

		}
	}

	public void update(World world) {
		world.provider.getDimension();
	}

	public class CapData implements INBTSerializable<NBTTagCompound> {

		Map<String, Integer> point = new HashMap<>();

		@Override
		public NBTTagCompound serializeNBT() {
			// TODO 自動生成されたメソッド・スタブ
			return null;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			// TODO 自動生成されたメソッド・スタブ

		}

	}
}
