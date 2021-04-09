package hide.chat;

import static hide.chat.HideChatSystem.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.google.common.collect.Lists;

import hide.chat.HideChatSystem.ChatChannel;
import hide.core.FactionUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.Loader;

public class HideChatDB {

	private static Connection conn;

	public static void start() {
		File file = new File(Loader.instance().getConfigDir().getParentFile(), "/FactionDB/");
		System.out.println("File " + file);
		file.mkdirs();
		final String URL = "jdbc:sqlite:FactionDB/faction.db";
		// final String USER = "";
		// final String PASS = "";

		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(URL);
			Statement stmt = conn.createStatement();
			stmt.execute(
					"create table if not exists chat(Time datetime, Sender string, ChannelType string, ChannelName string, Msg string)");

		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void end() {
		if (conn == null)
			return;
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static List<HideChatLine> getPrevChatChunk(EntityPlayerMP player,
			Collection<ImmutablePair<ChatChannel, String>> channel, int from) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rowid, * from chat where ");
		if (from != -1) {
			sb.append("rowid <= ");
			sb.append(from);
			sb.append(" and ");
		}
		makeChannelFilter(sb, player, channel);
		sb.append(" order by rowid desc limit ");
		sb.append(CHUNK_SIZE);

		// System.out.println(sb.toString());

		List<HideChatLine> lines = new ArrayList<>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sb.toString());
			while (rs.next())
				lines.add(new HideChatLine(rs.getInt(1), rs.getString(2), rs.getString(3),
						ChatChannel.valueOf(rs.getString(4)), rs.getString(5), rs.getString(6)));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lines;
	}

	public static List<HideChatLine> getNextChatChunk(EntityPlayerMP player,
			Collection<ImmutablePair<ChatChannel, String>> channel, int from) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rowid, * from chat where ");
		if (from != -1) {
			sb.append("rowid >= ");
			sb.append(from);
			sb.append(" and ");
		}
		makeChannelFilter(sb, player, channel);
		sb.append(" limit ");
		sb.append(CHUNK_SIZE);

		// System.out.println(sb.toString());

		List<HideChatLine> lines = new ArrayList<>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sb.toString());
			while (rs.next())
				lines.add(new HideChatLine(rs.getInt(1), rs.getString(2), rs.getString(3),
						ChatChannel.valueOf(rs.getString(4)), rs.getString(5), rs.getString(6)));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Lists.reverse(lines);
	}

	/** チャンネルをorにして返す */
	private static void makeChannelFilter(StringBuilder sb, EntityPlayerMP player,
			Collection<ImmutablePair<ChatChannel, String>> channel) {
		boolean first = true;
		sb.append("(");
		for (ImmutablePair<ChatChannel, String> pair : channel) {
			if (first)
				first = false;
			else
				sb.append(" or ");
			sb.append("ChannelType = '");
			sb.append(pair.left);
			sb.append("'");
			if (pair.left == ChatChannel.Private) {
				sb.append(" and (Sender = '");
				sb.append(player.getUniqueID());
				sb.append("' or ChannelName = '");
				sb.append(player.getUniqueID());
				sb.append("')");
			} else if (pair.left == ChatChannel.Team) {
				sb.append("and ChannelName = '");
				sb.append(FactionUtil.getFaction(player));
				sb.append("'");
			} else if (pair.left == ChatChannel.Info) {
				sb.append(" and (ChannelName = '");
				sb.append("");
				sb.append("' or ChannelName = '");
				sb.append(FactionUtil.getFaction(player));
				sb.append("')");
			}
		}
		sb.append(")");
	}

	/** ログから検索して結果を取得 結果は新しいチャットから順番 */
	public static List<HideChatLine> getChatLog(String sender, ChatChannel channel, String channelName, int size,
			int offset, String timeout) {
		String sWhere = "";
		if (sender != null)
			sWhere += " and Sender = '" + sender + "'";
		if (channel != null)
			sWhere += " and ChannelType = '" + channel + "'";
		if (channelName != null)
			sWhere += " and ChannelName = '" + channelName + "'";
		if (timeout != null)
			sWhere += " and Time >= datetime('now', '-" + timeout + "')";
		sWhere = sWhere.replaceFirst("and", "where");

		String sLimit = "";
		if (0 < size)
			sLimit += " limit " + size + " offset " + offset;

		List<HideChatLine> lines = new ArrayList<>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select rowid, * from chat" + sWhere + " order by rowid desc" + sLimit);
			while (rs.next()) {
				lines.add(new HideChatLine(rs.getInt(1), rs.getString(2), rs.getString(3),
						ChatChannel.valueOf(rs.getString(4)), rs.getString(5), rs.getString(6)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lines;
	}

	/** ログに追加 */
	public static HideChatLine logChat(String sender, ChatChannel channel, String channelName, ITextComponent text) {
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("insert into chat values ( CURRENT_TIMESTAMP,'" + sender + "','" + channel + "','"
					+ channelName + "','" + ITextComponent.Serializer.componentToJson(text) + "')");
			ResultSet rs = stmt.executeQuery("select rowid, * from chat where rowid = last_insert_rowid()");
			rs.next();
			return new HideChatLine(rs.getInt(1), rs.getString(2), sender, channel, channelName, text);
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;
	}
}
