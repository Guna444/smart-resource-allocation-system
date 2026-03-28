package com.sras.strategy;

import com.sras.domain.Resource;
import com.sras.domain.Task;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * For eligible candidates, still pick by lowest utilization.
 * (Task ordering is already priority+deadline in backlog.)
 */
public final class PriorityThenDeadlineStrategy implements AllocationStrategy {
    @Override
    public String name() { return "PriorityThenDeadlineStrategy"; }

    @Override
    public Optional<Resource> select(Task task, List<Resource> eligibleCandidates) {
        return eligibleCandidates.stream()
                .min(Comparator.comparingDouble(Resource::utilization)
                        .thenComparingInt(Resource::loadPoints));
    }
}