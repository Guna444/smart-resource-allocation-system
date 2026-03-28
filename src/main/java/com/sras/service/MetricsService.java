package com.sras.service;

import com.sras.domain.Resource;
import com.sras.domain.TaskStatus;
import com.sras.repo.AssignmentRepository;
import com.sras.repo.ResourceRepository;
import com.sras.repo.TaskRepository;

import java.time.Duration;
import java.time.Instant;

public final class MetricsService {
    private final TaskRepository taskRepo;
    private final ResourceRepository resourceRepo;
    private final AssignmentRepository assignmentRepo;

    public MetricsService(TaskRepository taskRepo, ResourceRepository resourceRepo, AssignmentRepository assignmentRepo) {
        this.taskRepo = taskRepo;
        this.resourceRepo = resourceRepo;
        this.assignmentRepo = assignmentRepo;
    }

    public String summary() {
        long pending = taskRepo.countByStatus(TaskStatus.PENDING);
        long allocated = taskRepo.countByStatus(TaskStatus.ALLOCATED);
        long completed = taskRepo.countByStatus(TaskStatus.COMPLETED);

        double avgUtil = 0.0;
        int n = 0;
        for (Resource r : resourceRepo.all()) {
            avgUtil += r.utilization();
            n++;
        }
        if (n > 0) avgUtil /= n;

        Duration avgWait = averageWaitTimeForAllocatedTasks();

        return """
                METRICS
                - tasks.total: %d
                - tasks.pending: %d
                - tasks.allocated: %d
                - tasks.completed: %d
                - resources.total: %d
                - assignments.total: %d
                - resources.avgUtilization: %.2f%%
                - tasks.avgWait(allocated): %s
                """.formatted(
                taskRepo.totalCount(),
                pending,
                allocated,
                completed,
                resourceRepo.totalCount(),
                assignmentRepo.count(),
                avgUtil * 100.0,
                avgWait == null ? "n/a" : avgWait.toSeconds() + "s"
        );
    }

    private Duration averageWaitTimeForAllocatedTasks() {
        // approximate: for allocated tasks, measure now - createdAt (no assignedAt link kept in Task)
        // For a more accurate metric, link Assignment -> Task createdAt in future.
        long allocatedCount = 0;
        long totalSeconds = 0;

        Instant now = Instant.now();
        for (var t : taskRepo.all()) {
            if (t.status() != TaskStatus.ALLOCATED) continue;
            allocatedCount++;
            totalSeconds += Duration.between(t.createdAt(), now).toSeconds();
        }
        if (allocatedCount == 0) return null;
        return Duration.ofSeconds(totalSeconds / allocatedCount);
    }
}