package hide.core.gui;

import static hide.chat.HideChatManager.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.google.common.collect.ImmutableSet;

import hide.chat.HideChatLine;
import hide.chat.HideChatManager.ChatChannel;
import hide.chat.PacketChat;
import hide.core.HideFaction;
import hide.core.HideRefCounter;
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

	/**IDで降順*/
	private LinkedList<ChatChunk> scopeList = new LinkedList<>();
	private ChatChunk current;

	int scroll = 0;

	public void scroll(int amount) {
		int old = scroll;
		scroll += amount;
		if (current.isOldest && current.getTotalCount() - gui.getLineCount() <= scroll) {
			//最古なら
			scroll = current.getTotalCount() - gui.getLineCount();
		} else if (scroll < 0) {
			ChatChunk next = current.next;
			System.out.println("next scroll " + next);
			if (setCurrent(next)) {
				scroll = Math.max(current.getTotalCount() + scroll, 0);
			} else
				scroll = 0;
		} else if (current.getTotalCount() <= scroll) {
			ChatChunk prev = current.prev;
			System.out.println("prev scroll " + prev);
			scroll = scroll - current.getTotalCount();
			setCurrent(prev);

			//scroll = scroll % CHUNK_SIZE;
		}
		//System.out.println("scroll " + scroll + " " + current.size + " " + current.getTotalCount()+" "+scopeList);
		checkNext();
		SPacketChat msg;
	}

	public void scrollLatest() {
		if (haveLatest()) {
			setCurrent(scopeList.peekFirst());
			checkNext();
			scroll = 0;
		} else
			HideFaction.NETWORK.sendToServer(PacketChat.reqPrev(channel, -1, id));
	}

	/**リクエストが返ってきていない間true*/
	boolean waitData = false;

	/**チャンクの末端に近ければ次を読み込む*/
	private void checkNext() {
		if (current == null)
			return;
		//リクエストのインターバル
		if (waitData && System.currentTimeMillis() < waitTime) {
			//System.out.println("skip!!!!!!");
			return;
		}
		int index = scopeList.indexOf(current);
		//System.out.println("current index = " + index + " size = " + current.size + " " + ArrayUtils.toString(current.chatLine));
		//古いほうのチェック
		//取得の必要性
		if (!current.isOldest && current.getTotalCount() < scroll + MAX_VIEW_SIZE + PREPARE_SIZE) {
			//Listに無いなら
			if (current.prev == null) {
				HideFaction.NETWORK.sendToServer(PacketChat.reqPrev(channel, current.getOldest().ID, id));
				waitTime = System.currentTimeMillis() + 500;
				waitData = true;
				//System.out.println("make prev req");
				//if (scopeList.size() > index + 1)
				//	System.out.println(scopeList.get(index + 1).getLatest().ID + "<" + current.chatLine[CHUNK_SIZE - TRANSITION_SIZE].ID);
			}
		}
		//新しいほう
		if (current.isFull() && scroll - PREPARE_SIZE < 0) {
			//Listに無いなら
			if (current.next == null) {
				HideFaction.NETWORK.sendToServer(PacketChat.reqNext(channel, current.getLatest().ID, id));
				waitTime = System.currentTimeMillis() + 500;
				waitData = true;
				//System.out.println("make next req " + (index != 0));
				//if (index != 0)
				//	System.out.println(current.chatLine[TRANSITION_SIZE - 1].ID + "<" + scopeList.get(index - 1).getOldest().ID);
			}
		}
	}

	public Iterator<HideChatLine> getIterator() {
		return getCurrent().new ChatIterator(scroll);
	}

	/**List内のスコープをCurrentにセット */
	protected boolean setCurrent(ChatChunk scope) {
		if (scope == null)
			return false;
		current = scope;
		refCount.ref(scope);
		return true;
	}

	/**サーバーへのデータ要求インターバル*/
	private long waitTime;

	/**無いならサーバーにリクエストを送信*/
	public ChatChunk getCurrent() {
		if (current == null && waitTime < System.currentTimeMillis()) {
			waitTime = System.currentTimeMillis() + 5000;
			HideFaction.NETWORK.sendToServer(PacketChat.reqPrev(channel, -1, id));
		}
		return current;
	}

	/**サーバーからのチャンク配信
	 * @param isNext trueならNextそうでなければPrev*/
	public boolean addChunk(HideChatLine[] chatArray, int size, int id, boolean isNext) {
		if (this.id != id)
			return false;
		//表示できないサイズを許容しない
		if (current != null && size < MAX_VIEW_SIZE) {
			System.out.println("debug "+size+" ");
			return false;
		}
		ChatChunk scope = new ChatChunk(chatArray, size, !isNext);
		waitData = false;
		addChunk(scope);
		//最初のチャンクが届いたらスクロール
		if (current == null || scope.isLatest) {
			current = scope;
			//更新
			scroll(0);
		}
		//最初のチャンクに追加する場合1度だけ
		if (!haveLatest()) {
			log.info("init latest chunk");
			ChatChunk latest = scopeList.peekFirst().makeNextScope();
			addChunk(latest);
		}
		return true;
	}

	private Queue<HideChatLine> newChatQueue = new LinkedList<>();//TODO

	public void addChatLine(HideChatLine line, boolean isClient) {
		if (!channel.contains(new ImmutablePair(line.Channel, line.ChannelName)))
			return;
		System.out.println("add clientChat");

		//スコープが無いならキューに一時保存
		if (current == null) {
			newChatQueue.add(line);
		} else {
			ChatChunk scope = scopeList.peekFirst();
			if (isClient) {
				line.format().forEach(l -> {
					scope.addClinetChat(l);
					scroll(-1);
				});
			} else {
				line.format().forEach(l -> {
					scroll(-1);
					if (scope.addChat(l)) {
						//参照を更新
						refCount.ref(scope);
						//一杯になったら次を用意
						System.out.println("arrocate new Chank");
						ChatChunk latest = scopeList.peekFirst().makeNextScope();
						addChunk(latest);
					}
				});
			}
		}

		scrollLatest();
	}

	public void deleteClientChatLine(int id) {
		current.deleteClientChat(id);
	}

	/**適切な位置に追加 参照を更新 */
	protected void addChunk(ChatChunk scope) {
		for (int i = 0; i < scopeList.size(); i++) {
			ChatChunk s = scopeList.get(i);
			//同じ位置ならサイズが大きいほうを採用
			if (scope.pos == s.pos) {
				if (s.size < scope.size) {
					refCount.remove(s);
					scopeList.set(i, scope);
					refCount.ref(scope);
				}
				System.out.println("not ADD " + scopeList);
				return;
			} else if (s.pos < scope.pos) {
				scopeList.add(i, scope);
				refCount.ref(scope);
				//前後の参照の追加
				if (0 < i)
					ChatChunk.setRef(scopeList.get(i - 1), scope);
				if (i < scopeList.size() - 1)
					ChatChunk.setRef(scope, scopeList.get(i + 1));
				return;
			}
		}
		//参照を追加
		if (0 < scopeList.size()) {
			ChatChunk.setRef(scopeList.getLast(), scope);
		}
		scopeList.addLast(scope);
		refCount.ref(scope);

	}

	/**最初のスコープがFullでないなら最新のスコープを持っている*/
	protected boolean haveLatest() {
		ChatChunk scope = scopeList.peekFirst();
		return scope != null && !scope.isFull();
	}

	private void remove(ChatChunk remove) {
		int i = scopeList.indexOf(remove);
		if (0 < i)
			ChatChunk.removeRef(scopeList.get(i - 1), remove);
		if (i < scopeList.size() - 1)
			ChatChunk.removeRef(remove, scopeList.get(i + 1));
		scopeList.remove(remove);
		//前後にあった場合は参照を試みる
		if (0 < i && i < scopeList.size() - 1)
			ChatChunk.setRef(scopeList.get(i - 1), scopeList.get(i + 1));
	}

	/**最新以外を対象に古いものを削除*/
	protected HideRefCounter<ChatChunk> refCount = new HideRefCounter<ChatChunk>(this::remove, 2).setFilter((obj) -> !obj.isLatest);

	/***/
	protected static class ChatChunk {

		/**最初なことが確定したら変更*/
		boolean isOldest;
		boolean isLatest;

		ChatChunk next;
		ChatChunk prev;

		/**参照を作成 接続不能なら何もしない*/
		public static void setRef(ChatChunk next, ChatChunk prev) {
			if (next.canConect(prev)) {
				prev.next = next;
				next.prev = prev;
			}
		}

		/**参照を削除*/
		public static void removeRef(ChatChunk next, ChatChunk prev) {
			if (next.prev == prev) {
				prev.next = null;
				next.prev = null;
			}
		}

		int size;
		/**一番後ろのチャットID*/
		final int pos;
		/**新しいチャットから順に後詰め格納*/
		final HideChatLine[] chatLine;

		/**クライアントのみの一時的な行を含む*/
		final List<HideChatLine> clientChat;

		/**最初のチャンクの場合はフラグ*/
		protected ChatChunk(HideChatLine[] chatArray, int size, boolean isOld) {
			this.size = size;
			this.isOldest = isOld && !isFull();
			this.isLatest = !isOld && !isFull();
			pos = size == 0 ? 0 : chatArray[chatArray.length - 1].ID;
			chatLine = chatArray;
			clientChat = new ArrayList<>(size);
			for (int i = CHUNK_SIZE - 1; CHUNK_SIZE - size <= i; i--) {
				//改行とフォーマット
				addClinetChat(chatArray[i]);
			}

			System.out.println(this + " isOldest " + isOldest + " isLatest " + isLatest);
		}

		/**接続が可能か*/
		protected boolean canConect(ChatChunk scope) {
			System.out.println(this + " " + scope);
			System.out.println((getOldest().ID <= scope.getLatest().ID) + " " + (scope.getOldest().ID <= getLatest().ID));
			return getOldest().ID <= scope.getLatest().ID &&
					scope.getOldest().ID <= getLatest().ID;
		}

		/**チャットを追加 行数を返す*/
		protected int addClinetChat(HideChatLine chat) {
			List<HideChatLine> list = chat.format();
			list.forEach(l -> {
				clientChat.add(l);
			});
			return list.size();
		}

		/**チャットラインを追加 チャンネルチェックはしてないので注意 もし一杯になったらtrue*/
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

		/**末端を引き継いだ次のスコープを作成*/
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
			return other.pos == pos;
		}

		@Override
		public int hashCode() {
			return pos;
		}

		@Override
		public String toString() {
			if (size == 0)
				return "[Size = 0]";
			return "[Size = " + size + ",ID = [ " + getLatest().ID + " - " + pos + " ]";
		}

		//イテレータ
		public class ChatIterator implements Iterator<HideChatLine> {
			private int i = MAX_VIEW_SIZE;
			private int index = 0;

			protected ChatIterator(int scroll) {
				index = Math.min(clientChat.size() - scroll, clientChat.size());
			}

			@Override
			public boolean hasNext() {
				if (i <= 0)
					return false;
				if (0 < index)
					return true;
				return prev != null;
			}

			@Override
			public HideChatLine next() {
				i--;
				index--;
				if (index < 0)
					return prev.clientChat.get(prev.clientChat.size() + index - 1);
				return clientChat.get(index);
			}

		}
	}
}
