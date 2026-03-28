package com.sras.repo;

import com.sras.domain.Task;
import com.sras.domain.TaskStatus;
import com.sras.ds.IndexedPriorityQueue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Comparator.*;

public final class TaskRepository {
    private final Map<String, Task> byId = new ConcurrentHashMap<>();

    // Priority (higher first), then earliest deadline, then oldest createdAt
    private final IndexedPriorityQueue<Task> backlog = new IndexedPriorityQueue<>(
            comparingInt((Task t) -> t.priority().weight()).reversed()
                    .thenComparing(Task::deadline)
                    .thenComparing(Task::createdAt)
    );

    public void add(Task task) {
        byId.put(task.id(), task);
        if (task.status() == TaskStatus.PENDING) backlog.add(task);
    }

    public Optional<Task> get(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public List<Task> listAllInBacklogOrder() {
        // show ALL tasks but with pending tasks shown in backlog ordering first
        List<Task> pendingOrdered = backlog.snapshotSorted();
        List<Task> rest = new ArrayList<>();
        for (Task t : byId.values()) {
            if (t.status() != TaskStatus.PENDING) rest.add(t);
        }
        rest.sort(comparing(Task::createdAt));
        ArrayList<Task> result = new ArrayList<>(pendingOrdered);
        result.addAll(rest);
        return result;
    }

    public Optional<Task> pollNextPending() {
        while (true) {
            Task t = backlog.poll();
            if (t == null) return Optional.empty();
            if (t.status() == TaskStatus.PENDING) return Optional.of(t);
            // else ignore stale entry
        }
    }

    public void rebuildBacklog() {
        backlog.clear();
        for (Task t : byId.values()) {
            if (t.status() == TaskStatus.PENDING) backlog.add(t);
        }
    }

    public int totalCount() { return byId.size(); }

    public long countByStatus(TaskStatus status) {
        return byId.values().stream().filter(t -> t.status() == status).count();
    }

    public Collection<Task> all() { return byId.values(); }
}