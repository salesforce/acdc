# ACDC

> A Coarse-grained Data Catalog

## Features

A play framework based CRUD service that helps keeping track of dataset's
current and historical location, and their lineage.

Checkout `acdc-ws/app/routers/ApiRouter.scala` for supported routes.

## Setup

Checkout [play
documentation](https://www.playframework.com/documentation/2.8.x/Production) on
how to deploy the service.

To setup the database run `com.salesforce.mce.acdc.tool.ProvisionDatabase`.

```shell 
sbt 'project core; run ProvisionDatabase'
```

If config ```acdc.auth.enabled = true``` is enabled, authorization api-keys are SHA-256 hashed.  The REST enb-points should include a http header key ```x-api-key``` with appropriate value.  

Set an environment variable for the authorized key value pairs.  Example:

```shell
export MCE_ENV_X_API_KEYS="{f6e42a3c0dffee079face0da061ee2c9a871eebe098ac481248e34cfe023955b = [admin, user], 70ad8c1543728a3e61bbec26d1df5bd742d1c2f464f2ac54a2fec5e709eba890 = [admin]}"
```

In the above example, ```f6e42a3c0dffee079face0da061ee2c9a871eebe098ac481248e34cfe023955b``` is a
hashed api-key, and ```admin``` and ```user``` are access roles.  More than one api-key can be included in the environment variable ```MCE_ENV_X_API_KEYS``` for keys rotation or 
keys with different access roles.

## Run web service 

```shell 
sbt ws/run
```
