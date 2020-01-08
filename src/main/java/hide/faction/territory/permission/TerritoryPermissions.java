package hide.faction.territory.permission;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import hide.region.EnumRegionPermission;

public class TerritoryPermissions {
	private Gson gson = new Gson();
	Map<String, Map<EnumRegionPermission, Boolean>> map = new HashMap<>();

	public String toJson() {
		return gson.toJson(map);
	}

	public TerritoryPermissions(String gsonStr) {
		map = gson.fromJson(gsonStr, HashMap.class);
	}



}
