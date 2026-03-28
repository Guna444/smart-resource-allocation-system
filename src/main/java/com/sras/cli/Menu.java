package com.sras.cli;

import com.sras.domain.*;
import com.sras.repo.AssignmentRepository;
import com.sras.repo.ResourceRepository;
import com.sras.repo.TaskRepository;
import com.sras.service.AllocationEngine;
import com.sras.service.MetricsService;
import com.sras.service.Validation;
import com.sras.strategy.*;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public final class Menu {
    private final ConsoleIO io = new ConsoleIO();

    private final AllocationEngine engine;
    private final MetricsService metrics;
    private final TaskRepository taskRepo;
    private final ResourceRepository resourceRepo;
    private final AssignmentRepository assignmentRepo;

    private AllocationStrategy strategy = new SkillMatchBalancedStrategy();

    public Menu(AllocationEngine engine,
                MetricsService metrics,
                TaskRepository taskRepo,
                ResourceRepository resourceRepo,
                AssignmentRepository assignmentRepo) {
        this.engine = engine;
        this.metrics = metrics;
        this.taskRepo = taskRepo;
        this.resourceRepo = resourceRepo;
        this.assignmentRepo = assignmentRepo;
    }

    public void run() {
        while (true) {
            io.printDivider();
            io.println("Smart Resource Allocation System (LLD) - March 2026");
            io.println("Current strategy: " + strategy.name());
            io.println("");
            io.println("1) List tasks");
            io.println("2) List resources");
            io.println("3) Create task");
            io.println("4) Create resource");
            io.println("5) Allocate next task (single)");
            io.println("6) Allocate backlog (batch)");
            io.println("7) Mark task completed");
            io.println("8) Release resource load (simulate time passed)");
            io.println("9) Show metrics");
            io.println("10) Switch strategy");
            io.println("0) Exit");
            io.printDivider();

            String choice = io.readLine("Choose: ").trim();
            try {
                switch (choice) {
                    case "1" -> listTasks();
                    case "2" -> listResources();
                    case "3" -> createTask();
                    case "4" -> createResource();
                    case "5" -> allocateOne();
                    case "6" -> allocateBatch();
                    case "7" -> completeTask();
                    case "8" -> releaseLoad();
                    case "9" -> showMetrics();
                    case "10" -> switchStrategy();
                    case "0" -> { io.println("Bye."); return; }
                    default -> io.println("Invalid choice.");
                }
            } catch (Exception e) {
                io.println("Operation failed: " + e.getMessage());
            }
        }
    }

    private void listTasks() {
        io.printDivider();
        io.println("TASKS (backlog ordering is priority+deadline+createdAt):");
        for (Task t : taskRepo.listAllInBacklogOrder()) {
            io.println(t.toDisplayString());
        }
        io.println("");
        io.println("Assignments: " + assignmentRepo.count());
    }

    private void listResources() {
        io.printDivider();
        io.println("RESOURCES:");
        for (Resource r : resourceRepo.listAll()) {
            io.println(r.toDisplayString());
        }
    }

    private void createTask() {
        io.printDivider();
        io.println("Create Task");
        String title = Validation.requireNonBlank(io.readLine("Title: "));
        Priority priority = Validation.parseEnum(io.readLine("Priority (LOW/MEDIUM/HIGH/CRITICAL): "), Priority.class);

        int estMinutes = Validation.requireInt(io.readLine("Estimated minutes (e.g., 30): "), 1, 10_000);

        long deadlineEpochSec = Validation.requireLong(
                io.readLine("Deadline epoch seconds (or blank for +2 hours): "),
                0, Long.MAX_VALUE,
                Instant.now().plusSeconds(2 * 3600).getEpochSecond()
        );

        Set<Skill> skills = parseSkills(io.readLine("Required skills comma-separated (JAVA,DB,NETWORKING,OPS,PM): "));

        Task task = Task.create(title, priority, Instant.ofEpochSecond(deadlineEpochSec), estMinutes, skills);
        taskRepo.add(task);
        io.println("Created: " + task.id());
    }

    private void createResource() {
        io.printDivider();
        io.println("Create Resource");
        String name = Validation.requireNonBlank(io.readLine("Name: "));
        int capacity = Validation.requireInt(io.readLine("Capacity points (e.g., 100): "), 1, 1_000_000);
        Set<Skill> skills = parseSkills(io.readLine("Skills comma-separated (JAVA,DB,NETWORKING,OPS,PM): "));
        Resource r = Resource.create(name, skills, capacity);
        resourceRepo.add(r);
        io.println("Created: " + r.id());
    }

    private void allocateOne() {
        io.printDivider();
        Optional<Assignment> assignment = engine.allocateNext(strategy);
        if (assignment.isEmpty()) {
            io.println("No allocation possible (no pending tasks or no eligible resources).");
            return;
        }
        io.println("Allocated:");
        io.println(assignment.get().toDisplayString());
    }

    private void allocateBatch() {
        io.printDivider();
        int max = Validation.requireInt(io.readLine("Max allocations to attempt: "), 1, 100_000);
        int allocated = engine.allocateBacklog(strategy, max);
        io.println("Allocated " + allocated + " task(s).");
    }

    private void completeTask() {
        io.printDivider();
        String id = Validation.requireNonBlank(io.readLine("Task ID to mark completed: "));
        boolean ok = engine.markTaskCompleted(id);
        io.println(ok ? "Task marked completed." : "Task not found or not allocated.");
    }

    private void releaseLoad() {
        io.printDivider();
        String id = Validation.requireNonBlank(io.readLine("Resource ID: "));
        int points = Validation.requireInt(io.readLine("Release load points: "), 1, 1_000_000);
        boolean ok = engine.releaseResourceLoad(id, points);
        io.println(ok ? "Released." : "Resource not found.");
    }

    private void showMetrics() {
        io.printDivider();
        io.println(metrics.summary());
    }

    private void switchStrategy() {
        io.printDivider();
        io.println("1) LowestLoadStrategy");
        io.println("2) PriorityThenDeadlineStrategy");
        io.println("3) SkillMatchBalancedStrategy (recommended)");
        String c = io.readLine("Choose strategy: ").trim();
        this.strategy = switch (c) {
            case "1" -> new LowestLoadStrategy();
            case "2" -> new PriorityThenDeadlineStrategy();
            case "3" -> new SkillMatchBalancedStrategy();
            default -> this.strategy;
        };
        io.println("Now using: " + strategy.name());
    }

    private Set<Skill> parseSkills(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return EnumSet.noneOf(Skill.class);
        }
        String[] parts = raw.split(",");
        EnumSet<Skill> set = EnumSet.noneOf(Skill.class);
        for (String p : parts) {
            String s = p.trim();
            if (s.isEmpty()) continue;
            set.add(Validation.parseEnum(s, Skill.class));
        }
        return set;
    }
}