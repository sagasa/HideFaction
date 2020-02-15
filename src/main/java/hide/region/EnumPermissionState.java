package hide.region;

import hide.util.IEnumIndex;

/**indexはbyteだから注意*/
public enum EnumPermissionState implements IEnumIndex {
	ALLOW, DENY, NONE;

	private byte index;

	private static boolean isInit = false;

	private static void init() {
		byte n = 0;
		for (EnumPermissionState e : values()) {
			e.index = n;
			n++;
		}
	}

	public byte getIndex() {
		if (!isInit)
			init();
		return index;
	}

	/**None以外なら渡された値を返す*/
	public EnumPermissionState returnIfNone(EnumPermissionState state) {
		if (state == EnumPermissionState.NONE)
			return this;
		return state;
	}
}
