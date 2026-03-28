package com.sras.strategy;

import com.sras.domain.Resource;
import com.sras.domain.Task;

import java.util.List;
import java.util.Optional;

public interface AllocationStrategy {
    String name();

    /**
     * Choose one resource for the task from eligible candidates.
     * Candidates are already filtered by ACTIVE + skills + capacity.
     */
    Optional<Resource> select(Task task, List<Resource> eligibleCandidates);
}