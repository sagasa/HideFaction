package hide.region.network;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import hide.core.HideFaction;
import hide.region.EnumPermissionState;
import hide.region.EnumRegionPermission;
import hide.region.RegionHolder;
import hide.region.RegionRect;
import hide.region.RegionRule;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketRegionData implements IMessage, IMessageHandler<PacketRegionData, IMessage> {

	public PacketRegionData() {
	}

	private static final byte DEFAULT_RULE = 1;
	private static final byte REGION_LIST = 2;
	private static final byte RULE_MAP = 3;
	private static final byte OP_ADD = 4;
	private static final byte OP_REMOVE = 5;

	private byte mode;

	public PacketRegionData(byte mode) {
		this.mode = mode;
	}

	private EnumMap<EnumRegionPermission, EnumPermissionState> defaultMap;

	public static PacketRegionData defaultRule(RegionHolder manager) {
		PacketRegionData data = new PacketRegionData(DEFAULT_RULE);
		data.defaultMap = manager.DefaultPermission;
		return data;
	}

	private List<RegionRect> regionList;

	public static PacketRegionData regionList(RegionHolder manager) {
		PacketRegionData data = new PacketRegionData(REGION_LIST);
		data.regionList = manager.RegionList;
		return data;
	}

	private Map<String, RegionRule> ruleMap;

	public static PacketRegionData ruleMap(RegionHolder manager) {
		PacketRegionData data = new PacketRegionData(RULE_MAP);
		data.ruleMap = manager.RuleMap;
		return data;
	}

	private UUID uuid;

	public static PacketRegionData addOP(UUID player) {
		PacketRegionData data = new PacketRegionData(OP_ADD);
		data.uuid = player;
		return data;
	}

	public static PacketRegionData removeOP(UUID player) {
		PacketRegionData data = new PacketRegionData(OP_REMOVE);
		data.uuid = player;
		return data;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(mode);
		if (mode == DEFAULT_RULE) {
			buf.writeByte(defaultMap.size());
			for (Entry<EnumRegionPermission, EnumPermissionState> entry : defaultMap.entrySet()) {
				buf.writeByte(entry.getKey().getIndex());
				buf.writeByte(entry.getValue().getIndex());
			}
		} else if (mode == REGION_LIST) {
			buf.writeByte(regionList.size());
			for (RegionRect rg : regionList) {
				rg.toBytes(buf);
			}
		} else if (mode == RULE_MAP) {
			buf.writeByte(ruleMap.size());
			for (Entry<String, RegionRule> entry : ruleMap.entrySet()) {
				ByteBufUtils.writeUTF8String(buf, entry.getKey());
				entry.getValue().toBytes(buf);
			}
		}else if (mode == OP_ADD||mode == OP_REMOVE) {
			buf.writeLong(uuid.getMostSignificantBits());
			buf.writeLong(uuid.getLeastSignificantBits());
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		mode = buf.readByte();
		if (mode == DEFAULT_RULE) {
			byte size = buf.readByte();
			defaultMap = new EnumMap<>(EnumRegionPermission.class);
			for (int i = 0; i < size; i++) {
				defaultMap.put(EnumRegionPermission.values()[buf.readByte()],
						EnumPermissionState.values()[buf.readByte()]);
			}
		} else if (mode == REGION_LIST) {
			byte size = buf.readByte();
			regionList = new ArrayList<>();
			for (int i = 0; i < size; i++) {
				RegionRect rg = new RegionRect();
				rg.fromBytes(buf);
				regionList.add(rg);
			}
		} else if (mode == RULE_MAP) {
			byte size = buf.readByte();
			ruleMap = new HashMap<>();
			for (int i = 0; i < size; i++) {
				String name = ByteBufUtils.readUTF8String(buf);
				RegionRule rule = new RegionRule();
				rule.fromBytes(buf);
				ruleMap.put(name, rule);
			}
		}else if (mode == OP_ADD||mode == OP_REMOVE) {
			uuid = new UUID(buf.readLong(), buf.readLong());
		}
	}

	@Override
	public IMessage onMessage(PacketRegionData msg, MessageContext ctx) {
		// 受信したデータで上書き
		if (ctx.side == Side.CLIENT) {
			onMsg(msg);
		} // TODO サーバー側で受信時に全員に配信
		return null;
	}

	@SideOnly(Side.CLIENT)
	private void onMsg(PacketRegionData msg) {
		if (!Minecraft.getMinecraft().isIntegratedServerRunning())
			switch (msg.mode) {
			case REGION_LIST:
				RegionHolder.getManager().RegionList = msg.regionList;
				RegionHolder.getManager().registerRegionMap();
				HideFaction.log.info("receive RegionList from server");
				break;
			case DEFAULT_RULE:
				RegionHolder.getManager().DefaultPermission = msg.defaultMap;
				RegionHolder.getManager().registerRegionMap();
				HideFaction.log.info("receive DefaultRule from server");
				break;
			case RULE_MAP:
				RegionHolder.RuleMap = msg.ruleMap;
				RegionHolder.getManager().registerRegionMap();
				HideFaction.log.info("receive RuleMap from server");
				break;
			case OP_ADD:
				RegionHolder.OPPlayers.add(msg.uuid);
				break;
			case OP_REMOVE:
				RegionHolder.OPPlayers.remove(msg.uuid);
				break;
			default:
				break;
			}
	}
}
