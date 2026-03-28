package com.smartallocation.model;

import com.smartallocation.enums.TaskStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Resource {

    private final String id;
    private final String name;
    private final int capacity;
    private final List<Task> assignedTasks;

    public Resource(String id, String name, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Resource capacity must be greater than zero.");
        }
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.assignedTasks = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<Task> getAssignedTasks() {
        return Collections.unmodifiableList(assignedTasks);
    }

    public int getCurrentLoad() {
        return assignedTasks.size();
    }

    public double getLoadPercentage() {
        return (double) assignedTasks.size() / capacity * 100.0;
    }

    public boolean isAvailable() {
        return assignedTasks.size() < capacity;
    }

    public boolean assignTask(Task task) {
        if (!isAvailable()) {
            return false;
        }
        assignedTasks.add(task);
        task.setAssignedResourceId(this.id);
        task.setStatus(TaskStatus.ASSIGNED);
        return true;
    }

    public boolean releaseTask(String taskId) {
        return assignedTasks.removeIf(t -> t.getId().equals(taskId));
    }

    @Override
    public String toString() {
        return String.format("Resource{id='%s', name='%s', capacity=%d, currentLoad=%d, loadPct=%.1f%%}",
                id, name, capacity, getCurrentLoad(), getLoadPercentage());
    }
}
