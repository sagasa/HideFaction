package hide.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import hide.chat.HideChatManager.ChatChannel;
import hide.chat.HideChatManager.ServerChatData;
import hide.core.HideFaction;
import hide.core.HidePlayerDataManager;
import hide.core.HideUtil;
import hide.core.gui.GuiHideNewChat;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketChatState implements IMessage, IMessageHandler<PacketChatState, IMessage> {

	public PacketChatState() {
	}

	private static final byte CHANNEL_SET = 0;
	private static final byte CHANNEL_SEND = 1;

	protected byte mode;

	protected PacketChatState(byte mode) {
		this.mode = mode;
	}

	private ImmutablePair<ChatChannel, String> channel;

	public static PacketChatState cannelSent(ImmutablePair<ChatChannel, String> send) {
		PacketChatState data = new PacketChatState(CHANNEL_SEND);
		data.channel = send;
		return data;
	}

	private Collection<ImmutablePair<ChatChannel, String>> channel_set;

	public static PacketChatState cannelState(Collection<ImmutablePair<ChatChannel, String>> set) {
		PacketChatState data = new PacketChatState(CHANNEL_SET);
		data.channel_set = set;
		return data;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(mode);
		if (mode == CHANNEL_SET) {
			buf.writeByte(channel_set.size());
			for (Pair<ChatChannel, String> pair : channel_set) {
				buf.writeByte(pair.getLeft().getIndex());
				HideUtil.writeString(buf, pair.getRight());
			}
		} else if (mode == CHANNEL_SEND) {
			buf.writeByte(channel.getLeft().getIndex());
			HideUtil.writeString(buf, channel.getRight());
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		mode = buf.readByte();
		if (mode == CHANNEL_SET) {
			channel_set = new ArrayList<>();
			int size = buf.readByte();
			for (int i = 0; i < size; i++) {
				channel_set.add(new ImmutablePair<>(ChatChannel.values()[buf.readByte()], HideUtil.readString(buf)));
			}
		} else if (mode == CHANNEL_SEND) {
			channel = new ImmutablePair<>(ChatChannel.values()[buf.readByte()], HideUtil.readString(buf));
		}
	}

	@Override
	public IMessage onMessage(PacketChatState msg, MessageContext ctx) {
		if (ctx.side == Side.CLIENT) {
			onMsg(msg);
		} else {
			ctx.getServerHandler().player.getServer().addScheduledTask(() -> {
				EntityPlayerMP player = ctx.getServerHandler().player;
				switch (msg.mode) {
				case CHANNEL_SET:
					//検証して保存して返信
					List<ImmutablePair<ChatChannel, String>> list = new ArrayList<>();
					for (ImmutablePair<ChatChannel, String> c : msg.channel_set) {
						if (c.left == ChatChannel.Team)
							c = new ImmutablePair<>(ChatChannel.Team, HideChatManager.getFaction(player));
						list.add(c);
					}
					HideFaction.NETWORK.sendTo(cannelState(list), player);
					break;
				case CHANNEL_SEND:
					HidePlayerDataManager.getServerData(ServerChatData.class, player).sendChannel = msg.channel;
					break;
				default:
					break;
				}
			});
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	private void onMsg(PacketChatState msg) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			GuiHideNewChat chat = (GuiHideNewChat) Minecraft.getMinecraft().ingameGUI.getChatGUI();
			switch (msg.mode) {
			case CHANNEL_SET:

				break;
			default:
				break;
			}
		});
	}
}
