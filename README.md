# Smart Resource Allocation System (LLD) — Mar 2026

A **menu-driven Java 17 console application** that simulates a real-world **resource allocation engine**.  
It allocates incoming tasks to eligible resources using **priority + deadline ordering**, **optimized data structures**, and **pluggable allocation strategies** (Strategy Pattern) to balance utilization and reduce bottlenecks.

> Purpose: LLD/resume-ready project focusing on clean architecture, extensibility, and correctness (not a live web app).

---

## Highlights (What this demonstrates)
- **Priority-driven allocation** with deterministic ordering (Priority → Deadline → CreatedAt)
- **Low-latency selection** using Java Collections (`PriorityQueue`-style ordering, `HashMap` repositories, `EnumSet` skills)
- **Dynamic strategy switching** (Strategy Pattern) to optimize assignments under varying workloads
- **Real-time state tracking** for tasks/resources (status + load updates)
- **Fault-tolerant CLI** with validation and exception-safe flows
- **Metrics & observability** (backlog, utilization, wait time, assignment count)

---

## Tech Stack
- **Language:** Java 17
- **Build:** Maven
- **Core:** OOP, Java Collections Framework, Exception Handling
- **Design Patterns:** Strategy Pattern
- **Interface:** Console / CLI (menu-driven)

---

## System Overview

### Core Concepts
- **Task**
  - `priority` (LOW/MEDIUM/HIGH/CRITICAL)
  - `deadline`
  - `estimatedMinutes` (converted into load points)
  - `requiredSkills`
  - `status`: PENDING → ALLOCATED → COMPLETED

- **Resource**
  - `skills`
  - `capacityPoints`
  - `loadPoints` (current utilization)
  - `status`: ACTIVE / INACTIVE

- **Assignment**
  - mapping of `taskId -> resourceId` with timestamp and applied load

### Allocation Pipeline (High Level)
1. Pick next task from backlog ordered by **Priority → Deadline → CreatedAt**
2. Filter eligible resources:
   - ACTIVE
   - contains all required skills
   - has remaining capacity
3. Use selected **AllocationStrategy** to choose best resource
4. Apply assignment:
   - update resource load
   - update task status/resourceId
   - record assignment + metrics

---

## Allocation Strategies (Strategy Pattern)
You can switch strategies at runtime via the menu:

- **LowestLoadStrategy**  
  Picks the eligible resource with the least current load/utilization.

- **PriorityThenDeadlineStrategy**  
  Task ordering already prioritizes critical items; strategy chooses the least utilized eligible resource.

- **SkillMatchBalancedStrategy (recommended)**  
  Score-based selection:
  - rewards high skill match
  - penalizes high utilization to reduce bottlenecks

---

## Data Structures Used
- **Backlog ordering:** `PriorityQueue`-style ordering (priority weight + deadline + createdAt)
- **Fast lookups:** `HashMap` repositories for tasks/resources by id
- **Skills representation:** `EnumSet<Skill>` / `Set<Skill>` for efficient skill matching

---

## Project Structure
```text
src/main/java/com/sras/
  cli/        -> Menu-driven console UI + safe input handling
  domain/     -> Task, Resource, Assignment, enums (Priority, Skill, statuses)
  repo/       -> In-memory repositories
  strategy/   -> AllocationStrategy + implementations
  service/    -> AllocationEngine, MetricsService, SeedData, Validation
  ds/         -> Small supporting DS utilities
```

---

## How to Run

### Option 1: IntelliJ (recommended)
1. Open the project folder
2. Ensure Maven is detected (`pom.xml`)
3. Run:
   - `com.sras.App`

### Option 2: Terminal (build)
```bash
mvn -q -DskipTests clean package
```

> Note: This is a console application; running from the IDE is simplest.

---

## Example Menu Actions
- Create tasks/resources
- Allocate **one** task or allocate the **entire backlog**
- Mark tasks completed
- Release resource load (simulate time passing)
- View metrics and current system state
- Switch allocation strategy

---

## Metrics (Sample)
The system reports:
- total tasks, pending/allocated/completed
- total resources
- average utilization
- average wait time (approx.)
- assignment count

---

## Extensibility Ideas (Next Enhancements)
This project is intentionally kept **in-memory** for simplicity and interview focus. Easy extensions include:
- JSON persistence for tasks/resources (e.g., Gson)
- Unit tests (JUnit) for strategies and engine behavior
- Preemption / SLA-aware scheduling
- Concurrency (thread-safe queue) + benchmarking

---

## Resume Bullets (ready to paste)
- Built a **priority + deadline-driven task allocation engine** using Java Collections to enable low-latency selection and efficient utilization under varying workloads.
- Implemented **pluggable allocation strategies (Strategy pattern)** with real-time state tracking to balance load and prevent resource bottlenecks during peak demand.
- Delivered a **fault-tolerant, menu-driven system** with modular components and clear separation of concerns for maintainable, scalable architecture.

---

## License
MIT (or add your preferred license)
