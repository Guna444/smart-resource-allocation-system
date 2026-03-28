package com.smartallocation.model;

import com.smartallocation.enums.Priority;
import com.smartallocation.enums.TaskStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String id;
    private final String name;
    private final Priority priority;
    private final LocalDateTime deadline;
    private TaskStatus status;
    private String assignedResourceId;

    public Task(String id, String name, Priority priority, LocalDateTime deadline) {
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.deadline = deadline;
        this.status = TaskStatus.PENDING;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Priority getPriority() {
        return priority;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getAssignedResourceId() {
        return assignedResourceId;
    }

    public void setAssignedResourceId(String assignedResourceId) {
        this.assignedResourceId = assignedResourceId;
    }

    @Override
    public String toString() {
        return String.format("Task{id='%s', name='%s', priority=%s, deadline=%s, status=%s, assignedResource='%s'}",
                id, name, priority, deadline.format(FORMATTER), status,
                assignedResourceId != null ? assignedResourceId : "none");
    }
}
