package hide.region.network;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import hide.region.EnumPermissionState;
import hide.region.EnumRegionPermission;
import hide.region.RegionManager;
import hide.region.RegionRect;
import hide.region.RegionRule;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketRegionData implements IMessage, IMessageHandler<PacketRegionData, IMessage> {

	public PacketRegionData() {
	}

	private static final byte DEFAULT_RULE = 0;
	private static final byte REGION_LIST = 1;
	private static final byte RULE_MAP = 2;

	private byte mode;

	public PacketRegionData(byte mode) {
		this.mode = mode;
	}

	private Map<EnumRegionPermission, EnumPermissionState> defaultMap;

	public static PacketRegionData defaultRule(RegionManager manager) {
		PacketRegionData data = new PacketRegionData(DEFAULT_RULE);
		data.defaultMap = manager.DefaultPermission;
		return data;
	}

	private List<RegionRect> regionList;

	public static PacketRegionData regionList(RegionManager manager) {
		PacketRegionData data = new PacketRegionData(REGION_LIST);
		data.regionList = manager.RegionList;
		return data;
	}

	private Map<String, RegionRule> ruleMap;

	public static PacketRegionData ruleMap(RegionManager manager) {
		PacketRegionData data = new PacketRegionData(RULE_MAP);
		data.ruleMap = manager.RuleMap;
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
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		mode = buf.readByte();
		if (mode == DEFAULT_RULE) {
			byte size = buf.readByte();
			defaultMap = new EnumMap<>(EnumRegionPermission.class);
			for (int i = 0; i < size; i++) {
				defaultMap.put(EnumRegionPermission.values()[buf.readByte()], EnumPermissionState.values()[buf.readByte()]);
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
		}
	}

	@Override
	public IMessage onMessage(PacketRegionData msg, MessageContext ctx) {
		System.out.println("RRRRRRR " + Thread.currentThread() + " " + msg.mode);
		//受信したデータで上書き
		if (ctx.side == Side.CLIENT) {
			switch (msg.mode) {
			case REGION_LIST:
				RegionManager.getManager(Minecraft.getMinecraft().world).RegionList = msg.regionList;
				RegionManager.getManager(Minecraft.getMinecraft().world).registerRegionMap();
				System.out.println("OVER "+msg.regionList);
				break;

			default:
				break;
			}
		}
		return null;
	}

}
