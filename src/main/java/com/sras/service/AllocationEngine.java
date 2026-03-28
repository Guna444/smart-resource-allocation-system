package com.sras.service;

import com.sras.domain.*;
import com.sras.repo.AssignmentRepository;
import com.sras.repo.ResourceRepository;
import com.sras.repo.TaskRepository;
import com.sras.strategy.AllocationStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AllocationEngine {
    private final TaskRepository taskRepo;
    private final ResourceRepository resourceRepo;
    private final AssignmentRepository assignmentRepo;

    public AllocationEngine(TaskRepository taskRepo, ResourceRepository resourceRepo, AssignmentRepository assignmentRepo) {
        this.taskRepo = taskRepo;
        this.resourceRepo = resourceRepo;
        this.assignmentRepo = assignmentRepo;
    }

    public Optional<Assignment> allocateNext(AllocationStrategy strategy) {
        Optional<Task> nextOpt = taskRepo.pollNextPending();
        if (nextOpt.isEmpty()) return Optional.empty();

        Task task = nextOpt.get();
        List<Resource> eligible = eligibleResourcesFor(task);

        if (eligible.isEmpty()) {
            // Put task back by rebuilding backlog.
            // (Simple for demo: task is still PENDING, rebuild restores ordering.)
            taskRepo.rebuildBacklog();
            return Optional.empty();
        }

        Resource chosen = strategy.select(task, eligible).orElse(null);
        if (chosen == null) {
            taskRepo.rebuildBacklog();
            return Optional.empty();
        }

        int loadPoints = estimateLoadPoints(task);
        if (!chosen.canTake(loadPoints)) {
            // Rare case due to race changes; restore.
            taskRepo.rebuildBacklog();
            return Optional.empty();
        }

        // Apply assignment (state tracking)
        chosen.addLoad(loadPoints);
        task.markAllocatedTo(chosen.id());

        Assignment a = Assignment.create(task.id(), chosen.id(), loadPoints);
        assignmentRepo.add(a);

        return Optional.of(a);
    }

    public int allocateBacklog(AllocationStrategy strategy, int maxAllocations) {
        int count = 0;
        for (int i = 0; i < maxAllocations; i++) {
            Optional<Assignment> a = allocateNext(strategy);
            if (a.isEmpty()) break;
            count++;
        }
        return count;
    }

    public boolean markTaskCompleted(String taskId) {
        Optional<Task> tOpt = taskRepo.get(taskId);
        if (tOpt.isEmpty()) return false;

        Task t = tOpt.get();
        if (t.status() != TaskStatus.ALLOCATED) return false;

        t.markCompleted();
        return true;
    }

    public boolean releaseResourceLoad(String resourceId, int points) {
        Optional<Resource> rOpt = resourceRepo.get(resourceId);
        if (rOpt.isEmpty()) return false;
        rOpt.get().releaseLoad(points);
        return true;
    }

    private List<Resource> eligibleResourcesFor(Task task) {
        ArrayList<Resource> eligible = new ArrayList<>();
        int loadPoints = estimateLoadPoints(task);

        for (Resource r : resourceRepo.all()) {
            if (!r.isActive()) continue;

            // skills check: resource must contain all required skills
            if (!r.skills().containsAll(task.requiredSkills())) continue;

            // capacity check
            if (!r.canTake(loadPoints)) continue;

            eligible.add(r);
        }
        return eligible;
    }

    /**
     * Convert estimatedMinutes into "load points".
     * In real systems this could be cost models, weights, or SLAs.
     */
    private int estimateLoadPoints(Task task) {
        // simple mapping: 1 minute = 1 point; scale by priority
        int base = Math.max(1, task.estimatedMinutes());
        return base + (task.priority().weight() * 5);
    }
}