# Pantheon 2.x

[![Build Status](https://travis-ci.org/redhataccess/pantheon.png)](https://travis-ci.org/redhataccess/pantheon)
[![Code Coverage](https://codecov.io/gh/redhataccess/pantheon/branch/master/graph/badge.svg)](https://codecov.io/github/redhataccess/pantheon?branch=master)

Pantheon 2 is a modular documentation management and publication system based on asciidoc
and built on top of Apache sling.

### Prerequsistes
Podman
Maven
Java

### Unit Tests

```sh
mvn test
```

### How to run this App

To run the application, run the Apache sling podman container.

```sh
podman run -p 8080:8080 --name slingapp apache/sling 
```

This will create a container called `slingapp`.

The Sling launchpad can be accessed at `http://localhost:8080` and you can log in to
it using the `admin/admin` username password combo.

To deploy the code used in this demo, all you have to do is run (from the project root)

```sh
 mvn clean package sling:install
```

This will install the code in this project on the running Sling instance, where it can
be previewed and modified.

### What To do once running

Head to http://localhost:8080/pantheon for the application's entry point.

For sling's management UI, you can head to http://localhost:8080/starter/index.html

### How to run against a Mongo datastore

Pull the mongo db image:
```sh
podman pull mongo
```

Run a mongo db container (were are calling it `slingmongo` here)
```sh
podman run --network=host --name slingmongo -d mongo
```

Run a transient Sling container which stores everything in the linked mongo db:
```sh
podman run --network=host --rm -d -p 8080:8080 -e SLING_OPTS='-Dsling.run.modes=oak_mongo -Doak.mongo.uri=mongodb://localhost:27017' apache/sling
```

**Notice the --network flag** this is needed for both containers that talk to each other. You can see the --network flag documentation in this [Link.](https://github.com/containers/libpod/blob/master/docs/podman-create.1.md)

### Building the application in the a container.

We also provide an podman image under the container folder that does a two stage build that will generate a container with the application.

Prerequsistes for this is only **podman** and **buildah**.

1. Build the container

```
buildah -t YOURTAG bud container
```

2. To run the container


Run the container using the previously started mongo instance:
```sh
podman run --network=host --rm -d -p 8080:8080 -e SLING_OPTS='-Dsling.run.modes=oak_mongo -Doak.mongo.uri=mongodb://localhost:27017 -Dsling.fileinstall.dir=/install' YOURTAG
```

You can also run the container without Mongo, but this will result the data to be destroyed when the container gets destroyed.
```sh
podman run --rm -p 8080:8080  YOURTAG
```

3. To get inside the container and debug

get the container process
```
podman ps
```

```
podman exec -it PROCESS bash
```

### more to come...
