package com.sras.strategy;

import com.sras.domain.Resource;
import com.sras.domain.Task;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class LowestLoadStrategy implements AllocationStrategy {
    @Override
    public String name() { return "LowestLoadStrategy"; }

    @Override
    public Optional<Resource> select(Task task, List<Resource> eligibleCandidates) {
        return eligibleCandidates.stream()
                .min(Comparator.comparingInt(Resource::loadPoints)
                        .thenComparingDouble(Resource::utilization));
    }
}