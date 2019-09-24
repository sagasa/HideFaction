package hide.faction.territory.permission;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

public class TerritoryPermissions {
	private Gson gson = new Gson();
	Map<String, Map<PermissionType, Boolean>> map = new HashMap<>();

	public String toJson() {
		return gson.toJson(map);
	}

	public TerritoryPermissions(String gsonStr) {
		map = gson.fromJson(gsonStr, HashMap.class);
	}



}
