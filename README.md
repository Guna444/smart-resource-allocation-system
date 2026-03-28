# Smart Resource Allocation System (LLD)

Menu-driven console application demonstrating low-latency, priority-based task allocation to resources using optimized data structures and pluggable allocation strategies.

## Tech
- Java 17
- Maven
- Console UI (no live site)

## Features
- Priority-driven task queue (priority + deadline + createdAt ordering)
- Eligibility: resource skills + capacity
- Strategies:
  - Lowest Load
  - Priority Then Deadline
  - Skill Match Balanced (score-based)
- Real-time state tracking (task/resource status updates)
- Metrics: backlog, utilization, average wait time, assignment counts
- Fault-tolerant input validation and safe menu operations

## Run
Open as Maven project in IntelliJ, then run:

- `com.sras.App`

Or terminal:
```bash
mvn -q -DskipTests package
# Run from IDE recommended (simple)
```

## Notes
This project is designed for LLD / resume demonstration. It keeps data in-memory for simplicity, but has clear extension points to add JSON persistence.