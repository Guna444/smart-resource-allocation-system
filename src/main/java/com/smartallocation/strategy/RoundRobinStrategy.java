package com.smartallocation.strategy;

import com.smartallocation.model.Resource;
import com.smartallocation.model.Task;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Round-Robin allocation strategy.
 *
 * Distributes tasks evenly across resources in a cyclic order,
 * skipping any resource that has reached its maximum capacity.
 */
public class RoundRobinStrategy implements AllocationStrategy {

    private final AtomicInteger nextIndex = new AtomicInteger(0);

    @Override
    public Resource allocate(Task task, List<Resource> resources) {
        if (resources.isEmpty()) {
            return null;
        }
        int size = resources.size();
        int attempts = 0;
        while (attempts < size) {
            int index = nextIndex.getAndUpdate(i -> (i + 1) % size);
            Resource candidate = resources.get(index);
            if (candidate.isAvailable()) {
                return candidate;
            }
            attempts++;
        }
        return null;
    }

    @Override
    public String getStrategyName() {
        return "Round-Robin (cyclic distribution)";
    }
}
