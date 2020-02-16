package hide.util;

public class HideMath {
	public static double larp(double min, double max, float value) {
		return min + (max - min) * value;
	}

	public static float larp(float min, float max, float value) {
		return min + (max - min) * value;
	}
}
