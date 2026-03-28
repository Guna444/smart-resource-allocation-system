package com.sras.repo;

import com.sras.domain.Assignment;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AssignmentRepository {
    private final List<Assignment> assignments = new CopyOnWriteArrayList<>();

    public void add(Assignment a) { assignments.add(a); }

    public int count() { return assignments.size(); }

    public List<Assignment> listAll() { return List.copyOf(assignments); }
}