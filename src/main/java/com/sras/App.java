package com.sras;

import com.sras.cli.Menu;
import com.sras.repo.AssignmentRepository;
import com.sras.repo.ResourceRepository;
import com.sras.repo.TaskRepository;
import com.sras.service.AllocationEngine;
import com.sras.service.MetricsService;
import com.sras.service.SeedData;

public class App {
    public static void main(String[] args) {
        TaskRepository taskRepo = new TaskRepository();
        ResourceRepository resourceRepo = new ResourceRepository();
        AssignmentRepository assignmentRepo = new AssignmentRepository();

        AllocationEngine engine = new AllocationEngine(taskRepo, resourceRepo, assignmentRepo);
        MetricsService metrics = new MetricsService(taskRepo, resourceRepo, assignmentRepo);

        SeedData.seed(resourceRepo, taskRepo);

        Menu menu = new Menu(engine, metrics, taskRepo, resourceRepo, assignmentRepo);
        menu.run();
    }
}