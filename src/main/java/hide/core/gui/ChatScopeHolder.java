package hide.core.gui;

import static hide.chat.HideChatManager.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.google.common.collect.ImmutableSet;

import hide.chat.HideChatLine;
import hide.chat.HideChatManager.ChatChannel;
import hide.chat.PacketChat;
import hide.core.HideFaction;
import hide.core.gui.ChatScopeHolder.ChatChunk.ChatIterator;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.network.play.server.SPacketChat;

public class ChatScopeHolder {

	final int id;
	private final ImmutableSet<ImmutablePair<ChatChannel, String>> channel;
	private final GuiNewChat gui;

	public ChatScopeHolder(ImmutableSet<ImmutablePair<ChatChannel, String>> channel, GuiNewChat gui) {
		this.channel = channel;
		this.gui = gui;
		id = channel.hashCode();
	}

	/** IDで降順 */
	// private LinkedList<ChatChunk> scopeList = new LinkedList<>();
	private ChatChunk current;
	private ChatChunk latest;

	int scroll = 0;

	public void scroll(int amount) {
		int old = scroll;
		scroll += amount;
		if (current.getCountToOldest(scroll, gui.getLineCount()) < 0) {
			// 最古なら
			scroll += current.getCountToOldest(scroll, gui.getLineCount());
		} else if (scroll < 0) {
			ChatChunk next = current.next;
			System.out.println("next scroll " + next);
			if (next != null) {
				current = next;
				scroll = Math.max(current.getTotalCount() + scroll - 1, 0);
			} else
				scroll = 0;
		} else if (current.getTotalCount() <= scroll) {
			ChatChunk prev = current.prev;
			System.out.println("prev scroll " + prev);
			scroll = scroll - current.getTotalCount() + 1;
			if (prev != null)
				current = prev;

			// scroll = scroll % CHUNK_SIZE;
		}
		// System.out.println(current.getCountToOldest(scroll, gui.getLineCount()) + " "
		// + gui.getLineCount() + " " + scroll);
		// System.out.println("scroll " + scroll + " " + current.size + " " +
		// current.getTotalCount()+" "+scopeList);
		checkNext();
		SPacketChat msg;
	}

	public void scrollLatest() {
		System.out.println("scrollLatest");
		current = latest;
		checkNext();
		scroll = 0;

	}

	/** リクエストが返ってきていない間true */
	boolean waitData = false;

	/** チャンクの末端に近ければ次を読み込む */
	private void checkNext() {
		if (current == null)
			return;
		// リクエストのインターバル
		if (waitData && System.currentTimeMillis() < waitTime) {
			// System.out.println("skip!!!!!!");
			return;
		}
		// 取得の必要性
		// 古いほうのチェック
		if (!current.isOldest && current.getTotalCount() < scroll + MAX_VIEW_SIZE + PREPARE_SIZE) {
			// Listに無いなら
			if (current.prev == null) {
				HideFaction.NETWORK.sendToServer(PacketChat.reqPrev(channel, current.getOldest().ID, id));
				waitTime = System.currentTimeMillis() + 500;
				waitData = true;
			}
		}
		// 新しいほう
		if (current.isFull() && scroll - PREPARE_SIZE < 0) {
			// Listに無いなら
			if (current.next == null) {
				HideFaction.NETWORK.sendToServer(PacketChat.reqNext(channel, current.getLatest().ID, id));
				waitTime = System.currentTimeMillis() + 500;
				waitData = true;
			}
		}
	}

	public Iterator<HideChatLine> getIterator() {
		return new ChatIterator(getCurrent(), scroll);
	}

	/** サーバーへのデータ要求インターバル */
	private long waitTime;

	/** 無いならサーバーにリクエストを送信 */
	public ChatChunk getCurrent() {
		if (current == null && waitTime < System.currentTimeMillis()) {
			waitTime = System.currentTimeMillis() + 5000;
			HideFaction.NETWORK.sendToServer(PacketChat.reqPrev(channel, -1, id));
			System.out.println("req init chunk");
		}
		return current;
	}

