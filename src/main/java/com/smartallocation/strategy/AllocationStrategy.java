package com.smartallocation.strategy;

import com.smartallocation.model.Resource;
import com.smartallocation.model.Task;

import java.util.List;

public interface AllocationStrategy {

    /**
     * Selects the most suitable resource for the given task from the available resources.
     *
     * @param task      the task to allocate
     * @param resources the list of all registered resources
     * @return the chosen Resource, or null if no resource is available
     */
    Resource allocate(Task task, List<Resource> resources);

    /**
     * Returns a human-readable name for this strategy.
     */
    String getStrategyName();
}
