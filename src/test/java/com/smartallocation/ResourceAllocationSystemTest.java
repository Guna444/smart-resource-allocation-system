package com.smartallocation;

import com.smartallocation.enums.Priority;
import com.smartallocation.enums.TaskStatus;
import com.smartallocation.model.Resource;
import com.smartallocation.model.Task;
import com.smartallocation.service.ResourceAllocationSystem;
import com.smartallocation.strategy.DeadlineBasedStrategy;
import com.smartallocation.strategy.PriorityBasedStrategy;
import com.smartallocation.strategy.RoundRobinStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ResourceAllocationSystemTest {

    private ResourceAllocationSystem system;
    private LocalDateTime futureDeadline;
    private LocalDateTime urgentDeadline;

    @BeforeEach
    void setUp() {
        system = new ResourceAllocationSystem();
        futureDeadline = LocalDateTime.now().plusDays(7);
        urgentDeadline = LocalDateTime.now().plusHours(1);
    }

    // -------------------------------------------------------------------------
    // Resource tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Add resource increases resource count")
    void addResource_increasesCount() {
        assertTrue(system.getAllResources().isEmpty());
        system.addResource("Developer A", 3);
        assertEquals(1, system.getAllResources().size());
    }

    @Test
    @DisplayName("Added resource has correct fields and generated ID")
    void addResource_hasCorrectFields() {
        Resource r = system.addResource("QA Engineer", 2);
        assertEquals("R1", r.getId());
        assertEquals("QA Engineer", r.getName());
        assertEquals(2, r.getCapacity());
        assertEquals(0, r.getCurrentLoad());
        assertTrue(r.isAvailable());
    }

    @Test
    @DisplayName("Resource IDs are auto-incremented")
    void addResource_autoIncrementId() {
        Resource r1 = system.addResource("A", 1);
        Resource r2 = system.addResource("B", 1);
        assertEquals("R1", r1.getId());
        assertEquals("R2", r2.getId());
    }

    @Test
    @DisplayName("Resource capacity must be positive")
    void addResource_invalidCapacityThrows() {
        assertThrows(IllegalArgumentException.class, () -> system.addResource("Bad", 0));
        assertThrows(IllegalArgumentException.class, () -> system.addResource("Bad", -1));
    }

    @Test
    @DisplayName("Remove resource re-queues its tasks as PENDING")
    void removeResource_reQueuesTasks() {
        Resource r = system.addResource("Dev", 2);
        Task t = system.addTask("Task1", Priority.HIGH, futureDeadline);
        system.allocateTask(t.getId());
        assertEquals(TaskStatus.ASSIGNED, t.getStatus());

        system.removeResource(r.getId());

        assertEquals(TaskStatus.PENDING, t.getStatus());
        assertNull(t.getAssignedResourceId());
        assertTrue(system.getAllResources().isEmpty());
    }

    // -------------------------------------------------------------------------
    // Task tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Add task creates task with PENDING status and generated ID")
    void addTask_createsPendingTask() {
        Task t = system.addTask("Fix bug", Priority.HIGH, futureDeadline);
        assertEquals("T1", t.getId());
        assertEquals("Fix bug", t.getName());
        assertEquals(Priority.HIGH, t.getPriority());
        assertEquals(TaskStatus.PENDING, t.getStatus());
        assertNull(t.getAssignedResourceId());
    }

    @Test
    @DisplayName("Task IDs are auto-incremented")
    void addTask_autoIncrementId() {
        Task t1 = system.addTask("A", Priority.LOW, futureDeadline);
        Task t2 = system.addTask("B", Priority.LOW, futureDeadline);
        assertEquals("T1", t1.getId());
        assertEquals("T2", t2.getId());
    }

    @Test
    @DisplayName("Complete task sets COMPLETED status and releases from resource")
    void completeTask_releasesResource() {
        system.addResource("Dev", 1);
        Task t = system.addTask("Deploy", Priority.MEDIUM, futureDeadline);
        system.allocateTask(t.getId());
        assertEquals(TaskStatus.ASSIGNED, t.getStatus());

        boolean result = system.completeTask(t.getId());
        assertTrue(result);
        assertEquals(TaskStatus.COMPLETED, t.getStatus());
        assertNull(t.getAssignedResourceId());

        // Resource should be free again
        Resource r = system.getAllResources().iterator().next();
        assertEquals(0, r.getCurrentLoad());
    }

    @Test
    @DisplayName("Complete non-existent task returns false")
    void completeTask_nonExistentReturnsFalse() {
        assertFalse(system.completeTask("T999"));
    }

    @Test
    @DisplayName("Cancel task sets CANCELLED status and releases from resource")
    void cancelTask_releasesResource() {
        system.addResource("Dev", 1);
        Task t = system.addTask("Research", Priority.LOW, futureDeadline);
        system.allocateTask(t.getId());

        boolean result = system.cancelTask(t.getId());
        assertTrue(result);
        assertEquals(TaskStatus.CANCELLED, t.getStatus());
        assertNull(t.getAssignedResourceId());
        assertEquals(0, system.getAllResources().iterator().next().getCurrentLoad());
    }

    @Test
    @DisplayName("Cancel a completed task returns false")
    void cancelTask_completedTaskReturnsFalse() {
        system.addResource("Dev", 1);
        Task t = system.addTask("Work", Priority.HIGH, futureDeadline);
        system.allocateTask(t.getId());
        system.completeTask(t.getId());

        assertFalse(system.cancelTask(t.getId()));
    }

    // -------------------------------------------------------------------------
    // Allocation tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Allocate task assigns it to an available resource")
    void allocateTask_assignsToAvailableResource() {
        system.addResource("Dev", 2);
        Task t = system.addTask("Build feature", Priority.HIGH, futureDeadline);

        Optional<Resource> result = system.allocateTask(t.getId());
        assertTrue(result.isPresent());
        assertEquals(TaskStatus.ASSIGNED, t.getStatus());
        assertNotNull(t.getAssignedResourceId());
        assertEquals("R1", t.getAssignedResourceId());
    }

    @Test
    @DisplayName("Allocate returns empty when no resources exist")
    void allocateTask_noResources_returnsEmpty() {
        Task t = system.addTask("Orphan", Priority.LOW, futureDeadline);
        Optional<Resource> result = system.allocateTask(t.getId());
        assertFalse(result.isPresent());
        assertEquals(TaskStatus.PENDING, t.getStatus());
    }

    @Test
    @DisplayName("Allocate returns empty when all resources are at capacity")
    void allocateTask_fullCapacity_returnsEmpty() {
        system.addResource("Dev", 1);
        Task t1 = system.addTask("T1", Priority.HIGH, futureDeadline);
        Task t2 = system.addTask("T2", Priority.HIGH, futureDeadline);
        system.allocateTask(t1.getId());

        Optional<Resource> result = system.allocateTask(t2.getId());
        assertFalse(result.isPresent());
        assertEquals(TaskStatus.PENDING, t2.getStatus());
    }

    @Test
    @DisplayName("Allocate already-assigned task throws IllegalStateException")
    void allocateTask_alreadyAssigned_throws() {
        system.addResource("Dev", 2);
        Task t = system.addTask("Do work", Priority.MEDIUM, futureDeadline);
        system.allocateTask(t.getId());
        assertThrows(IllegalStateException.class, () -> system.allocateTask(t.getId()));
    }

    @Test
    @DisplayName("Allocate non-existent task throws NoSuchElementException")
    void allocateTask_notFound_throws() {
        assertThrows(NoSuchElementException.class, () -> system.allocateTask("T999"));
    }

    @Test
    @DisplayName("Allocate all pending tasks processes multiple tasks")
    void allocateAllPendingTasks_allocatesMultiple() {
        system.addResource("Dev1", 3);
        system.addResource("Dev2", 3);
        system.addTask("Task A", Priority.HIGH, futureDeadline);
        system.addTask("Task B", Priority.MEDIUM, futureDeadline);
        system.addTask("Task C", Priority.LOW, futureDeadline);

        Map<String, Resource> results = system.allocateAllPendingTasks();
        assertEquals(3, results.size());
        system.getAllTasks().forEach(t -> assertEquals(TaskStatus.ASSIGNED, t.getStatus()));
    }

    @Test
    @DisplayName("Allocate all pending tasks skips already-assigned tasks")
    void allocateAllPendingTasks_skipsAssigned() {
        system.addResource("Dev", 2);
        Task t1 = system.addTask("A", Priority.HIGH, futureDeadline);
        Task t2 = system.addTask("B", Priority.LOW, futureDeadline);
        system.allocateTask(t1.getId());

        Map<String, Resource> results = system.allocateAllPendingTasks();
        assertEquals(1, results.size());
        assertTrue(results.containsKey(t2.getId()));
    }

    // -------------------------------------------------------------------------
    // Strategy tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Default strategy is PriorityBasedStrategy")
    void defaultStrategy_isPriorityBased() {
        assertInstanceOf(PriorityBasedStrategy.class, system.getStrategy());
    }

    @Test
    @DisplayName("Can switch to DeadlineBasedStrategy")
    void setStrategy_deadlineBased() {
        system.setStrategy(new DeadlineBasedStrategy());
        assertInstanceOf(DeadlineBasedStrategy.class, system.getStrategy());
    }

    @Test
    @DisplayName("Can switch to RoundRobinStrategy")
    void setStrategy_roundRobin() {
        system.setStrategy(new RoundRobinStrategy());
        assertInstanceOf(RoundRobinStrategy.class, system.getStrategy());
    }

    @Test
    @DisplayName("Null strategy throws NullPointerException")
    void setStrategy_null_throws() {
        assertThrows(NullPointerException.class, () -> system.setStrategy(null));
    }

    @Test
    @DisplayName("PriorityBased strategy prefers least-loaded resource")
    void priorityBasedStrategy_prefersLeastLoaded() {
        system.setStrategy(new PriorityBasedStrategy());
        Resource r1 = system.addResource("Heavy", 5);
        Resource r2 = system.addResource("Light", 5);

        // Pre-load r1 with 3 tasks directly to make it the more-loaded resource
        for (int i = 0; i < 3; i++) {
            Task filler = system.addTask("Filler" + i, Priority.LOW, futureDeadline);
            r1.assignTask(filler);
        }
        // r1 is at 60% load, r2 is at 0% — next task should go to r2
        Task t = system.addTask("Important", Priority.HIGH, futureDeadline);
        Optional<Resource> result = system.allocateTask(t.getId());
        assertTrue(result.isPresent());
        assertEquals(r2.getId(), result.get().getId());
    }

    @Test
    @DisplayName("RoundRobin strategy distributes across resources")
    void roundRobinStrategy_distributes() {
        system.setStrategy(new RoundRobinStrategy());
        system.addResource("A", 5);
        system.addResource("B", 5);

        Task t1 = system.addTask("T1", Priority.LOW, futureDeadline);
        Task t2 = system.addTask("T2", Priority.LOW, futureDeadline);
        system.allocateTask(t1.getId());
        system.allocateTask(t2.getId());

        // Should go to different resources
        assertNotEquals(t1.getAssignedResourceId(), t2.getAssignedResourceId());
    }

    @Test
    @DisplayName("DeadlineBasedStrategy allocates urgent tasks to most-available resource")
    void deadlineBasedStrategy_urgentTaskGoesToMostAvailable() {
        system.setStrategy(new DeadlineBasedStrategy());
        Resource r1 = system.addResource("Small", 2);
        Resource r2 = system.addResource("Large", 5);

        Task t = system.addTask("Urgent", Priority.HIGH, urgentDeadline);
        Optional<Resource> result = system.allocateTask(t.getId());
        assertTrue(result.isPresent());
        // Large resource has more remaining capacity
        assertEquals(r2.getId(), result.get().getId());
    }

    // -------------------------------------------------------------------------
    // Load and reporting tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Resource load percentage updates after task assignment")
    void resourceLoad_updatesAfterAssignment() {
        Resource r = system.addResource("Dev", 4);
        assertEquals(0.0, r.getLoadPercentage(), 0.001);

        Task t = system.addTask("Work", Priority.HIGH, futureDeadline);
        system.allocateTask(t.getId());

        assertEquals(25.0, r.getLoadPercentage(), 0.001);
    }

    @Test
    @DisplayName("Load report is generated without error")
    void generateLoadReport_nonEmpty() {
        system.addResource("Dev", 2);
        String report = system.generateLoadReport();
        assertTrue(report.contains("Dev"));
        assertTrue(report.contains("R1"));
    }

    @Test
    @DisplayName("Load report returns message when no resources")
    void generateLoadReport_empty() {
        assertEquals("No resources registered.", system.generateLoadReport());
    }

    @Test
    @DisplayName("Task summary is generated without error")
    void generateTaskSummary_nonEmpty() {
        system.addTask("My Task", Priority.HIGH, futureDeadline);
        String summary = system.generateTaskSummary();
        assertTrue(summary.contains("My Task"));
        assertTrue(summary.contains("T1"));
    }

    @Test
    @DisplayName("Task summary returns message when no tasks")
    void generateTaskSummary_empty() {
        assertEquals("No tasks registered.", system.generateTaskSummary());
    }
}
