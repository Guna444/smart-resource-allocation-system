package com.sras.domain;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public final class Resource {
    private final String id;
    private final String name;
    private final Set<Skill> skills;
    private final int capacityPoints;
    private final Instant createdAt;

    private ResourceStatus status;
    private int loadPoints; // current used capacity

    private Resource(String id,
                     String name,
                     Set<Skill> skills,
                     int capacityPoints,
                     Instant createdAt,
                     ResourceStatus status,
                     int loadPoints) {
        this.id = id;
        this.name = name;
        this.skills = skills;
        this.capacityPoints = capacityPoints;
        this.createdAt = createdAt;
        this.status = status;
        this.loadPoints = loadPoints;
    }

    public static Resource create(String name, Set<Skill> skills, int capacityPoints) {
        return new Resource(
                UUID.randomUUID().toString(),
                name,
                skills == null ? Collections.emptySet() : Set.copyOf(skills),
                capacityPoints,
                Instant.now(),
                ResourceStatus.ACTIVE,
                0
        );
    }

    public String id() { return id; }
    public String name() { return name; }
    public Set<Skill> skills() { return skills; }
    public int capacityPoints() { return capacityPoints; }
    public Instant createdAt() { return createdAt; }
    public ResourceStatus status() { return status; }
    public int loadPoints() { return loadPoints; }

    public boolean isActive() { return status == ResourceStatus.ACTIVE; }

    public void setInactive() { this.status = ResourceStatus.INACTIVE; }
    public void setActive() { this.status = ResourceStatus.ACTIVE; }

    public boolean canTake(int additionalPoints) {
        return isActive() && (loadPoints + additionalPoints) <= capacityPoints;
    }

    public void addLoad(int points) {
        this.loadPoints += points;
        if (this.loadPoints < 0) this.loadPoints = 0;
    }

    public void releaseLoad(int points) {
        this.loadPoints -= points;
        if (this.loadPoints < 0) this.loadPoints = 0;
    }

    public double utilization() {
        if (capacityPoints <= 0) return 0.0;
        return Math.min(1.0, (double) loadPoints / (double) capacityPoints);
    }

    public String toDisplayString() {
        return "Resource{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", skills=" + skills +
                ", capacity=" + capacityPoints +
                ", load=" + loadPoints +
                ", util=" + String.format("%.2f", utilization() * 100) + "%" +
                ", status=" + status +
                '}';
    }
}