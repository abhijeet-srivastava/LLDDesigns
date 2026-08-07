# JobScheduler

A Spring Boot REST API for scheduling and running cron-based jobs. Users submit a job (message +
priority + cron expression); a poller finds tasks due soon and hands them to a worker thread pool
for execution.

## Architecture

```
POST /scheduler/schedule
        |
        v
   JobService --------------------------------+
        |                                     |
        v                                     v
  Job table (H2)                        Task table (H2)
        ^                                     |
        |                    status=QUEUED, next_execution_time
        |                                     |
        |                                     v
        |                          TaskPollerService (@Scheduled)
        |                          polls QUEUED tasks due within
        |                          the next N minutes, de-dupes
        |                          against an in-memory dispatched
        |                          set, and puts them on...
        |                                     |
        |                                     v
        |                    PriorityBlockingQueue<TaskRef>
        |                    (priority, then next_execution_time)
        |                                     |
        |                                     v
        |                       Fixed-size worker thread pool
        |                       (TaskWorker) takes tasks, marks
        |                       IN_PROGRESS, "executes" (prints
        +----- new QUEUED task  the message), marks COMPLETED/
               for the next     FAILED, and if the job is still
               cron fire  <-----active, inserts the next task.
```

A `TaskExecutionRegistry` maps `job_id -> currently executing Thread`, so the interrupt endpoint
can call `Thread.interrupt()` on the worker running that job's task.

## Endpoints

### Create a job
`POST /scheduler/schedule`
```json
{
  "description": "daily report",
  "message": "Generating report...",
  "priority": 1,
  "cronstatement": "0 0 * * * *"
}
```
Response:
```json
{ "jobId": 1, "message": "Generating report...", "jobStatus": "QUEUED" }
```

### Get job details
`GET /scheduler/jobs/{job_id}` -> job fields plus its current task's id/status/next execution time.
404 if the job doesn't exist.

### Update a job
`PUT /scheduler/jobs/{job_id}` — partial update: `description`, `message`, `priority` (1-5),
`cronstatement`, `active`. Changing the cron recomputes the pending `QUEUED` task's next execution
time. 404 if unknown, 400 on invalid priority/cron.

### Delete a job
`DELETE /scheduler/jobs/{job_id}` — interrupts any running task, deletes all its tasks, then the
job. 404 if unknown.

### Interrupt a running task
`POST /scheduler/jobs/{job_id}/task/interrupt` -> `{ "jobId": 1, "interrupted": true|false }`.
404 if the job doesn't exist.

## Data model
- **Job**: `id`, `description`, `message`, `priority` (1-5), `cron`, `active`.
- **Task**: `id`, `job_id`, `status` (`QUEUED -> IN_PROGRESS -> COMPLETED|FAILED`), `next_execution_time`, `priority`.

Each cron fire creates a new `Task` row (rather than recycling one row), so a job's task history is
preserved.

## Running locally
```bash
mvn -pl JobScheduler -am spring-boot:run
```
H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:jobscheduler`, user `sa`, no password).

## Running with Docker
```bash
docker compose -f JobScheduler/docker-compose.yml up --build
```

## Configuration (`application.yml`)
- `jobscheduler.poller.fixed-delay-ms` (default 15000) — how often the poller runs.
- `jobscheduler.poller.lookahead-minutes` (default 1) — how far ahead the poller looks for due tasks.
- `jobscheduler.worker.pool-size` (default 4) — number of worker threads.

## Tests
```bash
mvn -pl JobScheduler -am test
```
Covers controller (`@WebMvcTest`), service, cron computation, poller de-dup, and worker
completion/interruption behavior, with positive and negative cases at each layer.

## Known limitations
- If a worker dies mid-`take()` (should not happen under normal `ExecutorService` operation), a
  dispatched task has no requeue/heartbeat mechanism and would sit unprocessed. Not handled, to
  keep the scaffold minimal.
- The poller dispatches tasks up to `lookahead-minutes` ahead of their `next_execution_time`;
  workers execute immediately on pickup rather than waiting for the exact scheduled second — this
  matches the "poll for tasks due in the next minute" design in the original spec, at minute-level
  rather than second-level precision.