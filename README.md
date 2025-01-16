# ACDC

> A Coarse-grained Data Catalog

[![CircleCI](https://circleci.com/gh/salesforce/acdc.svg?style=svg)](https://circleci.com/gh/salesforce/acdc)

## Features

A play framework based CRUD service that helps keeping track of dataset's
current and historical location, and their lineage.

Checkout `acdc-ws/app/routers/ApiRouter.scala` for supported routes.

## Setup

Set postgres password by

```shell
export POSTGRES_PASSWORD=<my db password>
```

If config ```acdc.auth.enabled = true``` is enabled, authorization api-keys are SHA-256 hashed.  The REST endpoints should include a http header key ```x-api-key``` with appropriate value.

Set an environment variable for the authorized x-api-key hashed string.  Example:

```shell
export MCE_ENV_X_API_ADMIN1=f6e42a3c0dffee079face0da061ee2c9a871eebe098ac481248e34cfe023955b
export MCE_ENV_X_API_ADMIN2=70ad8c1543728a3e61bbec26d1df5bd742d1c2f464f2ac54a2fec5e709eba890
```

For keys rotation, 2 keys can cycle through still older copies of hashed strings.

## Start 


Run
```shell
docker compose up -d
docker compose exec ws sbt
project ws; run
```
You need to send a query to trigger compile.

For local testing, can query by http://localhost:9001/__status

To run flyway migration
```
docker compose run --rm flyway migrate 
```

To access database
```
docker compose exec db psql -h db -U postgres 
```