	/**
	 * サーバーからのチャンク配信 適切な位置に追加 接続可能なチャンクのみ許可
	 *
	 * @param isNext trueならNextそうでなければPrev
	 */
	public boolean addChunk(HideChatLine[] chatArray, int size, int id, boolean isNext) {
		if (this.id != id)
			return false;
		// 表示できないサイズを許容しない
		if (current != null && size <= 1) {
			ChatChunk tes = new ChatChunk(chatArray, size, !isNext);
			System.out.println("debug " + current + " " + tes.isLatest + " " + tes.isOldest + " " + tes + " ");
			return false;
		}
		ChatChunk scope = new ChatChunk(chatArray, size, !isNext);
		waitData = false;

		// 最初のチャンクが届いたらスクロール
		if (current == null) {
			log.info("init latest chunk");
			current = scope;
			latest = scope.makeNextScope();
			while (!newChatQueue.isEmpty()) {
				addChatLine(newChatQueue.poll(), true);
			}
			scroll(0);
		} else {
			current.connect(scope);
			current.remove(1, null);
		}
		return true;
	}

	private Queue<HideChatLine> newChatQueue = new LinkedList<>();// TODO

	public void addChatLine(HideChatLine line, boolean isClient) {
		if (!channel.contains(new ImmutablePair(line.Channel, line.ChannelName)))
			return;
		System.out.println("add clientChat " + line.Sender + " " + line.getMsg().getFormattedText());

		// スコープが無いならキューに一時保存
		if (current == null) {
			newChatQueue.add(line);
		} else {
			if (isClient) {
				line.format().forEach(l -> {
					latest.addClinetChat(l);
					scroll(-1);
				});
			} else {
				line.format().forEach(l -> {
					scroll(-1);
					if (latest.addChat(l)) {
						// 一杯になったら次を用意
						System.out.println("arrocate new Chank");
						latest = latest.makeNextScope();
					}
				});
			}
		}

		scrollLatest();
	}

	public void deleteClientChatLine(int id) {
		current.deleteClientChat(id);
	}

	/***/
	protected static class ChatChunk {

		/** 最初なことが確定したら変更 */
		boolean isOldest;
		boolean isLatest;

		ChatChunk next;
		ChatChunk prev;

		/** 参照を作成 接続不能なら何もしない */
		public boolean connect(ChatChunk other) {
			if (getLatest().ID == other.getOldest().ID) {
				next = other;
				other.prev = this;
				return true;
			}
			if (getOldest().ID == other.getLatest().ID) {
				prev = other;
				other.next = this;
				return true;
			}
			return false;
		}

		/** 参照を削除 */
		public void remove(int length, ChatChunk from) {
			length--;
			//System.out.println("remove " + this + " " + length);
			if (prev != null && prev != from)
				if (length < 0) {
					prev.next = null;
					prev = null;
				} else
					prev.remove(length, this);
			if (next != null && next != from && !next.isLatest)
				if (length < 0) {
					next.prev = null;
					next = null;
				} else
					next.remove(length, this);
		}

		int size;
		/** 一番後ろのチャットID */
		final int pos;
		/** 新しいチャットから順に後詰め格納 */
		final HideChatLine[] chatLine;

		/** クライアントのみの一時的な行を含む */
		final List<HideChatLine> clientChat;

		/** 最初のチャンクの場合はフラグ */
		protected ChatChunk(HideChatLine[] chatArray, int size, boolean isOld) {
			this.size = size;
			this.isOldest = isOld && !isFull();
			this.isLatest = !isOld && !isFull();
			pos = size == 0 ? 0 : chatArray[chatArray.length - 1].ID;
			chatLine = chatArray;
			clientChat = new ArrayList<>(size);
			for (int i = CHUNK_SIZE - 1; CHUNK_SIZE - size <= i; i--) {
				// 改行とフォーマット
				addClinetChat(chatArray[i]);
			}
			// System.out.println(this + " isOldest " + isOldest + " isLatest " + isLatest);
		}

