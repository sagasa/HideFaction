package hide.chat.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import hide.chat.HideChatLine;
import hide.chat.HideChatSystem.ChatChannel;
import hide.chat.gui.ChatScopeHolder.ChatChunk;
import hide.core.FactionUtil;
import hide.core.HideRefCounter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;;

public class GuiHideNewChat extends GuiNewChat {

	protected Set<ImmutablePair<ChatChannel, String>> channelView = new HashSet<>();
	protected Map<Set<ImmutablePair<ChatChannel, String>>, ChatScopeHolder> scopeMap = new HashMap<>();
	protected HideRefCounter<Set<ImmutablePair<ChatChannel, String>>> ref = new HideRefCounter<>(scopeMap::remove, 4);

	public GuiHideNewChat(Minecraft mcIn) {
		super(mcIn);
		setChannelView(ImmutableSet.of(new ImmutablePair(ChatChannel.Global, ""),
				new ImmutablePair(ChatChannel.Info, ""), new ImmutablePair(ChatChannel.ClientOut, "")));
	}

	@Override
	public void refreshChat() {
		getChannelHolder().refresh();
	}

	public void updateTeam() {
		channelView = Sets.union(channelView, ImmutableSet.of(new ImmutablePair<>(ChatChannel.Team, FactionUtil.getFaction())));
		channelView = ImmutableSet.copyOf(channelView.stream().map((pair) -> {
			if (pair.getLeft() == ChatChannel.Team)
				return new ImmutablePair<>(pair.getLeft(), FactionUtil.getFaction());
			return pair;
		}).iterator());
	}

	/** チャットのビューを変更 */
	public void setChannelView(Set<ImmutablePair<ChatChannel, String>> view) {
		if (!channelView.equals(view)) {
			//System.out.println("Change "+view);
			channelView = view;
			ref.ref(channelView);
		}
	}

	public Set<ImmutablePair<ChatChannel, String>> getChannelView() {
		return channelView;
	}

	protected ChatScopeHolder getChannelHolder() {
		if (!scopeMap.containsKey(channelView)) {
			scopeMap.put(channelView, new ChatScopeHolder(ImmutableSet.copyOf(channelView), this));
		}
		return scopeMap.get(channelView);
	}

	/** 新規チャット追加 必要なら新しいスコープを用意する */
	public void addNewChatLine(HideChatLine line) {
		line.UpdatedCounter = mc.ingameGUI.getUpdateCounter();
		ImmutablePair<ChatChannel, String> channel = ImmutablePair.of(line.Channel, line.ChannelName);
		for (Entry<Set<ImmutablePair<ChatChannel, String>>, ChatScopeHolder> entry : scopeMap.entrySet()) {
			if (entry.getKey().contains(channel))
				entry.getValue().addChatLine(line, false);
		}
	}

	public void addChatChunk(HideChatLine[] chatArray, int size, int id, boolean isNext) {
		getChannelHolder().addChunk(chatArray, size, id, isNext);
	}

	@Override
	public void clearChatMessages(boolean p_146231_1_) {
		scopeMap.clear();
		updateTeam();
	}

	/** チャットは届かないはずなのでシステムメッセージのみ */
	@Override
	public void printChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId) {
		LOGGER.info("[CHAT] {}",
				chatComponent.getUnformattedText().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
		if (chatLineId != 0) {
			this.deleteChatLine(chatLineId);
		}
		chatComponent = new TextComponentString(ChatChannel.ClientOut.Prefix).appendSibling(chatComponent);
		HideChatLine line = new HideChatLine(chatComponent, chatLineId);
		line.ClientID = chatLineId;
		line.UpdatedCounter = mc.ingameGUI.getUpdateCounter();
		getChannelHolder().addChatLine(line, true);
	}

	@Override
	public void deleteChatLine(int id) {
		getChannelHolder().deleteClientChatLine(id);
	}

	@Override
	public void drawChat(int updateCounter) {
		if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
			ChatScopeHolder holder = getChannelHolder();
			ChatChunk scope = holder.getCurrent();

			if (scope == null)
				return;

			int lineCount = this.getLineCount();
			int j = scope.getTotalCount();
			float chatOpacity = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

			if (j > 0) {
				boolean flag = false;

				float chatScale = this.getChatScale();
				int width = MathHelper.ceil(this.getChatWidth() / chatScale);
				GlStateManager.pushMatrix();

				if (this.getChatOpen()) {
					flag = true;
				}

				GlStateManager.translate(2.0F, 8.0F, 0.0F);
				GlStateManager.scale(chatScale, chatScale, 1.0F);

				Iterator<HideChatLine> itr = holder.getIterator();
				for (int i = 0; itr.hasNext() && i < lineCount; ++i) {
					HideChatLine chatline = itr.next();
					if (chatline != null) {
						int time = updateCounter - chatline.UpdatedCounter;
						if (time < 200 || flag) {
							double d = time / 200.0D;
							d = 1.0D - d;
							d = d * 10.0D;
							d = MathHelper.clamp(d, 0.0D, 1.0D);
							d = d * d;
							int alpha = (int) (255.0D * d);

							if (flag) {
								alpha = 255;
							}

							alpha = (int) (alpha * chatOpacity);

							if (alpha > 3) {
								int i2 = 0;
								int j2 = -i * 9;
								drawRect(0, j2 - 9, width + 4, j2, alpha / 2 << 24);
								String s = chatline.getMsg().getFormattedText();
								GlStateManager.enableBlend();
								this.mc.fontRenderer.drawStringWithShadow(s, 0, j2 - 8, 16777215 + (alpha << 24));
								GlStateManager.disableAlpha();
								GlStateManager.disableBlend();
							}
						}
					}
				}
				GlStateManager.popMatrix();
			}
		}
	}

	@Override
	public ITextComponent getChatComponent(int mouseX, int mouseY) {
		if (!this.getChatOpen()) {
			return null;
		} else {
			// *
			ScaledResolution scaledresolution = new ScaledResolution(this.mc);
			int scaleFactor = scaledresolution.getScaleFactor();
			float f = this.getChatScale();
			/* チャット左下を原点とした座標 */
			int x = mouseX / scaleFactor - 2;
			int y = mouseY / scaleFactor - 40;
			x = MathHelper.floor(x / f);
			y = MathHelper.floor(y / f);

			ChatScopeHolder holder = getChannelHolder();
			// Iterator<HideChatLine> itr = holder.getIterator();

			// チャット内か？
			if (x >= 0 && y >= 0 && x <= MathHelper.floor(this.getChatWidth() / this.getChatScale())) {
				if (y <= holder.getCount() * mc.fontRenderer.FONT_HEIGHT) {
					int index = y / this.mc.fontRenderer.FONT_HEIGHT;
					Iterator<HideChatLine> itr = holder.getIterator();
					int lineCount = this.getLineCount();
					for (int i = 0; itr.hasNext() && i < lineCount; ++i) {
						HideChatLine chatline = itr.next();
						if (i == index) {
							int textX = 0;
							for (ITextComponent itextcomponent : chatline.getMsg()) {
								if (itextcomponent instanceof TextComponentString) {
									textX += mc.fontRenderer
											.getStringWidth(GuiUtilRenderComponents.removeTextColorsIfConfigured(
													((TextComponentString) itextcomponent).getText(), false));
									if (textX > x) {
										return itextcomponent;
									}
								}
							}
						}
					}
				}

			} else {
				return null;
			}
		}
		return null;
	}

	@Override
	public void scroll(int amount) {
		if (amount < 0)
			amount = -5;
		if (0 < amount)
			amount = 5;
		getChannelHolder().scroll(amount);
	}
}
