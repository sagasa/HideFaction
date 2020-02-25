package hide.region.network;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import hide.core.HideFaction;
import hide.region.EnumPermissionState;
import hide.region.EnumRegionPermission;
import hide.region.RegionManager;
import hide.region.RegionRect;
import hide.region.RegionRule;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 変更配信パケット クライアントから受信した場合他のプレイヤーに配信 */
public class PacketRegionEdit implements IMessage, IMessageHandler<PacketRegionEdit, IMessage> {

	private static final byte MASK_OPERATOR = 0xF;
	private static final byte ADD = 0x0;
	private static final byte REMOVE = 0x1;
	private static final byte EDIT = 0x2;

	private static final byte MASK_TARGET = 0x70;
	private static final byte DEFAULT_RULE = 0x00;
	private static final byte RULE_MAP = 0x10;
	private static final byte REGION_LIST = 0x20;

	public PacketRegionEdit() {
	}

	private byte mode;

	public PacketRegionEdit(byte mode) {
		this.mode = mode;
	}

	private Map<EnumRegionPermission, EnumPermissionState> defaultRule;

	public static PacketRegionEdit editDefaultRule(Map<EnumRegionPermission, EnumPermissionState> value) {
		PacketRegionEdit packet = new PacketRegionEdit((byte) (EDIT | DEFAULT_RULE));
		packet.defaultRule = value;
		return packet;
	}

	private String name;
	private RegionRule rule;

	/** EditもAddで */
	public static PacketRegionEdit addRule(String name, RegionRule value) {
		PacketRegionEdit packet = new PacketRegionEdit((byte) (ADD | RULE_MAP));
		packet.name = name;
		packet.rule = value;
		return packet;
	}

	public static PacketRegionEdit removeRule(String name) {
		PacketRegionEdit packet = new PacketRegionEdit((byte) (REMOVE | RULE_MAP));
		packet.name = name;
		return packet;
	}

	private byte index;
	private RegionRect region;

	public static PacketRegionEdit editRegion(byte index, RegionRect value) {
		PacketRegionEdit packet = new PacketRegionEdit((byte) (EDIT | REGION_LIST));
		packet.index = index;
		packet.region = value;
		return packet;
	}

	public static PacketRegionEdit addRegion(RegionRect value) {
		PacketRegionEdit packet = new PacketRegionEdit((byte) (ADD | REGION_LIST));
		packet.region = value;
		return packet;
	}

	public static PacketRegionEdit removeRegion(byte index) {
		PacketRegionEdit packet = new PacketRegionEdit((byte) (REMOVE | REGION_LIST));
		packet.index = index;
		return packet;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(mode);
		if ((mode & MASK_TARGET) == DEFAULT_RULE) {
			buf.writeByte(defaultRule.size());
			for (Entry<EnumRegionPermission, EnumPermissionState> entry : defaultRule.entrySet()) {
				buf.writeByte(entry.getKey().getIndex());
				buf.writeByte(entry.getValue().getIndex());
			}
		} else if ((mode & MASK_TARGET) == RULE_MAP) {
			ByteBufUtils.writeUTF8String(buf, name);
			// remove時はvalueは無い
			if ((mode & MASK_OPERATOR) != REMOVE)
				rule.toBytes(buf);
		} else if ((mode & MASK_TARGET) == REGION_LIST) {
			// add時はindexは無い
			if ((mode & MASK_OPERATOR) != ADD) {
				buf.writeByte(index);
			}
			// remove時はvalueは無い
			if ((mode & MASK_OPERATOR) != REMOVE) {
				region.toBytes(buf);
			}
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		mode = buf.readByte();
		if ((mode & MASK_TARGET) == DEFAULT_RULE) {
			byte size = buf.readByte();
			defaultRule = new EnumMap<>(EnumRegionPermission.class);
			for (int i = 0; i < size; i++) {
				defaultRule.put(EnumRegionPermission.values()[buf.readByte()],
						EnumPermissionState.values()[buf.readByte()]);
			}
		} else if ((mode & MASK_TARGET) == RULE_MAP) {
			name = ByteBufUtils.readUTF8String(buf);
			// remove時はvalueは無い
			if ((mode & MASK_OPERATOR) != REMOVE) {
				rule = new RegionRule();
				rule.fromBytes(buf);
			}
		} else if ((mode & MASK_TARGET) == REGION_LIST) {
			// add時はindexは無い
			if ((mode & MASK_OPERATOR) != ADD) {
				index = buf.readByte();
			}
			// remove時はvalueは無い
			if ((mode & MASK_OPERATOR) != REMOVE) {
				region = new RegionRect();
				region.fromBytes(buf);
			}
		}
	}

	@Override
	public IMessage onMessage(PacketRegionEdit msg, MessageContext ctx) {
		// サーバー側で受信したら全体に返信

		if (ctx.side == Side.SERVER) {
			EntityPlayer player = ctx.getServerHandler().player;
			RegionManager rm = RegionManager.getManager(player.dimension, Side.SERVER);
			player.getServer().addScheduledTask(() -> {
				if ((mode & MASK_TARGET) == DEFAULT_RULE) {
					rm.DefaultPermission = msg.defaultRule;
				} else if ((mode & MASK_TARGET) == RULE_MAP) {
					if ((mode & MASK_OPERATOR) == ADD)
						rm.RuleMap.put(name, rule);
					else if ((mode & MASK_OPERATOR) == REMOVE)
						rm.RuleMap.remove(name);
					rm.registerRegionMap();
				} else if ((mode & MASK_TARGET) == REGION_LIST) {
					if ((mode & MASK_OPERATOR) == EDIT)
						rm.RegionList.set(index, region);
					else if ((mode & MASK_OPERATOR) == ADD)
						rm.RegionList.add(region);
					else if ((mode & MASK_OPERATOR) == REMOVE)
						rm.RegionList.remove(index);
					rm.registerRegionMap();
				}
				// 受信元以外に配信
				for (EntityPlayer p : player.world.playerEntities)
					if (p != player)
						HideFaction.NETWORK.sendTo(msg, (EntityPlayerMP) p);
			});
		} else {
			onMsg(msg);
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	private void onMsg(PacketRegionEdit msg) {
		RegionManager rm = RegionManager.getManager();
		Minecraft.getMinecraft().addScheduledTask(() -> {
			if ((mode & MASK_TARGET) == DEFAULT_RULE) {
				rm.DefaultPermission = msg.defaultRule;
			} else if ((mode & MASK_TARGET) == RULE_MAP) {
				if ((mode & MASK_OPERATOR) == ADD)
					rm.RuleMap.put(name, rule);
				else if ((mode & MASK_OPERATOR) == REMOVE)
					rm.RuleMap.remove(name);
				rm.registerRegionMap();
			} else if ((mode & MASK_TARGET) == REGION_LIST) {
				if ((mode & MASK_OPERATOR) == EDIT)
					rm.RegionList.set(index, region);
				else if ((mode & MASK_OPERATOR) == ADD)
					rm.RegionList.add(region);
				else if ((mode & MASK_OPERATOR) == REMOVE)
					rm.RegionList.remove(index);
				rm.registerRegionMap();
			}
		});
	}
}