		/** 接続が可能か */
		protected boolean canConect(ChatChunk scope) {
			// System.out.println(this + " " + scope);
			// System.out.println((getOldest().ID <= scope.getLatest().ID) + " " +
			// (scope.getOldest().ID <= getLatest().ID));
			return getOldest().ID <= scope.getLatest().ID && scope.getOldest().ID <= getLatest().ID;
		}

		/** チャットを追加 行数を返す */
		protected int addClinetChat(HideChatLine chat) {
			List<HideChatLine> list = chat.format();
			list.forEach(l -> {
				clientChat.add(l);
			});
			return list.size();
		}

		/** チャットラインを追加 チャンネルチェックはしてないので注意 もし一杯になったらtrue */
		protected boolean addChat(HideChatLine chat) {
			addClinetChat(chat);
			chatLine[CHUNK_SIZE - size - 1] = chat;
			size++;
			this.isLatest = isLatest && !isFull();
			return CHUNK_SIZE == size;
		}

		protected void deleteClientChat(int id) {
			clientChat.removeIf(line -> line.ClientID == id);
		}

		/** 末端を引き継いだ次のスコープを作成 */
		protected ChatChunk makeNextScope() {
			if (size != CHUNK_SIZE)
				throw new ArrayIndexOutOfBoundsException("scope cap is not full");
			HideChatLine[] newArray = new HideChatLine[CHUNK_SIZE];
			newArray[CHUNK_SIZE - 1] = getLatest();
			return new ChatChunk(newArray, 1, false);
		}

		public int getTotalCount() {
			return clientChat.size();
		}

		/** 末端までの余裕 */
		public int getCountToOldest(int scroll, int viewSize) {
			if (prev != null && getTotalCount() < scroll + viewSize) {
				return prev.getCountToOldest(0, scroll + viewSize - getTotalCount() + 1);
			}
			return getTotalCount() - scroll - viewSize;
		}

		public HideChatLine getLatest() {
			return size == 0 ? null : chatLine[CHUNK_SIZE - size];
		}

		public HideChatLine getOldest() {
			return chatLine[CHUNK_SIZE - 1];
		}

		public boolean isFull() {
			return size == CHUNK_SIZE;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ChatChunk))
				return super.equals(obj);
			ChatChunk other = (ChatChunk) obj;
			return other.pos == pos && other.size == size;
		}

		@Override
		public int hashCode() {
			return Objects.hash(pos, size);
		}

		@Override
		public String toString() {
			if (size == 0)
				return "[Size = 0]";
			StringBuilder sb = new StringBuilder("[Size = " + size + ", ID = [" + getLatest().ID + " - " + pos + "]");
			if (prev != null)
				sb.append(", hasPrev");
			if (next != null)
				sb.append(", hasNext");
			sb.append("]");
			return sb.toString();
		}

		// イテレータ
		public static class ChatIterator implements Iterator<HideChatLine> {
			private ChatChunk current;
			private int i = MAX_VIEW_SIZE;
			private int index = 0;

			protected ChatIterator(ChatChunk start, int scroll) {
				current = start;
				index = Math.min(current.clientChat.size() - scroll, current.clientChat.size());
			}

			@Override
			public boolean hasNext() {
				if (i <= 0)
					return false;
				if (0 < index)
					return true;
				return current.prev != null;
			}

			@Override
			public HideChatLine next() {
				i--;
				index--;
				if (index < 0) {
					// １つ前のチャンクへ
					current = current.prev;
					index = current.clientChat.size() + index - 1;
				}
				return current.clientChat.get(index);
			}
		}
	}
}
