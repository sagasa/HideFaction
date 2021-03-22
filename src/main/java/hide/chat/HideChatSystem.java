package hide.chat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
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

		//GUIの差し替えを挿入
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

	//チャット欄の乗っ取り
	@SideOnly(Side.CLIENT)
	@SubscribeEvent()
	public void onEvent(GuiOpenEvent event) {
		//System.out.println(event.getGui());
		if (event.getGui() instanceof GuiChat) {
			event.setGui(new GuiHideChat(((GuiChat) event.getGui()).defaultInputFieldText));
		}
	}




	public enum ChatChannel {
		/**全体チャット*/
		Global(true, new TextComponentString("[Global]")),
		/**チームチャット*/
		Team(true, new TextComponentString("[Team]")),
		/**イベント情報等*/
		Info(false, new TextComponentString("[Info]")),

		System(false), Private(true);
		/**発言可能か*/
		public final boolean CanWrite;
		public final ITextComponent Name;

		private ChatChannel(boolean canWrite) {
			this(canWrite, new TextComponentString("[Default]"));
		}

		private ChatChannel(boolean canWrite, ITextComponent name) {
			CanWrite = canWrite;
			Name = name;
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

		public ITextComponent getChannelName() {
			return Name;
		}
	}

	public static class PlayerChatState {
		public Set<UUID> MutePlayer = new HashSet<>();
		public boolean MuteGlobal = false;
		public ChatChannel SentTo = ChatChannel.Global;
	}

	@SubscribeEvent()
	public static void onLogin(PlayerLoggedInEvent event) {
		EntityPlayerMP player = (EntityPlayerMP) event.player;
		String faction = FactionUtil.getFaction(player);
	}

	@SubscribeEvent()
	public static void onChat(ServerChatEvent event) {

		ImmutablePair<ChatChannel, String> channel = HidePlayerDataManager.getServerData(ServerChatData.class, event.getPlayer()).sendChannel;

		//*
		System.out.println(" " + event.getMessage() + " " + channel + " " + event.getPlayer());
		HideChatLine line = HideChatDB.logChat(event.getPlayer().getUniqueID().toString(), channel.left, channel.right, channel.left.getChannelName().appendSibling(event.getComponent()));
		HideFaction.NETWORK.sendToAll(PacketChat.newChat(line));
		//*/
		/*
		for (int i = 0; i < 100; i++) {
			HideChatLine line = HideFactionDB.logChat(event.getPlayer().getUniqueID().toString(),channel.left, channel.right, event.getComponent().createCopy().appendText(i + ""));
			HideFaction.NETWORK.sendToAll(PacketChat.newChat(line));
		}
		//*/
		event.setCanceled(true);
	}

	public static final Logger log = LogManager.getLogger();

	/**読み込みの1単位*/
	public static final int CHUNK_SIZE = 80;
	/**チャンクの重複させるサイズ*/
	public static final int TRANSITION_SIZE = 20;
	/**最大で表示される行数*/
	public static final int MAX_VIEW_SIZE = 20;
	/**端までの距離がこれ未満なら次を読み込む*/
	public static final int PREPARE_SIZE = 8;


	public static String getTitle(EntityPlayer player) {
		return null;//TODO
	}

	public static class ServerChatData implements IHidePlayerData {
		public ImmutablePair<ChatChannel, String> sendChannel = new ImmutablePair<>(ChatChannel.Global, "");

		@Override
		public void init(EntityPlayer arg0) {

		}

	}
}
