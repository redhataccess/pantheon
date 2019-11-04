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

The best way to run Pantheon is to install [podman](https://podman.io).

First, create a pod:

```sh
podman pod create --name pantheon -p 8080 -p 5005
```

This will create a `pantheon` pod with ports 8080 (for web access) and 5005 (for
remote Java debugging) open.

Run a mongo database container in the pod.

```sh
podman run --pod pantheon --name slingmongo -d mongo
```

Run the sling container pod in the pod.

```sh
podman run --pod pantheon -d -e SLING_OPTS='-Dsling.run.modes=oak_mongo -Doak.mongo.uri=mongodb://localhost:27017' --name sling apache/sling
```

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

You can stop and start the pod as necessary with podman's pod command:

```sh
podman pod stop pantheon
podman pod start pantheon
```

### Running straight from the Kubernetes pod definition

There kubernetes definition at [containers/pantheon.yaml](containers/pantheon.yaml)
will also start the full pod described above with a single command:

```sh
podman play kube container/pantheon.yaml
```

### Building the application in the a container

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

### Developing the frontend code

If making modifications that are entirely contained within the frontend, it is not necessary to use maven to rebuild and redeploy the package on every change.

These instructions provide an imperfect-but-workable shortcut that can accelerate development.

```sh
cd pantheon/frontend
yarn start
```

```sh
chromium-browser --disable-web-security --user-data-dir=/home/user/anywhere/chromeDev/ &
```

This works because there is code in app.tsx that preempts all fetch() calls and checks if the app is being served from localhost. If so, it modifies the request to point to localhost:8080 specifically, rather than localhost:9000 which is where webpack-dev-server serves the frontend code from.

It might be possible to improve this technique. Suggestions are welcome.
