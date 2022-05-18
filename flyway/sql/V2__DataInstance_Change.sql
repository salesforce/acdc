alter table "dataset" rename to "bak_1__dataset";
alter table "dataset_instance" rename to "bak_1__dataset_instance";
alter table "dataset_lineage" rename to "bak_1__dataset_lineage";
alter table "bak_1__dataset_lineage"
    rename constraint "pk_dataset_lineage" to "bak_1__pk_dataset_lineage";

create table "dataset" (
    "name" VARCHAR(256) NOT NULL PRIMARY KEY,
    "created_at" TIMESTAMP NOT NULL,
    "updated_at" TIMESTAMP NOT NULL
);

create table if not exists "dataset_instance" (
    "dataset" VARCHAR(256) NOT NULL,
    "name" VARCHAR(256) NOT NULL,
    "location" VARCHAR(2048) NOT NULL,
    "is_active" BOOLEAN NOT NULL,
    "created_at" TIMESTAMP NOT NULL,
    "updated_at" TIMESTAMP NOT NULL
);

alter table "dataset_instance" add constraint "pk_dataset_instance" primary key("dataset","name");

create table "dataset_lineage" (
    "from_dataset" VARCHAR(256) NOT NULL,
    "to_dataset" VARCHAR(256) NOT NULL
);

alter table "dataset_lineage" add constraint "pk_dataset_lineage" primary key(
    "from_dataset",
    "to_dataset"
);
