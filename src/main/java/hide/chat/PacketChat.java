package hide.chat;

import static hide.chat.HideChatSystem.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableSet;

import hide.chat.HideChatSystem.ChatChannel;
import hide.chat.gui.GuiHideNewChat;
import hide.core.HideFaction;
import hide.core.util.BufUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketChat implements IMessage, IMessageHandler<PacketChat, IMessage> {

	public PacketChat() {
	}

	private static final byte CLEAR = 0;
	private static final byte NEWCHAT = 1;
	private static final byte CHUNK_NEXT = 2;
	private static final byte CHUNK_PREV = 3;
	private static final byte REQ_NEXT = 4;
	private static final byte REQ_PREV = 5;

	protected byte mode;

	protected PacketChat(byte mode) {
		this.mode = mode;
	}

	private Collection<ImmutablePair<ChatChannel, String>> channel;
	private int from;
	private int id;

	public static PacketChat reqNext(ImmutableSet<ImmutablePair<ChatChannel, String>> channel, int from, int id) {
		PacketChat data = new PacketChat(REQ_NEXT);
		data.channel = channel;
		data.from = from;
		data.id = id;
		return data;
	}

	public static PacketChat reqPrev(ImmutableSet<ImmutablePair<ChatChannel, String>> channel, int from, int id) {
		PacketChat data = new PacketChat(REQ_PREV);
		data.channel = channel;
		data.from = from;
		data.id = id;
		return data;
	}

	public static PacketChat clearChat() {
		PacketChat data = new PacketChat(CLEAR);
		return data;
	}

	private HideChatLine newLine;

	public static PacketChat newChat(HideChatLine line) {
		PacketChat data = new PacketChat(NEWCHAT);
		data.newLine = line;
		return data;
	}

	private List<HideChatLine> SOldChats;
	private HideChatLine[] COldChat;
	private int COldChatSize;

	/**新しいチャットから順に*/
	public static PacketChat chatChunkNext(List<HideChatLine> lines, int id) {
		PacketChat data = new PacketChat(CHUNK_NEXT);
		data.SOldChats = lines;
		data.id = id;
		return data;
	}

	/**新しいチャットから順に*/
	public static PacketChat chatChunkPrev(List<HideChatLine> lines, int id) {
		PacketChat data = new PacketChat(CHUNK_PREV);
		data.SOldChats = lines;
		data.id = id;
		return data;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(mode);
		if (mode == NEWCHAT) {
			newLine.toBytes(buf);
		} else if (mode == CHUNK_NEXT || mode == CHUNK_PREV) {
			buf.writeInt(id);
			buf.writeByte(SOldChats.size());
			for (HideChatLine line : SOldChats) {
				line.toBytes(buf);
			}
		} else if (mode == REQ_NEXT || mode == REQ_PREV) {
			buf.writeInt(id);
			buf.writeInt(from);
			buf.writeByte(channel.size());
			for (Pair<ChatChannel, String> pair : channel) {
				buf.writeByte(pair.getLeft().getIndex());
				BufUtil.writeString(buf, pair.getRight());
			}
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		mode = buf.readByte();
		if (mode == NEWCHAT) {
			newLine = new HideChatLine();
			newLine.fromBytes(buf);
		} else if (mode == CHUNK_NEXT || mode == CHUNK_PREV) {
			id = buf.readInt();
			//チャンクサイズの配列を作って後詰めで書き込み
			COldChat = new HideChatLine[CHUNK_SIZE];
			int size = buf.readByte();
			COldChatSize = size;
			for (int i = CHUNK_SIZE - size; i < CHUNK_SIZE; i++) {
				HideChatLine line = new HideChatLine();
				line.fromBytes(buf);
				COldChat[i] = line;
			}
		} else if (mode == REQ_NEXT || mode == REQ_PREV) {
			id = buf.readInt();
			from = buf.readInt();
			channel = new ArrayList<>();
			int size = buf.readByte();
			for (int i = 0; i < size; i++) {
				channel.add(new ImmutablePair<>(ChatChannel.values()[buf.readByte()], BufUtil.readString(buf)));
			}
		}
	}

	@Override
	public IMessage onMessage(PacketChat msg, MessageContext ctx) {
		// 受信したデータで上書き
		if (ctx.side == Side.CLIENT) {
			onMsg(msg);
		} else {
			ctx.getServerHandler().player.getServer().addScheduledTask(() -> {
				EntityPlayerMP player = ctx.getServerHandler().player;
				switch (msg.mode) {
				case REQ_NEXT:
					HideFaction.NETWORK.sendTo(chatChunkNext(HideChatDB.getNextChatChunk(ctx.getServerHandler().player, msg.channel, msg.from), msg.id), player);
					//System.out.println("新着 チャット送信 ");
					break;
				case REQ_PREV:
					HideFaction.NETWORK.sendTo(chatChunkPrev(HideChatDB.getPrevChatChunk(ctx.getServerHandler().player, msg.channel, msg.from), msg.id), player);
					//System.out.println("過去 チャット送信 ");
					break;
				default:
					break;
				}
			});
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	private void onMsg(PacketChat msg) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			GuiHideNewChat chat = (GuiHideNewChat) Minecraft.getMinecraft().ingameGUI.getChatGUI();
			switch (msg.mode) {
			case NEWCHAT:
				log.debug("receive new chat "+msg.newLine);
				chat.addNewChatLine(msg.newLine);
				break;
			case CHUNK_NEXT:
				log.debug("receive old chat " + msg.id);
				chat.addChatChunk(msg.COldChat, msg.COldChatSize, msg.id, true);
				break;
			case CHUNK_PREV:
				log.debug("receive old chat " + msg.id);
				chat.addChatChunk(msg.COldChat, msg.COldChatSize, msg.id, false);
				break;
			case CLEAR:
				chat.clearChatMessages(true);
				break;
			default:
				break;
			}
		});
	}
}
