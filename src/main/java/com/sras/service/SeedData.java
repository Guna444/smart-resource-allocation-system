package com.sras.service;

import com.sras.domain.Priority;
import com.sras.domain.Resource;
import com.sras.domain.Skill;
import com.sras.domain.Task;
import com.sras.repo.ResourceRepository;
import com.sras.repo.TaskRepository;

import java.time.Instant;
import java.util.EnumSet;

public final class SeedData {
    private SeedData() {}

    public static void seed(ResourceRepository resourceRepo, TaskRepository taskRepo) {
        // Resources
        resourceRepo.add(Resource.create("Alice", EnumSet.of(Skill.JAVA, Skill.DB), 200));
        resourceRepo.add(Resource.create("Bob", EnumSet.of(Skill.JAVA, Skill.NETWORKING), 160));
        resourceRepo.add(Resource.create("Chandra", EnumSet.of(Skill.OPS, Skill.NETWORKING), 140));
        resourceRepo.add(Resource.create("Dina", EnumSet.of(Skill.PM, Skill.DB), 120));

        // Tasks
        taskRepo.add(Task.create("Fix DB connection pool", Priority.CRITICAL,
                Instant.now().plusSeconds(3600), 60, EnumSet.of(Skill.DB, Skill.OPS)));

        taskRepo.add(Task.create("Implement task allocation strategy", Priority.HIGH,
                Instant.now().plusSeconds(3 * 3600), 90, EnumSet.of(Skill.JAVA)));

        taskRepo.add(Task.create("Network latency investigation", Priority.MEDIUM,
                Instant.now().plusSeconds(6 * 3600), 120, EnumSet.of(Skill.NETWORKING)));

        taskRepo.add(Task.create("Update sprint plan", Priority.LOW,
                Instant.now().plusSeconds(24 * 3600), 45, EnumSet.of(Skill.PM)));
    }
}