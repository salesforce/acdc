create table if not exists "dataset" (
    "name" VARCHAR(256) NOT NULL PRIMARY KEY,
    "created_at" TIMESTAMP NOT NULL,
    "updated_at" TIMESTAMP NOT NULL
);

create table if not exists "dataset_instance" (
    "name" VARCHAR(256) NOT NULL PRIMARY KEY,
    "location" VARCHAR(2048) NOT NULL,
    "dataset" VARCHAR(256),
    "created_at" TIMESTAMP NOT NULL
);

create table if not exists "dataset_lineage" (
    "from_dataset" VARCHAR(256) NOT NULL,
    "to_dataset" VARCHAR(256) NOT NULL
);

alter table "dataset_lineage" add constraint "pk_dataset_lineage" primary key("from_dataset","to_dataset");
