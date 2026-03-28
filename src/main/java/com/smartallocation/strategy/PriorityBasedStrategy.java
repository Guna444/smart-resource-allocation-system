package com.smartallocation.strategy;

import com.smartallocation.model.Resource;
import com.smartallocation.model.Task;

import java.util.Comparator;
import java.util.List;

/**
 * Allocates a task to the resource with the lowest current load.
 * When multiple resources have the same load, high-priority tasks
 * are sent to the resource with the most remaining capacity so that
 * capacity is reserved proportionally to priority.
 */
public class PriorityBasedStrategy implements AllocationStrategy {

    @Override
    public Resource allocate(Task task, List<Resource> resources) {
        return resources.stream()
                .filter(Resource::isAvailable)
                .min(Comparator
                        .comparingDouble(Resource::getLoadPercentage)
                        .thenComparingInt(r -> -(r.getCapacity() - r.getCurrentLoad()) * task.getPriority().getValue()))
                .orElse(null);
    }

    @Override
    public String getStrategyName() {
        return "Priority-Based (least-loaded, priority-weighted)";
    }
}
