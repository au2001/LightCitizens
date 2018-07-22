package me.au2001.lightcitizens.pathfinder;

import java.util.*;

public class PriorityQueue<T> implements Queue {

	private Comparator<T> comparator;
	private LinkedList<T> entries = new LinkedList<T>();

	public PriorityQueue() {
		this(null);
	}

	public PriorityQueue(Comparator<T> comparator) {
		this.comparator = comparator;

		if (comparator == null) {
			this.comparator = new Comparator<T>() {
				@SuppressWarnings("unchecked")
				public int compare(T o1, T o2) {
					if (o1 != null) {
						Comparable<? super T> oc1 = null;
						try {
							oc1 = (Comparable<? super T>) o1;
						} catch (ClassCastException e) {}
						if (oc1 != null) return oc1.compareTo(o2);
					}

					if (o2 != null) {
						Comparable<? super T> oc2 = null;
						try {
							oc2 = (Comparable<? super T>) o2;
						} catch (ClassCastException e) {}
						if (oc2 != null) return oc2.compareTo(o1);
					}

					return 0;
				}
			};
		}
	}

	public int size() {
		return entries.size();
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}

	public boolean contains(Object element) {
		return entries.contains(element);
	}

	public Iterator<T> iterator() {
		return entries.iterator();
	}

	public Object[] toArray() {
		return entries.toArray();
	}

	public Object[] toArray(Object[] array) {
		return entries.toArray(array);
	}

	@SuppressWarnings("unchecked")
	public boolean add(Object element) {
		if (element == null) return false;
		T tElement;
		try {
			tElement = (T) element;
		} catch (ClassCastException e) {
			return false;
		}

		if (entries.isEmpty()) return entries.add(tElement);

		boolean removed = false, inserted = false;

		ListIterator<T> iterator = entries.listIterator();
		while (iterator.hasNext()) {
			T other = iterator.next();

			if (comparator.compare(tElement, other) <= 0) {
				iterator.previous();
				iterator.add(tElement);
				inserted = true;
				break;
			}

			if (other.equals(tElement)) {
				iterator.remove();
				removed = true;
			}
		}

		if (!inserted) {
			// inserted = true;
			return entries.add(tElement);
		} else if (!removed) {
			while (iterator.hasNext()) {
				T other = iterator.next();

				if (other.equals(tElement)) {
					iterator.remove();
					// removed = true;
					break;
				}
			}
		}

		return true;
	}

	public boolean remove(Object element) {
		return entries.remove(element);
	}

	public boolean addAll(Collection collection) {
		boolean result = true;
		for (Object element : collection)
			result = add(element) && result;
		return result;
	}

	public void clear() {
		entries.clear();
	}

	public boolean retainAll(Collection collection) {
		return entries.retainAll(collection);
	}

	public boolean removeAll(Collection collection) {
		return entries.retainAll(collection);
	}

	public boolean containsAll(Collection collection) {
		return entries.containsAll(collection);
	}

	public boolean offer(Object element) {
		return add(element);
	}

	public T remove() {
		return entries.remove();
	}

	public T poll() {
		return entries.poll();
	}

	public T element() {
		return entries.element();
	}

	public T peek() {
		return entries.peek();
	}

}
