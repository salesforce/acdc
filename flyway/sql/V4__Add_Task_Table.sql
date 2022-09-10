create table if not exists "expiration_task" (
    "task_runner_name" VARCHAR(256) NOT NULL,
    "timestamp" TIMESTAMP NOT NULL
);

alter table "expiration_task"
    add constraint "pk_expiration_task" primary key("task_runner_name", "timestamp");
