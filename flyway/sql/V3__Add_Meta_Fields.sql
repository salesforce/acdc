drop table bak_1__dataset;
drop table bak_1__dataset_instance;
drop table bak_1__dataset_lineage;
drop table dataset;
drop table dataset_instance;
drop table dataset_lineage;

create table "dataset" (
    "name" VARCHAR(256) NOT NULL PRIMARY KEY,
    "created_at" TIMESTAMP NOT NULL,
    "updated_at" TIMESTAMP NOT NULL,
    "meta" TEXT
);

create table "dataset_instance" (
    "dataset" VARCHAR(256) NOT NULL,
    "name" VARCHAR(256) NOT NULL,
    "created_at" TIMESTAMP NOT NULL,
    "updated_at" TIMESTAMP NOT NULL,
    "location" VARCHAR(2048) NOT NULL,
    "is_active" BOOLEAN NOT NULL,
    "meta" TEXT
);

alter table "dataset_instance"
    add constraint "pk_dataset_instance" primary key("dataset","name");

create table "dataset_lineage" (
    "from_dataset" VARCHAR(256) NOT NULL,
    "to_dataset" VARCHAR(256) NOT NULL
);

alter table "dataset_lineage" add constraint "pk_dataset_lineage" primary key(
    "from_dataset",
    "to_dataset"
);

alter table "dataset_instance"
    add constraint "fk_dataset_dataset"
    foreign key("dataset") references "dataset"("name")
    on update CASCADE
    on delete RESTRICT;

alter table "dataset_lineage"
    add constraint "fk_dataset_lineage_from_dataset"
    foreign key("from_dataset") references "dataset"("name")
    on update CASCADE
    on delete RESTRICT;

alter table "dataset_lineage"
    add constraint "fk_dataset_lineage_to_dataset"
    foreign key("from_dataset")
    references "dataset"("name")
    on update CASCADE
    on delete CASCADE;
