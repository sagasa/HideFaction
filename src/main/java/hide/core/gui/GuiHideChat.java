package hide.core.gui;

import java.io.IOException;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.google.common.collect.ImmutableSet;

import hide.chat.HideChatManager;
import hide.chat.HideChatManager.ChatChannel;
import hide.chat.PacketChatState;
import hide.core.HideFaction;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;

public class GuiHideChat extends GuiChat {

	private GuiHideNewChat newChat;

	public void initGui() {
		buttonList.add(new GuiButton(10, 0, this.height - 75, 30, 20, "Global"));
		buttonList.add(new GuiButton(11, 0, this.height - 55, 30, 20, "Team"));
		buttonList.add(new GuiButton(12, 0, this.height - 35, 30, 20, "All"));
		newChat = (GuiHideNewChat) mc.ingameGUI.persistantChatGUI;
		super.initGui();
	}

	/*
	 * クライアント->サーバー リクエスト
	 * サーバー->クライアント 変更と
	 *
	 * */

	private static final ImmutablePair<ChatChannel, String> Global = new ImmutablePair<>(ChatChannel.Global, "");
	private static final ImmutablePair<ChatChannel, String> Info = new ImmutablePair<>(ChatChannel.Info, "");

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case 10:
			newChat.setChannelView(ImmutableSet.of(Global, Info));
			HideFaction.NETWORK.sendToServer(PacketChatState.cannelSent(Global));
			break;
		case 11:
			ImmutablePair<ChatChannel, String> team = new ImmutablePair<>(ChatChannel.Team, HideChatManager.getFaction());
			newChat.setChannelView(ImmutableSet.of(team, Info));
			HideFaction.NETWORK.sendToServer(PacketChatState.cannelSent(team));
			break;
		case 12:
			newChat.setChannelView(ImmutableSet.of(new ImmutablePair<>(ChatChannel.Team, HideChatManager.getFaction()), Global, Info));
			HideFaction.NETWORK.sendToServer(PacketChatState.cannelSent(Global));
			break;

		default:
			break;
		}
		super.actionPerformed(button);
	}
}
