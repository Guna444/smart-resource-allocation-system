package com.smartallocation.service;

import com.smartallocation.enums.Priority;
import com.smartallocation.enums.TaskStatus;
import com.smartallocation.model.Resource;
import com.smartallocation.model.Task;
import com.smartallocation.strategy.AllocationStrategy;
import com.smartallocation.strategy.PriorityBasedStrategy;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Core service that manages tasks, resources, and the active allocation strategy.
 */
public class ResourceAllocationSystem {

    private final Map<String, Task> tasks = new LinkedHashMap<>();
    private final Map<String, Resource> resources = new LinkedHashMap<>();
    private AllocationStrategy strategy;
    private int taskCounter = 1;
    private int resourceCounter = 1;

    public ResourceAllocationSystem() {
        this.strategy = new PriorityBasedStrategy();
    }

    // -------------------------------------------------------------------------
    // Strategy management
    // -------------------------------------------------------------------------

    public void setStrategy(AllocationStrategy strategy) {
        Objects.requireNonNull(strategy, "Strategy must not be null.");
        this.strategy = strategy;
    }

    public AllocationStrategy getStrategy() {
        return strategy;
    }

    // -------------------------------------------------------------------------
    // Resource management
    // -------------------------------------------------------------------------

    public Resource addResource(String name, int capacity) {
        String id = "R" + resourceCounter++;
        Resource resource = new Resource(id, name, capacity);
        resources.put(id, resource);
        return resource;
    }

    public Optional<Resource> getResource(String id) {
        return Optional.ofNullable(resources.get(id));
    }

    public Collection<Resource> getAllResources() {
        return Collections.unmodifiableCollection(resources.values());
    }

    public boolean removeResource(String id) {
        Resource resource = resources.get(id);
        if (resource == null) {
            return false;
        }
        // Re-queue all tasks that were assigned to this resource
        for (Task task : resource.getAssignedTasks()) {
            task.setStatus(TaskStatus.PENDING);
            task.setAssignedResourceId(null);
        }
        resources.remove(id);
        return true;
    }

    // -------------------------------------------------------------------------
    // Task management
    // -------------------------------------------------------------------------

    public Task addTask(String name, Priority priority, LocalDateTime deadline) {
        String id = "T" + taskCounter++;
        Task task = new Task(id, name, priority, deadline);
        tasks.put(id, task);
        return task;
    }

    public Optional<Task> getTask(String id) {
        return Optional.ofNullable(tasks.get(id));
    }

    public Collection<Task> getAllTasks() {
        return Collections.unmodifiableCollection(tasks.values());
    }

    // -------------------------------------------------------------------------
    // Allocation
    // -------------------------------------------------------------------------

    /**
     * Allocates the given task using the active strategy.
     *
     * @param taskId the ID of the task to allocate
     * @return the resource the task was allocated to, or empty if allocation failed
     */
    public Optional<Resource> allocateTask(String taskId) {
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new NoSuchElementException("Task not found: " + taskId);
        }
        if (task.getStatus() == TaskStatus.ASSIGNED || task.getStatus() == TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Task " + taskId + " is already assigned.");
        }
        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CANCELLED) {
            throw new IllegalStateException("Task " + taskId + " cannot be re-allocated (status: " + task.getStatus() + ").");
        }

        Resource chosen = strategy.allocate(task, new ArrayList<>(resources.values()));
        if (chosen == null) {
            return Optional.empty();
        }
        chosen.assignTask(task);
        return Optional.of(chosen);
    }

    /**
     * Allocates all PENDING tasks using the active strategy (sorted by priority then deadline).
     *
     * @return a map from task ID to the resource it was allocated to (may be missing if no resource was free)
     */
    public Map<String, Resource> allocateAllPendingTasks() {
        List<Task> pending = tasks.values().stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING)
                .sorted(Comparator
                        .comparingInt((Task t) -> -t.getPriority().getValue())
                        .thenComparing(Task::getDeadline))
                .collect(java.util.stream.Collectors.toList());

        Map<String, Resource> results = new LinkedHashMap<>();
        for (Task task : pending) {
            Resource chosen = strategy.allocate(task, new ArrayList<>(resources.values()));
            if (chosen != null) {
                chosen.assignTask(task);
                results.put(task.getId(), chosen);
            }
        }
        return results;
    }

    /**
     * Marks a task as completed and releases it from its resource.
     *
     * @param taskId the ID of the task to complete
     * @return true if the task was successfully completed
     */
    public boolean completeTask(String taskId) {
        Task task = tasks.get(taskId);
        if (task == null) {
            return false;
        }
        String resourceId = task.getAssignedResourceId();
        if (resourceId != null) {
            Resource resource = resources.get(resourceId);
            if (resource != null) {
                resource.releaseTask(taskId);
            }
        }
        task.setStatus(TaskStatus.COMPLETED);
        task.setAssignedResourceId(null);
        return true;
    }

    /**
     * Cancels a task, releasing it from any resource it was assigned to.
     *
     * @param taskId the ID of the task to cancel
     * @return true if the task was successfully cancelled
     */
    public boolean cancelTask(String taskId) {
        Task task = tasks.get(taskId);
        if (task == null) {
            return false;
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            return false;
        }
        String resourceId = task.getAssignedResourceId();
        if (resourceId != null) {
            Resource resource = resources.get(resourceId);
            if (resource != null) {
                resource.releaseTask(taskId);
            }
        }
        task.setStatus(TaskStatus.CANCELLED);
        task.setAssignedResourceId(null);
        return true;
    }

    // -------------------------------------------------------------------------
    // Reporting
    // -------------------------------------------------------------------------

    /**
     * Returns a formatted load report for all resources.
     */
    public String generateLoadReport() {
        if (resources.isEmpty()) {
            return "No resources registered.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s %-20s %-10s %-10s %-10s%n",
                "ID", "Name", "Capacity", "Load", "Load %"));
        sb.append("-".repeat(60)).append(System.lineSeparator());
        for (Resource r : resources.values()) {
            sb.append(String.format("%-6s %-20s %-10d %-10d %-10.1f%%%n",
                    r.getId(), r.getName(), r.getCapacity(),
                    r.getCurrentLoad(), r.getLoadPercentage()));
        }
        return sb.toString();
    }

    /**
     * Returns a formatted task summary grouped by status.
     */
    public String generateTaskSummary() {
        if (tasks.isEmpty()) {
            return "No tasks registered.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s %-20s %-8s %-20s %-12s %-8s%n",
                "ID", "Name", "Priority", "Deadline", "Status", "Resource"));
        sb.append("-".repeat(78)).append(System.lineSeparator());
        for (Task t : tasks.values()) {
            sb.append(String.format("%-6s %-20s %-8s %-20s %-12s %-8s%n",
                    t.getId(),
                    truncate(t.getName(), 20),
                    t.getPriority(),
                    t.getDeadline().toString().replace("T", " ").substring(0, 16),
                    t.getStatus(),
                    t.getAssignedResourceId() != null ? t.getAssignedResourceId() : "-"));
        }
        return sb.toString();
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
