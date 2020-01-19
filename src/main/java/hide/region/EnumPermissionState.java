package hide.region;

public enum EnumPermissionState {
	ALLOW, DENY, NONE;
	/***/
	public EnumPermissionState returnIfNone(EnumPermissionState state) {
		if(state == EnumPermissionState.NONE)
			return this;
		return state;
	}
}
