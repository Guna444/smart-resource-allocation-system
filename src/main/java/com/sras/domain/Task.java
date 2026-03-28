package com.sras.domain;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public final class Task {
    private final String id;
    private final String title;
    private final Priority priority;
    private final Instant deadline;
    private final int estimatedMinutes;
    private final Set<Skill> requiredSkills;
    private final Instant createdAt;

    private TaskStatus status;
    private String allocatedResourceId; // nullable

    private Task(String id,
                 String title,
                 Priority priority,
                 Instant deadline,
                 int estimatedMinutes,
                 Set<Skill> requiredSkills,
                 Instant createdAt,
                 TaskStatus status,
                 String allocatedResourceId) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.deadline = deadline;
        this.estimatedMinutes = estimatedMinutes;
        this.requiredSkills = requiredSkills;
        this.createdAt = createdAt;
        this.status = status;
        this.allocatedResourceId = allocatedResourceId;
    }

    public static Task create(String title, Priority priority, Instant deadline, int estimatedMinutes, Set<Skill> requiredSkills) {
        return new Task(
                UUID.randomUUID().toString(),
                title,
                priority,
                deadline,
                estimatedMinutes,
                requiredSkills == null ? Collections.emptySet() : Set.copyOf(requiredSkills),
                Instant.now(),
                TaskStatus.PENDING,
                null
        );
    }

    public String id() { return id; }
    public String title() { return title; }
    public Priority priority() { return priority; }
    public Instant deadline() { return deadline; }
    public int estimatedMinutes() { return estimatedMinutes; }
    public Set<Skill> requiredSkills() { return requiredSkills; }
    public Instant createdAt() { return createdAt; }
    public TaskStatus status() { return status; }
    public String allocatedResourceId() { return allocatedResourceId; }

    public void markAllocatedTo(String resourceId) {
        this.status = TaskStatus.ALLOCATED;
        this.allocatedResourceId = resourceId;
    }

    public void markCompleted() {
        this.status = TaskStatus.COMPLETED;
    }

    public boolean isPending() { return status == TaskStatus.PENDING; }

    public String toDisplayString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", deadline=" + deadline +
                ", estMin=" + estimatedMinutes +
                ", skills=" + requiredSkills +
                ", status=" + status +
                (allocatedResourceId != null ? ", allocatedTo=" + allocatedResourceId : "") +
                '}';
    }
}