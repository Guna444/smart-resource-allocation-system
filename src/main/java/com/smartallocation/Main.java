package com.smartallocation;

import com.smartallocation.enums.Priority;
import com.smartallocation.model.Resource;
import com.smartallocation.model.Task;
import com.smartallocation.service.ResourceAllocationSystem;
import com.smartallocation.strategy.DeadlineBasedStrategy;
import com.smartallocation.strategy.PriorityBasedStrategy;
import com.smartallocation.strategy.RoundRobinStrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Menu-driven console entry point for the Smart Resource Allocation System.
 */
public class Main {

    private static final DateTimeFormatter DT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ResourceAllocationSystem system = new ResourceAllocationSystem();
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        printBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1 -> handleResourceMenu();
                case 2 -> handleTaskMenu();
                case 3 -> handleAllocationMenu();
                case 4 -> handleStrategyMenu();
                case 5 -> handleReportsMenu();
                case 0 -> {
                    System.out.println("\nGoodbye!");
                    running = false;
                }
                default -> System.out.println("  [!] Invalid choice. Please try again.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Menus
    // -------------------------------------------------------------------------

    private void printMainMenu() {
        System.out.println("\n========================================");
        System.out.println("  SMART RESOURCE ALLOCATION SYSTEM");
        System.out.println("  Active strategy: " + system.getStrategy().getStrategyName());
        System.out.println("========================================");
        System.out.println("  1. Resource Management");
        System.out.println("  2. Task Management");
        System.out.println("  3. Allocation");
        System.out.println("  4. Switch Strategy");
        System.out.println("  5. Reports & Load Status");
        System.out.println("  0. Exit");
        System.out.println("----------------------------------------");
    }

    private void handleResourceMenu() {
        System.out.println("\n--- Resource Management ---");
        System.out.println("  1. Add Resource");
        System.out.println("  2. List Resources");
        System.out.println("  3. Remove Resource");
        System.out.println("  0. Back");
        int choice = readInt("Enter choice: ");
        switch (choice) {
            case 1 -> addResource();
            case 2 -> listResources();
            case 3 -> removeResource();
            case 0 -> { }
            default -> System.out.println("  [!] Invalid choice.");
        }
    }

    private void handleTaskMenu() {
        System.out.println("\n--- Task Management ---");
        System.out.println("  1. Add Task");
        System.out.println("  2. List Tasks");
        System.out.println("  3. Complete Task");
        System.out.println("  4. Cancel Task");
        System.out.println("  0. Back");
        int choice = readInt("Enter choice: ");
        switch (choice) {
            case 1 -> addTask();
            case 2 -> listTasks();
            case 3 -> completeTask();
            case 4 -> cancelTask();
            case 0 -> { }
            default -> System.out.println("  [!] Invalid choice.");
        }
    }

    private void handleAllocationMenu() {
        System.out.println("\n--- Allocation ---");
        System.out.println("  1. Allocate a specific task");
        System.out.println("  2. Allocate all pending tasks");
        System.out.println("  0. Back");
        int choice = readInt("Enter choice: ");
        switch (choice) {
            case 1 -> allocateTask();
            case 2 -> allocateAllPending();
            case 0 -> { }
            default -> System.out.println("  [!] Invalid choice.");
        }
    }

    private void handleStrategyMenu() {
        System.out.println("\n--- Switch Allocation Strategy ---");
        System.out.println("  Current: " + system.getStrategy().getStrategyName());
        System.out.println("  1. Priority-Based (least-loaded, priority-weighted)");
        System.out.println("  2. Deadline-Based (urgency-aware capacity allocation)");
        System.out.println("  3. Round-Robin (cyclic distribution)");
        System.out.println("  0. Back");
        int choice = readInt("Enter choice: ");
        switch (choice) {
            case 1 -> {
                system.setStrategy(new PriorityBasedStrategy());
                System.out.println("  [OK] Strategy set to Priority-Based.");
            }
            case 2 -> {
                system.setStrategy(new DeadlineBasedStrategy());
                System.out.println("  [OK] Strategy set to Deadline-Based.");
            }
            case 3 -> {
                system.setStrategy(new RoundRobinStrategy());
                System.out.println("  [OK] Strategy set to Round-Robin.");
            }
            case 0 -> { }
            default -> System.out.println("  [!] Invalid choice.");
        }
    }

    private void handleReportsMenu() {
        System.out.println("\n--- Reports & Load Status ---");
        System.out.println("  1. Resource Load Report");
        System.out.println("  2. Task Summary");
        System.out.println("  3. Full Status (both)");
        System.out.println("  0. Back");
        int choice = readInt("Enter choice: ");
        switch (choice) {
            case 1 -> {
                System.out.println("\n--- Resource Load Report ---");
                System.out.print(system.generateLoadReport());
            }
            case 2 -> {
                System.out.println("\n--- Task Summary ---");
                System.out.print(system.generateTaskSummary());
            }
            case 3 -> {
                System.out.println("\n--- Resource Load Report ---");
                System.out.print(system.generateLoadReport());
                System.out.println("\n--- Task Summary ---");
                System.out.print(system.generateTaskSummary());
            }
            case 0 -> { }
            default -> System.out.println("  [!] Invalid choice.");
        }
    }

    // -------------------------------------------------------------------------
    // Resource actions
    // -------------------------------------------------------------------------

    private void addResource() {
        System.out.println("\n  Add Resource");
        String name = readString("  Resource name: ");
        int capacity = readPositiveInt("  Max concurrent tasks (capacity): ");
        Resource r = system.addResource(name, capacity);
        System.out.printf("  [OK] Resource added: %s (ID: %s, capacity: %d)%n",
                r.getName(), r.getId(), r.getCapacity());
    }

    private void listResources() {
        System.out.println("\n  --- Resources ---");
        if (system.getAllResources().isEmpty()) {
            System.out.println("  No resources registered.");
            return;
        }
        system.getAllResources().forEach(r -> System.out.printf(
                "  %s | %-20s | capacity: %d | load: %d/%d (%.1f%%)%n",
                r.getId(), r.getName(), r.getCapacity(),
                r.getCurrentLoad(), r.getCapacity(), r.getLoadPercentage()));
    }

    private void removeResource() {
        listResources();
        if (system.getAllResources().isEmpty()) return;
        String id = readString("  Enter Resource ID to remove: ").toUpperCase();
        if (system.removeResource(id)) {
            System.out.println("  [OK] Resource " + id + " removed. Assigned tasks set back to PENDING.");
        } else {
            System.out.println("  [!] Resource not found: " + id);
        }
    }

    // -------------------------------------------------------------------------
    // Task actions
    // -------------------------------------------------------------------------

    private void addTask() {
        System.out.println("\n  Add Task");
        String name = readString("  Task name: ");
        Priority priority = readPriority();
        LocalDateTime deadline = readDeadline();
        Task t = system.addTask(name, priority, deadline);
        System.out.printf("  [OK] Task added: %s (ID: %s, priority: %s, deadline: %s)%n",
                t.getName(), t.getId(), t.getPriority(), t.getDeadline().format(DT_FORMATTER));
    }

    private void listTasks() {
        System.out.println("\n  --- Tasks ---");
        if (system.getAllTasks().isEmpty()) {
            System.out.println("  No tasks registered.");
            return;
        }
        System.out.print(system.generateTaskSummary());
    }

    private void completeTask() {
        listTasks();
        if (system.getAllTasks().isEmpty()) return;
        String id = readString("  Enter Task ID to mark as complete: ").toUpperCase();
        if (system.completeTask(id)) {
            System.out.println("  [OK] Task " + id + " marked as COMPLETED and released from resource.");
        } else {
            System.out.println("  [!] Task not found: " + id);
        }
    }

    private void cancelTask() {
        listTasks();
        if (system.getAllTasks().isEmpty()) return;
        String id = readString("  Enter Task ID to cancel: ").toUpperCase();
        if (system.cancelTask(id)) {
            System.out.println("  [OK] Task " + id + " CANCELLED.");
        } else {
            System.out.println("  [!] Task not found or already completed: " + id);
        }
    }

    // -------------------------------------------------------------------------
    // Allocation actions
    // -------------------------------------------------------------------------

    private void allocateTask() {
        listTasks();
        if (system.getAllTasks().isEmpty()) return;
        String id = readString("  Enter Task ID to allocate: ").toUpperCase();
        try {
            Optional<Resource> result = system.allocateTask(id);
            if (result.isPresent()) {
                System.out.printf("  [OK] Task %s allocated to resource %s (%s).%n",
                        id, result.get().getId(), result.get().getName());
            } else {
                System.out.println("  [!] No available resource found for task " + id + ".");
            }
        } catch (IllegalStateException e) {
            System.out.println("  [!] " + e.getMessage());
        } catch (java.util.NoSuchElementException e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    private void allocateAllPending() {
        Map<String, Resource> results = system.allocateAllPendingTasks();
        if (results.isEmpty()) {
            System.out.println("  [!] No pending tasks were allocated (no pending tasks or no available resources).");
            return;
        }
        System.out.println("  [OK] Allocated " + results.size() + " task(s):");
        results.forEach((taskId, resource) ->
                System.out.printf("       Task %-6s -> Resource %s (%s)%n",
                        taskId, resource.getId(), resource.getName()));
    }

    // -------------------------------------------------------------------------
    // Input helpers
    // -------------------------------------------------------------------------

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid integer.");
            }
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value > 0) return value;
            System.out.println("  [!] Value must be greater than zero.");
        }
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private Priority readPriority() {
        System.out.println("  Priority options: 1=HIGH, 2=MEDIUM, 3=LOW");
        while (true) {
            int choice = readInt("  Select priority: ");
            switch (choice) {
                case 1 -> { return Priority.HIGH; }
                case 2 -> { return Priority.MEDIUM; }
                case 3 -> { return Priority.LOW; }
                default -> System.out.println("  [!] Enter 1, 2, or 3.");
            }
        }
    }

    private LocalDateTime readDeadline() {
        System.out.println("  Deadline format: yyyy-MM-dd HH:mm  (e.g. 2025-12-31 18:00)");
        while (true) {
            String input = readString("  Enter deadline: ");
            try {
                return LocalDateTime.parse(input, DT_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Invalid date/time format. Please use yyyy-MM-dd HH:mm.");
            }
        }
    }

    private void printBanner() {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   Smart Resource Allocation System       ║");
        System.out.println("║   LLD — Strategy Pattern Edition         ║");
        System.out.println("╚══════════════════════════════════════════╝");
    }
}
