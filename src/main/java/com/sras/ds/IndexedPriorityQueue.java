package com.sras.ds;

import java.util.*;

/**
 * PriorityQueue + index to avoid duplicates.
 * Not a full decrease-key heap; we rebuild when statuses change (simple + reliable for LLD demo).
 */
public final class IndexedPriorityQueue<T> {
    private final PriorityQueue<T> pq;
    private final Set<T> index = new HashSet<>();

    public IndexedPriorityQueue(Comparator<T> comparator) {
        this.pq = new PriorityQueue<>(comparator);
    }

    public void add(T t) {
        if (index.add(t)) {
            pq.add(t);
        }
    }

    public void addAll(Collection<T> items) {
        for (T t : items) add(t);
    }

    public T poll() {
        T t = pq.poll();
        if (t != null) index.remove(t);
        return t;
    }

    public boolean isEmpty() { return pq.isEmpty(); }
    public int size() { return pq.size(); }

    public void clear() {
        pq.clear();
        index.clear();
    }

    public List<T> snapshotSorted() {
        ArrayList<T> list = new ArrayList<>(pq);
        list.sort(pq.comparator());
        return list;
    }
}