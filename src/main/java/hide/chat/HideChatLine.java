package hide.chat;

import java.util.List;
import java.util.stream.Collectors;

import hide.chat.HideChatManager.ChatChannel;
import hide.core.HideUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HideChatLine {
	public HideChatLine() {
	}

	/**クライアント側のチャット*/
	@SideOnly(Side.CLIENT)
	public HideChatLine(ITextComponent msg, int id) {
		this(0, null, null, ChatChannel.Info, "", msg);
		ClientID = id;
	}

	public HideChatLine(int id, String time, String sender, ChatChannel channel, String channelName, ITextComponent msg) {
		ID = id;
		Time = time;
		Sender = sender;
		Channel = channel;
		ChannelName = channelName;
		this.msg = msg;
	}

	public HideChatLine(int id, String time, String sender, ChatChannel channel, String channelName, String msg) {
		ID = id;
		Time = time;
		Sender = sender;
		Channel = channel;
		ChannelName = channelName;
		msgJson = msg;
	}

	private HideChatLine(int id, String time, String sender, ChatChannel channel, String channelName, ITextComponent msg, int update, int clinetID) {
		ID = id;
		Time = time;
		Sender = sender;
		Channel = channel;
		ChannelName = channelName;
		this.msg = msg;
		ClientID = clinetID;
		UpdatedCounter = update;
	}

	public int UpdatedCounter;

	public int ID;
	public int ClientID;
	public String Time;
	public ChatChannel Channel;
	public String ChannelName;
	public String Sender;
	private String msgJson;
	private ITextComponent msg;

	@SideOnly(Side.CLIENT)
	public List<HideChatLine> format() {
		Minecraft mc = Minecraft.getMinecraft();
		GuiNewChat gui = mc.ingameGUI.persistantChatGUI;
		int i = MathHelper.floor((float) gui.getChatWidth() / gui.getChatScale());
		List<ITextComponent> list = GuiUtilRenderComponents.splitText(getMsg(), i, mc.fontRenderer, false, false);
		return list.stream().map(newmsg -> new HideChatLine(ID, Time, Sender, Channel, ChannelName, newmsg, UpdatedCounter, ClientID)).collect(Collectors.toList());
	}

	public ITextComponent getMsg() {
		if (msg == null)
			msg = ITextComponent.Serializer.jsonToComponent(msgJson);
		return msg;
	}

	public String getMsgJson() {
		if (msgJson == null)
			msgJson = ITextComponent.Serializer.componentToJson(msg);
		return msgJson;
	}

	public void toBytes(ByteBuf buf) {
		buf.writeInt(ID);
		HideUtil.writeString(buf, Time);
		HideUtil.writeString(buf, Channel.toString());
		HideUtil.writeString(buf, ChannelName);
		HideUtil.writeString(buf, Sender);
		HideUtil.writeString(buf, getMsgJson());
	}

	public void fromBytes(ByteBuf buf) {
		ID = buf.readInt();
		Time = HideUtil.readString(buf);
		Channel = ChatChannel.valueOf(HideUtil.readString(buf));
		ChannelName = HideUtil.readString(buf);
		Sender = HideUtil.readString(buf);
		msgJson = HideUtil.readString(buf);
	}

	@Override
	public String toString() {
		return "[ID=" + ID + ",Sender=" + Sender + ",Channel=" + Channel + ",Msg=" + msg + "]";
	}

}
