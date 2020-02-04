# Pantheon 2.x

[![Build Status](https://travis-ci.org/redhataccess/pantheon.png)](https://travis-ci.org/redhataccess/pantheon)
[![Code Coverage](https://codecov.io/gh/redhataccess/pantheon/branch/master/graph/badge.svg)](https://codecov.io/github/redhataccess/pantheon?branch=master)

Pantheon 2 is a modular documentation management and publication system based on asciidoc
and built on top of Apache sling.

### Prerequsistes
Podman
Buildah
Java

### Build the application
_(All commands from here on will be assumed to be ran from the project's root directory)_

```sh
./mvnw clean install
```

### Unit Tests

```sh
./mvnw test
```

### Run the application

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

Build the pantheon docker image

```sh
buildah bud --layers -f container/Dockerfile -t pantheon-app .
```

Run the sling container pod in the pod.

```sh
podman run --pod pantheon -d -e SLING_OPTS='-Dsling.run.modes=oak_mongo -Doak.mongo.uri=mongodb://localhost:27017' --name pantheon-app pantheon-app
```

The Sling launchpad can be accessed at `http://localhost:8080` and you can log in to
it using the `admin/admin` username password combo.

### Live deploy of code

This is useful when developing the application.
To deploy the code live to a running application, all you have to do is

```sh
./mvnw clean package sling:install -pl pantheon-bundle
```

This will install the code in this project on the running Sling instance, where it can
be previewed and modified.

### Using the application

Head to http://localhost:8080/pantheon for the application's entry point.

For sling's management UI, you can head to http://localhost:8080/starter/index.html

You can stop and start the pod as necessary with podman's pod command:

```sh
podman pod stop pantheon
podman pod start pantheon
```

### Other use cases...

Run the container without Mongo, but this will result in the data being destroyed with the container.
```sh
podman run --rm -p 8080:8080  YOURTAG
```

Open a terminal inside the container and debug

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

NOTE: It will likely be necessary to increase your inotify limit to ensure that yarn is able to detect changes in every project file.
If you are running into issues with yarn not automatically detecting saved changes, run the following command (its effects are permenent):
```sh
echo fs.inotify.max_user_watches=65535 | sudo tee -a /etc/sysctl.conf && sudo sysctl -p
```

```sh
chromium-browser --disable-web-security --user-data-dir=/home/user/anywhere/chromeDev/ &
```

This works because there is code in app.tsx that preempts all fetch() calls and checks if the app is being served from localhost. If so, it modifies the request to point to localhost:8080 specifically, rather than localhost:9000 which is where webpack-dev-server serves the frontend code from.

It might be possible to improve this technique. Suggestions are welcome.

### End user documentation

The source for the end user documentation is stored in `/pantheon-bundle/src/main/resources/SLING-INF/content/docs/`.
