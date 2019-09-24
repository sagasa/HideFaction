package hide.faction.territory.permission;

public enum PermissionType {
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
	ExplosionDestroy,;
}