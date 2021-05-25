package hide.chat.gui;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.util.Strings;
import org.lwjgl.input.Mouse;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import hide.chat.HideChatSystem.ChatChannel;
import hide.chat.PacketChatState;
import hide.core.FactionUtil;
import hide.core.HideFaction;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.util.text.ITextComponent;

public class GuiHideChat extends GuiChat {

	private GuiHideNewChat newChat;

	public GuiHideChat(String str) {
		defaultInputFieldText = str;
	}

	private static final int X_OFFSET = 40;

	protected GuiHideButton sendGrobal;
	protected GuiHideButton sendTeam;

	protected GuiHideButton viewGrobal;
	protected GuiHideButton viewTeam;
	protected GuiHideButton viewAllPrivate;

	@Override
	public void initGui() {
		sendGrobal = new GuiHideButton(10, 10, this.height - 35, 45, 20, "Global", false);
		sendTeam = new GuiHideButton(11, 45, this.height - 35, 45, 20, "Team", false);
		buttonList.add(sendGrobal);
		buttonList.add(sendTeam);

		viewGrobal = new GuiHideButton(20, 90, this.height - 40, 48, 20, "Global", true);
		viewTeam = new GuiHideButton(21, 130, this.height - 40, 48, 20, "Team", true);
		viewAllPrivate = new GuiHideButton(22, 170, this.height - 40, 48, 20, "Private", true);

		viewAllPrivate.enabled = false;

		buttonList.add(viewGrobal);
		buttonList.add(viewTeam);
		buttonList.add(viewAllPrivate);

		newChat = (GuiHideNewChat) mc.ingameGUI.persistantChatGUI;
		super.initGui();
		updateSendChannel();
		updateViewChannel(null);

		if (sendChannel.left == ChatChannel.Team && FactionUtil.getTeam() == null) {
			sendChannel = Global;
		}

		inputField.x += X_OFFSET;
		inputField.width -= X_OFFSET;
	}

	private static final ImmutablePair<ChatChannel, String> Global = new ImmutablePair<>(ChatChannel.Global, "");
	private static final ImmutablePair<ChatChannel, String> Info = new ImmutablePair<>(ChatChannel.Info, "");
	private static final ImmutablePair<ChatChannel, String> Client = new ImmutablePair(ChatChannel.ClientOut, "");
	private ImmutablePair<ChatChannel, String> InfoTeam = new ImmutablePair<>(ChatChannel.Info,
			FactionUtil.getFaction());
	private ImmutablePair<ChatChannel, String> Team = new ImmutablePair<>(ChatChannel.Team, FactionUtil.getFaction());

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);

		String sendtotext = "§5[§cGrobal§5]";
		if (sendChannel.left == ChatChannel.Global)
			sendtotext = "§5[§6Global§5]";
		else if (sendChannel.left == ChatChannel.Team)
			sendtotext = "§5[§3Team§5]";
		else if (sendChannel.left == ChatChannel.Private)
			sendtotext = "§5[§3§5]";

		drawStringCenter(sendtotext, 2 + X_OFFSET / 2, this.height - 8, 0xFFFFFFFF);
		this.inputField.drawTextBox();
		ITextComponent itextcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

		if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null) {
			this.handleComponentHover(itextcomponent, mouseX, mouseY);
		}

		// GuiScrean
		for (int i = 0; i < this.buttonList.size(); ++i) {
			this.buttonList.get(i).drawButton(this.mc, mouseX, mouseY, partialTicks);
		}
		for (int j = 0; j < this.labelList.size(); ++j) {
			this.labelList.get(j).drawLabel(this.mc, mouseX, mouseY);
		}
	}

	private static ImmutablePair<ChatChannel, String> sendChannel = Global;

	public void setSendChannel(ImmutablePair<ChatChannel, String> channel) {
		if (sendChannel != channel) {
			sendChannel = channel;
			if (!newChat.getChannelView().contains(channel))
				updateViewChannel(channel);
			updateSendChannel();
			HideFaction.NETWORK.sendToServer(PacketChatState.cannelSent(channel));
		}
	}

	/** 最低限含まれるチャンネル */
	protected final ImmutableSet<ImmutablePair<ChatChannel, String>> COMMON_VIEW = ImmutableSet.of(Client, Info,
			InfoTeam);
	/** 選択トグル用 */
	protected Set<ImmutablePair<ChatChannel, String>> oldView = COMMON_VIEW;

	public void updateSendChannel() {
		sendGrobal.isSelected = sendChannel.left == ChatChannel.Global;
		sendTeam.isSelected = sendChannel.left == ChatChannel.Team;
		sendGrobal.canClick = sendChannel.left != ChatChannel.Global;
		sendTeam.canClick = sendChannel.left != ChatChannel.Team;
		sendTeam.enabled = FactionUtil.getTeam() != null;
	}

	protected void updateViewChannel(ImmutablePair<ChatChannel, String> channel) {
		if (channel != null) {
			Set<ImmutablePair<ChatChannel, String>> view = newChat.getChannelView();
			if (view.contains(channel)) {
				// 含むなら
				if (view.size() == COMMON_VIEW.size() + 1) {
					// １つしか入っていないなら
					// チームチャットの不正閲覧防止
					newChat.setChannelView(applyChange(oldView));
				} else {
					oldView = view;
					newChat.setChannelView(Sets.union(COMMON_VIEW, ImmutableSet.of(channel)));
					setSendChannel(channel);
				}
			} else {
				// 含まないなら
				newChat.setChannelView(Sets.union(applyChange(view), ImmutableSet.of(channel)));
			}
		}

		viewGrobal.isSelected = newChat.getChannelView().contains(Global);
		viewTeam.isSelected = newChat.getChannelView().contains(Team);
		viewTeam.enabled = FactionUtil.getTeam() != null;
	}

	public static ImmutableSet<ImmutablePair<ChatChannel, String>> applyChange(
			Set<ImmutablePair<ChatChannel, String>> set) {
		boolean flag = Strings.isNotBlank(FactionUtil.getFaction());

		return ImmutableSet.copyOf(set.stream().filter(pair -> flag || pair.getLeft() != ChatChannel.Team
				&& (pair.getLeft() != ChatChannel.Info || Strings.isBlank(pair.right))).map((pair) -> {
					if (pair.getLeft() == ChatChannel.Team
							|| pair.getLeft() == ChatChannel.Info && Strings.isNotBlank(pair.right))
						return new ImmutablePair<>(pair.getLeft(), FactionUtil.getFaction());
					return pair;
				}).iterator());
	}

	/*
	 * クライアント->サーバー リクエスト サーバー->クライアント 変更と
	 *
	 */

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case 10:
			setSendChannel(Global);
			break;
		case 11:
			setSendChannel(Team);
			break;
		case 20:
			updateViewChannel(Global);
			break;
		case 21:
			updateViewChannel(Team);
			break;
		default:
			break;
		}
		super.actionPerformed(button);
	}

	private void drawStringCenter(String str, int x, int y, int color) {
		int width = fontRenderer.getStringWidth(str);
		fontRenderer.drawString(str, x - width / 2, y - fontRenderer.FONT_HEIGHT / 2, color);
	}
}
