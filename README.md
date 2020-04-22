# Pantheon 2.x

[![Build Status](https://travis-ci.org/redhataccess/pantheon.png)](https://travis-ci.org/redhataccess/pantheon)
[![Code Coverage](https://codecov.io/gh/redhataccess/pantheon/branch/master/graph/badge.svg)](https://codecov.io/github/redhataccess/pantheon?branch=master)

Pantheon 2 is a modular documentation management and publication system based on Asciidoc
and built on top of Apache Sling.

* [Contributing to Pantheon](#contributing-to-pantheon)
* [Installing Pantheon](#installing-pantheon)
 * [Prerequisites](#prerequisites)
 * [Building the application](#building-the-application)
 * [Unit tests](#unit-tests)
 * [Running the application](#running-the-application)
 * [Live deploy of code](#live-deploy-of-code)
 * [Using the application](#using-the-application)
 * [Other use cases](#other-use-cases)
 * [Developing the frontend code](#developing-the-frontend-code)
* [End user documentation](#end-user-documentation)

## Contributing to Pantheon

### Prerequisites

* You must have an account on GitHub.  
  [Signing up for a new GitHub account](https://help.github.com/en/github/getting-started-with-github/signing-up-for-a-new-github-account)
* You must have registered SSH keys in your GitHub account.  
[Adding a new SSH key to your GitHub account](https://help.github.com/en/github/authenticating-to-github/adding-a-new-ssh-key-to-your-github-account)
* You must be a member of the `pantheon-doc-authors` team in the `redhataccess` group in GitHub.
* You must be logged in to your account on GitHub.

### Forking the repository

Fork the repository so that you can create and work with branches independently of the `redhataccess/pantheon` repository.

1. In a web browser, navigate to https://github.com/redhataccess/pantheon
1. Click **Fork**.
1. Click your user space in GitHub.

### Cloning the repository

After you have forked the repository, you must clone it to your local machine and add the original `redhataccess/pantheon` repository as an upstream remote.

1. From a terminal, clone the repository:  
       $ git clone git@github.com:<user-space>/pantheon.git
1. Set up `redhataccess/pantheon` as the upstream:  
       $ cd pantheon
       $ git remote add upstream git@github.com:redhataccess/pantheon.git

### Creating a working branch

Whenever you work on a new issue, you must create a new working branch based on the latest version of the upstream master branch.

1. Ensure you are on master  
       $ git checkout master
1. Ensure your fork is up to date  
       $ git pull upstream master
1. Create a working branch based on the issue in JIRA:  
       $ git checkout -b FCCEUD<ID#>

### Creating a pull request and completing review

When your work is ready to be reviewed and merged, create a pull request.

1. Push your working branch to your fork:  
       $ git push -u origin <branch_name>
1. From the repository page in GitHub, click **New pull request**.
1. Select your working branch from the compare list.
1. Add `WIP` to the title of the pull request.
1. Add the **awaiting tech review** label to the pull request.
1. In the pull request comment field, enter `@redhataccess/pantheon-developers Please review for technical completeness and accuracy`.
1. Click **Create new pull request**.

The developers review the pull request for technical accuracy and team members review the pull request for clarity and consistency and compliance with departmental standards.

### The review process

Both the technical review and peer review processes take place in pull requests in GitHub.

After creating and labeling a pull request as outlined above, the developers review the pull request and add comments regarding technical accuracy. Writers receive a notification that comments have been added via email, and when all comments have been addressed, the developers change the label from **awaiting tech review** to **tech review passed**.

When technical review is complete, writers click the **Request review** gear icon and select the name of a team member to request peer review. Writers receive a notification that comments have been added via email, and when all comments have been addressed, the reviewer clicks **Review changes > Approve** from the **Files changed** tab of the pull request to approve the changes and the pull request.

### Merging a pull request

When you have addressed all technical review and peer review comments, notify the developers to accept the pull request.

1. Remove `WIP` from the title of the pull request.
1. Click **Request Review** and enter `@redhataccess/pantheon-developers`.

The developers check that the **Tech review passed** label has been added to the pull request and peer pull request approval provided, then accept it.

## Installing Pantheon

### Prerequisites
* Podman
* Buildah
* Java

### Building the application
_(All commands from here on will be assumed to be run from the project's root directory)_

```sh
./mvnw clean install
```

### Unit tests

```sh
./mvnw test
```

### Running the application

The best way to run Pantheon is to install [podman](https://podman.io).

. First, create a pod:

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
