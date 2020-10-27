package hide.chat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hide.core.HideFaction;
import hide.core.HideFactionDB;
import hide.core.HidePlayerDataManager;
import hide.core.HidePlayerDataManager.IHidePlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HideChatManager {

	public enum ChatChannel {
		Global {
			@Override
			public ITextComponent getChannelName() {
				return new TextComponentString("[Global]");
			}
		},
		Team {
			@Override
			public ITextComponent getChannelName() {
				return new TextComponentString("[Team]");
			}
		},
		Info(false) {
			@Override
			public ITextComponent getChannelName() {
				return new TextComponentString("[Info]");
			}
		},
		System(false), Private;
		/**発言可能か*/
		public final boolean CanWrite;

		private ChatChannel() {
			this(true);
		}

		private ChatChannel(boolean canWrite) {
			CanWrite = canWrite;
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
			return new TextComponentString("[Default]");
		}
	}

	public static class PlayerChatState {
		public Set<UUID> MutePlayer = new HashSet<>();
		public boolean MuteGlobal = false;
		public ChatChannel SentTo = ChatChannel.Global;
	}

	public static void onLogin(EntityPlayerMP player) {
		String faction = getFaction(player);
	}

	public static void onChat(ServerChatEvent event) {

		ImmutablePair<ChatChannel, String> channel = HidePlayerDataManager.getServerData(ServerChatData.class, event.getPlayer()).sendChannel;

		//*
		//System.out.println(" " + event.getMessage() + " " + channel);
		HideChatLine line = HideFactionDB.logChat(event.getPlayer().getUniqueID().toString(), channel.left, channel.right, channel.left.getChannelName().appendSibling(event.getComponent()));
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

	public static String getFaction(EntityPlayer player) {
		if (player == null || player.world == null)
			return "";
		ScorePlayerTeam team = player.world.getScoreboard().getPlayersTeam(player.getName());
		return team == null ? "" : team.getName();
	}

	@SideOnly(Side.CLIENT)
	public static String getFaction() {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player == null || player.world == null)
			return "";
		ScorePlayerTeam team = player.world.getScoreboard().getPlayersTeam(player.getName());
		return team == null ? "" : team.getName();
	}

	public static class ServerChatData implements IHidePlayerData {
		public ImmutablePair<ChatChannel, String> sendChannel = new ImmutablePair<>(ChatChannel.Global, "");

		@Override
		public void init(EntityPlayer arg0) {

		}

	}
}
