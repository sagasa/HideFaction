package hide.region;

import hide.util.IEnumIndex;

public enum EnumRegionPermission implements IEnumIndex {
	/** ブロック破壊 */
	BlockDestroy,
	/** プレイヤー間のダメージの可否 */
	PvP,
	/** プレイヤーへのダメージの可否PvPを除く */
	PlayerDamage,
	/** チェスト以外のブロックへの右クリック処理 */
	BlockInteract,
	/** チェスト,トラップチェストへのインタラクト */
	ChestInteract,
	/** エンダーチェストへのインタラクト */
	EnderChestInteract,
	/** 爆発によるブロック破壊 //TODO */
	ExplosionDestroy(false),;

	//== index ==
	private byte index;
	private static boolean isInit = false;

	private static void init() {
		byte n = 0;
		for (EnumRegionPermission e : values()) {
			e.index = n;
			n++;
		}
	}

	public byte getIndex() {
		if (!isInit)
			init();
		return index;
	}

	private EnumRegionPermission() {
		this(true);
	}

	private EnumRegionPermission(boolean arrowFromOP) {
		ArrowFromOP = arrowFromOP;
	}

	public final boolean ArrowFromOP;
}
