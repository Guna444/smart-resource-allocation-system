package com.smartallocation.strategy;

import com.smartallocation.model.Resource;
import com.smartallocation.model.Task;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Deadline-Based allocation strategy.
 *
 * Tasks with an earlier (tighter) deadline are allocated first to the
 * resource that has the most remaining capacity, ensuring that
 * time-critical work can be completed before its due date.
 * If the task's deadline has already passed, the strategy still
 * allocates it to the least-loaded available resource so it can be
 * handled as quickly as possible.
 */
public class DeadlineBasedStrategy implements AllocationStrategy {

    @Override
    public Resource allocate(Task task, List<Resource> resources) {
        boolean isTight = task.getDeadline().isBefore(LocalDateTime.now().plusHours(24));

        return resources.stream()
                .filter(Resource::isAvailable)
                .min(isTight
                        ? Comparator.comparingInt(r -> -(r.getCapacity() - r.getCurrentLoad()))
                        : Comparator.comparingDouble(Resource::getLoadPercentage))
                .orElse(null);
    }

    @Override
    public String getStrategyName() {
        return "Deadline-Based (urgency-aware capacity allocation)";
    }
}
