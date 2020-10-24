package hide.core;

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

public class HideRefCounter<T> {
	private Consumer<T> removeFunc;
	private Function<T, Boolean> refFilter;
	private int maxRefCount = 6;

	/**指定数の値を保持 古いものから削除*/
	public HideRefCounter(Consumer<T> removeFunc, int size) {
		this.removeFunc = removeFunc;
		this.maxRefCount = size;
	}

	public HideRefCounter<T> setFilter(Function<T, Boolean> filter) {
		refFilter = filter;
		return this;
	}

	private LinkedList<T> refList = new LinkedList<>();

	/**しばらく参照されてない値をを削除*/
	public void ref(T scope) {
		//フィルタ
		if (refFilter != null && !refFilter.apply(scope))
			return;
		//重複を削除して先頭に追加
		refList.remove(scope);
		refList.addFirst(scope);
		if (maxRefCount < refList.size()) {
			T old = refList.get(maxRefCount);
			refList.remove(maxRefCount);
			if (refFilter == null || refFilter.apply(old))
				removeFunc.accept(old);
			//System.out.println("ref remove " + old + " from " + refList);

		}
	}

	/**参照保持から値を削除*/
	public void remove(T value) {
		refList.remove(value);
	}
}
