package hide.chat;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Strings;

import hide.chat.gui.GuiHideChat;
import hide.chat.gui.GuiHideNewChat;
import hide.core.FactionUtil;
import hide.core.HideFaction;
import hide.core.HidePlayerDataManager;
import hide.core.HidePlayerDataManager.IHidePlayerData;
import hide.core.IHideSubSystem;
import hide.core.asm.HideCoreHook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HideChatSystem implements IHideSubSystem {

	@Override
	public void init(Side side) {
		HidePlayerDataManager.register(ServerChatData.class, Side.SERVER);

		HideFaction.registerNetMsg(PacketChat.class, PacketChat.class, Side.SERVER);
		HideFaction.registerNetMsg(PacketChat.class, PacketChat.class, Side.CLIENT);

		HideFaction.registerNetMsg(PacketChatState.class, PacketChatState.class, Side.SERVER);
		HideFaction.registerNetMsg(PacketChatState.class, PacketChatState.class, Side.CLIENT);

		// GUIの差し替えを挿入
		if (side == Side.CLIENT) {
			Minecraft.getMinecraft().getFramebuffer().enableStencil();
			HideCoreHook.GuiNewChat = GuiHideNewChat::new;

		}
	}

	@Override
	public void serverStart(FMLServerStartingEvent event) {
		HideChatDB.start();
		System.out.println("Start Server");
		event.registerServerCommand(new CommandChat());
	}

	@Override
	public void serverStop(FMLServerStoppedEvent event) {
		HideChatDB.end();
	}

	// チャット欄の乗っ取り
	@SideOnly(Side.CLIENT)
	@SubscribeEvent()
	public void onEvent(GuiOpenEvent event) {
		// System.out.println(event.getGui());
		if (event.getGui() instanceof GuiChat) {
			event.setGui(new GuiHideChat(((GuiChat) event.getGui()).defaultInputFieldText));
		}
	}

	private static final SimpleDateFormat formatHHMM = new SimpleDateFormat("HH:mm");

	interface ChatPrefix {
		String decorate(World world, String sender, String cannnelName);
	}

	public enum ChatChannel {
		/** 全体チャット */
		Global(true, "§l§7[%s]§5[§6Global§5]§b[§r%s§b]§r%s§r %s"),
		/** チームチャット */
		Team(true, "§l§7[%s]§5[§3Team§5]§r%s§r %s"),
		/** イベント情報等 */
		Info(false, "§l§7[%s]§5[§ePrivate§5]§r%s§a-§l>§r%s§r %s"),

		ClientOut(false, "§5[§3Client§5]"), Private(true);
		/** 発言可能か */
		public final boolean CanWrite;
		/** sender cannelName return */
		public final String Prefix;

		private ChatChannel(boolean canWrite) {
			this(canWrite, "[Default]");
		}

		private ChatChannel(boolean canWrite, String prefix) {
			CanWrite = canWrite;
			Prefix = prefix;
		}

		private byte index;

		private static boolean isInit = false;

		private static void init() {
			byte n = 0;
			for (ChatChannel e : values()) {
				e.index = n;
				n++;
			}
		}

		public byte getIndex() {
			if (!isInit)
				init();
			return index;
		}

		public Style getChannelName() {
			return new Style();
		}
	}

	public static class PlayerChatState {
		public Set<UUID> MutePlayer = new HashSet<>();
		public boolean MuteGlobal = false;
		public ChatChannel SentTo = ChatChannel.Global;
	}

	@SubscribeEvent()
	public void onChat(ServerChatEvent event) {
		System.out.println("Chat event capture");
		EntityPlayer player = event.getPlayer();
		ImmutablePair<ChatChannel, String> channelPair = HidePlayerDataManager.getServerData(ServerChatData.class,
				player).sendChannel;

		ChatChannel channel = channelPair.left;

		String faction = FactionUtil.getFactionDisplay(player);

		ITextComponent text;
		if (channel == ChatChannel.Team) {
			text = new TextComponentString(String.format(channel.Prefix, formatHHMM.format(System.currentTimeMillis()),
					FactionUtil.getPlayerDisplay(player), event.getMessage()));
		} else {
			text = new TextComponentString(String.format(channel.Prefix, formatHHMM.format(System.currentTimeMillis()),
					Strings.isNullOrEmpty(faction) ? "Rookie" : faction, FactionUtil.getPlayerDisplay(player),
					event.getMessage()));
		}
		addChat(event.getPlayer().getUniqueID().toString(), channelPair, text);
		// *

		// */
		/*
		 * for (int i = 0; i < 100; i++) { HideChatLine line =
		 * HideFactionDB.logChat(event.getPlayer().getUniqueID().toString(),channel.
		 * left, channel.right, event.getComponent().createCopy().appendText(i + ""));
		 * HideFaction.NETWORK.sendToAll(PacketChat.newChat(line)); } //
		 */
		event.setCanceled(true);
	}

	private static final ImmutablePair<ChatChannel, String> SYSTEM_CHANNEL = ImmutablePair.of(ChatChannel.ClientOut,
			"");

	public static void sendToSystem(ITextComponent text) {
		addChat("system", SYSTEM_CHANNEL, text);
	}

	/** DBに焼いて配信 */
	private static void addChat(String sender, ImmutablePair<ChatChannel, String> channel, ITextComponent msg) {

		HideChatLine line = HideChatDB.logChat(sender, channel.left, channel.right, msg);
		HideFaction.NETWORK.sendToAll(PacketChat.newChat(line));
		// System.out.println("add msg sender = " + sender + ", channel = " + channel +
		// ", text = " + text.getUnformattedText());
	}

	public static final Logger log = LogManager.getLogger();

	/** 読み込みの1単位 */
	public static final int CHUNK_SIZE = 80;
	/** 最大で表示される行数 */
	public static final int MAX_VIEW_SIZE = 20;
	/** 端までの距離がこれ未満なら次を読み込む */
	public static final int PREPARE_SIZE = 8;

	public static String getTitle(EntityPlayer player) {
		return null;// TODO
	}

	public static class ServerChatData implements IHidePlayerData {
		public ImmutablePair<ChatChannel, String> sendChannel = new ImmutablePair<>(ChatChannel.Global, "");

		@Override
		public void init(EntityPlayer arg0) {

		}

	}
}
