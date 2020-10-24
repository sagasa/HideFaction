package hide.core.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.google.common.collect.ImmutableSet;

import hide.chat.HideChatLine;
import hide.chat.HideChatManager;
import hide.chat.HideChatManager.ChatChannel;
import hide.core.HideRefCounter;
import hide.core.gui.ChatScopeHolder.ChatChunk;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;;

public class GuiHideNewChat extends GuiNewChat {

	protected Set<ImmutablePair<ChatChannel, String>> channelView = new HashSet<>();
	protected Map<Set<ImmutablePair<ChatChannel, String>>, ChatScopeHolder> scopeMap = new HashMap<>();
	protected HideRefCounter<Set<ImmutablePair<ChatChannel, String>>> ref = new HideRefCounter<>(scopeMap::remove, 4);

	public GuiHideNewChat(Minecraft mcIn) {
		super(mcIn);
		setChannelView(ImmutableSet.of(new ImmutablePair(ChatChannel.Global, ""), new ImmutablePair(ChatChannel.Info, ""), new ImmutablePair(ChatChannel.System, "")));
	}

	/**チャットのビューを変更*/
	public void setChannelView(Set<ImmutablePair<ChatChannel, String>> view) {
		if (!channelView.equals(view)) {
			channelView = view;
			ref.ref(channelView);
			clearChatMessages(false);
		}
	}

	public Set<ImmutablePair<ChatChannel, String>> getChannelView() {
		return channelView;
	}

	protected ChatScopeHolder getChannelHolder() {
		if (!scopeMap.containsKey(channelView)) {
			scopeMap.put(channelView, new ChatScopeHolder((ImmutableSet<ImmutablePair<ChatChannel, String>>) channelView, this));
		}
		return scopeMap.get(channelView);
	}

	/**新規チャット追加 必要なら新しいスコープを用意する*/
	public void addNewChatLine(HideChatLine line) {
		line.UpdatedCounter = mc.ingameGUI.getUpdateCounter();
		getChannelHolder().addChatLine(line, false);
	}

	public void addChatChunk(HideChatLine[] chatArray, int size, int id, boolean isNext) {
		getChannelHolder().addChunk(chatArray, size, id, isNext);
	}

	@Override
	public void clearChatMessages(boolean p_146231_1_) {
		scopeMap.clear();
		channelView = ImmutableSet.copyOf(channelView.stream().map((pair) -> {
			if (pair.getLeft() == ChatChannel.Team)
				return new ImmutablePair<>(pair.getLeft(), HideChatManager.getFaction());
			return pair;
		}).iterator());
	}

	/**チャットは届かないはずなのでシステムメッセージのみ*/
	@Override
	public void printChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId) {
		LOGGER.info("[CHAT] {}", (Object) chatComponent.getUnformattedText().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
		if (chatLineId != 0) {
			this.deleteChatLine(chatLineId);
		}
		HideChatLine line = new HideChatLine(chatComponent, chatLineId);
		line.ClientID = chatLineId;
		line.UpdatedCounter = mc.ingameGUI.getUpdateCounter();
		getChannelHolder().addChatLine(line, true);
	}

	@Override
	public void deleteChatLine(int id) {
		getChannelHolder().deleteClientChatLine(id);
	}

	private static final int OpenChatOffsetX = 30;

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
				int width = MathHelper.ceil((float) this.getChatWidth() / chatScale);
				GlStateManager.pushMatrix();

				if (this.getChatOpen()) {
					flag = true;
					GlStateManager.translate(OpenChatOffsetX, 0, 0.0F);
				}

				GlStateManager.translate(2.0F, 8.0F, 0.0F);
				GlStateManager.scale(chatScale, chatScale, 1.0F);

				int i = 0;

				Iterator<HideChatLine> itr = holder.getIterator();
				for (; itr.hasNext() && i < lineCount; ++i) {
					HideChatLine chatline = itr.next();

					if (chatline != null) {
						int time = updateCounter - chatline.UpdatedCounter;

						if (time < 200 || flag) {
							double d = (double) time / 200.0D;
							d = 1.0D - d;
							d = d * 10.0D;
							d = MathHelper.clamp(d, 0.0D, 1.0D);
							d = d * d;
							int alpha = (int) (255.0D * d);

							if (flag) {
								alpha = 255;
							}

							alpha = (int) ((float) alpha * chatOpacity);

							if (alpha > 3) {
								int i2 = 0;
								int j2 = -i * 9;
								drawRect(2, j2 - 9, width + 4, j2, alpha / 2 << 24);
								String s = chatline.getMsg().getFormattedText();
								GlStateManager.enableBlend();
								this.mc.fontRenderer.drawStringWithShadow(s, 0, (float) (j2 - 8), 16777215 + (alpha << 24));
								GlStateManager.disableAlpha();
								GlStateManager.disableBlend();
							}
						}
					}
				}

				if (flag) {
					int k2 = this.mc.fontRenderer.FONT_HEIGHT;
					GlStateManager.translate(-3.0F, 0.0F, 0.0F);
					int l2 = j * k2 + j;
					int i3 = i * k2 + i;
					int j3 = this.scrollPos * i3 / j;
					int k1 = i3 * i3 / l2;

					if (l2 != i3) {
						int k3 = j3 > 0 ? 170 : 96;
						int l3 = this.isScrolled ? 13382451 : 3355562;
						drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
						drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
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
			/*
			ScaledResolution scaledresolution = new ScaledResolution(this.mc);
			int i = scaledresolution.getScaleFactor();
			float f = this.getChatScale();
			int j = mouseX / i - 2;
			int k = mouseY / i - 40;
			j = MathHelper.floor((float) j / f);
			k = MathHelper.floor((float) k / f);

			ChatScopeHolder ch = getChannelHolder();
			List<HideChatLine> list = ch.getCurrent().clientChat;

			if (j >= 0 && k >= 0) {
				int l = Math.min(this.getLineCount(), list.size());

				if (j <= MathHelper.floor((float) this.getChatWidth() / this.getChatScale()) && k < this.mc.fontRenderer.FONT_HEIGHT * l + l) {
					int i1 = k / this.mc.fontRenderer.FONT_HEIGHT + ch.scroll;

					if (i1 >= 0 && i1 < list.size()) {
						HideChatLine chatline = list.get(i1);
						int j1 = OpenChatOffsetX;

						for (ITextComponent itextcomponent : chatline.getMsg()) {
							if (itextcomponent instanceof TextComponentString) {
								j1 += this.mc.fontRenderer.getStringWidth(GuiUtilRenderComponents.removeTextColorsIfConfigured(((TextComponentString) itextcomponent).getText(), false));

								if (j1 > j) {
									return itextcomponent;
								}
							}
						}
					}

					return null;
				} else {
					return null;
				}
			} else {
				return null;
			}//*/
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
