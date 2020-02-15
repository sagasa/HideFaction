package hide.faction;

import hide.util.IEnumIndex;

public enum FactionRank implements IEnumIndex {
	Leader, SubLeader, Member, Temporary;

	private byte index;

	private static boolean isInit = false;

	private static void init() {
		byte n = 0;
		for (FactionRank e : values()) {
			e.index = n;
			n++;
		}
	}

	public byte getIndex() {
		if (!isInit)
			init();
		return index;
	}
}
