package com.sras.strategy;

import com.sras.domain.Resource;
import com.sras.domain.Skill;
import com.sras.domain.Task;

import java.util.List;
import java.util.Optional;

/**
 * Score-based: prefer high skill match, then lower utilization.
 * Demonstrates "dynamic allocation strategies to balance workload" for resume.
 */
public final class SkillMatchBalancedStrategy implements AllocationStrategy {
    @Override
    public String name() { return "SkillMatchBalancedStrategy"; }

    @Override
    public Optional<Resource> select(Task task, List<Resource> eligibleCandidates) {
        Resource best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Resource r : eligibleCandidates) {
            int matched = 0;
            for (Skill s : task.requiredSkills()) {
                if (r.skills().contains(s)) matched++;
            }
            // If task has no required skills, treat match as 1 (neutral)
            double matchRatio = task.requiredSkills().isEmpty()
                    ? 1.0
                    : (double) matched / (double) task.requiredSkills().size();

            // Penalize high utilization to prevent bottlenecks
            double score = (matchRatio * 100.0) - (r.utilization() * 50.0) - (r.loadPoints() / 1000.0);

            if (score > bestScore) {
                bestScore = score;
                best = r;
            }
        }
        return Optional.ofNullable(best);
    }
}