package com.sras.repo;

import com.sras.domain.Resource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ResourceRepository {
    private final Map<String, Resource> byId = new ConcurrentHashMap<>();

    public void add(Resource resource) {
        byId.put(resource.id(), resource);
    }

    public Optional<Resource> get(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public List<Resource> listAll() {
        ArrayList<Resource> list = new ArrayList<>(byId.values());
        list.sort(Comparator.comparing(Resource::createdAt));
        return list;
    }

    public int totalCount() { return byId.size(); }
    public Collection<Resource> all() { return byId.values(); }
}