package com.sras.domain;

import java.time.Instant;
import java.util.UUID;

public final class Assignment {
    private final String id;
    private final String taskId;
    private final String resourceId;
    private final Instant assignedAt;
    private final int loadPointsApplied;

    private Assignment(String id, String taskId, String resourceId, Instant assignedAt, int loadPointsApplied) {
        this.id = id;
        this.taskId = taskId;
        this.resourceId = resourceId;
        this.assignedAt = assignedAt;
        this.loadPointsApplied = loadPointsApplied;
    }

    public static Assignment create(String taskId, String resourceId, int loadPointsApplied) {
        return new Assignment(UUID.randomUUID().toString(), taskId, resourceId, Instant.now(), loadPointsApplied);
    }

    public String id() { return id; }
    public String taskId() { return taskId; }
    public String resourceId() { return resourceId; }
    public Instant assignedAt() { return assignedAt; }
    public int loadPointsApplied() { return loadPointsApplied; }

    public String toDisplayString() {
        return "Assignment{" +
                "id='" + id + '\'' +
                ", taskId='" + taskId + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", assignedAt=" + assignedAt +
                ", loadPointsApplied=" + loadPointsApplied +
                '}';
    }
}