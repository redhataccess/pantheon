# Pantheon 2.x

[![Build Status](https://travis-ci.org/redhataccess/pantheon.png)](https://travis-ci.org/redhataccess/pantheon)
[![Code Coverage](https://codecov.io/gh/redhataccess/pantheon/branch/master/graph/badge.svg)](https://codecov.io/github/redhataccess/pantheon?branch=master)

Pantheon 2 is a modular documentation management and publication system based on asciidoc
and built on top of Apache sling.
* [Contributing to Pantheon](#contributing-to-pantheon)
* [Installing Pantheon](#installing-pantheon)
 * [Prerequisites](#prerequisites)
 * [Environment Variables](#environment-variables)
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
* You must be a member of the `pantheon-developers` or `pantheon-doc-authors` team in the `redhataccess` group in GitHub.
* You must be logged in to your account on GitHub.

### Forking the repository

Fork the repository so that you can create and work with branches independently of the `redhataccess/pantheon` repository.

1. In a web browser, navigate to https://github.com/redhataccess/pantheon
1. Click **Fork**.
1. Click your user space in GitHub.

### Cloning the repository

After you have forked the repository, you must clone it to your local machine and add the original `redhataccess/pantheon` repository as an upstream remote.

1. From a terminal, clone the repository:
       ```
       $ git clone git@github.com:<user-space>/pantheon.git
       ```
1. Set up `redhataccess/pantheon` as the upstream:
       ```
       $ cd pantheon
       $ git remote add upstream git@github.com:redhataccess/pantheon.git
       ```

### Creating a working branch

Whenever you work on a new issue, you must create a new working branch based on the latest version of the upstream master branch.

1. Ensure you are on master
       ```
       $ git checkout master
       ```
1. Ensure your fork is up to date
       ```
       $ git pull upstream master
       ```
1. Create a working branch based on the issue in JIRA:
       ```
       $ git checkout -b FCCEUD<ID#>
       ```       

### Creating a pull request and completing review

When your work is ready to be reviewed and merged, create a pull request.

1. Push your working branch to your fork:
       ```
       $ git push -u origin <branch_name>
       ```
1. From the repository page in GitHub, click **New pull request**.
1. Select your working branch from the compare list.
1. Add `WIP:` to the title of the pull request. This automatically converts the pull request to a draft pull request.
1. Click **Create new pull request**.
1. Add the **awaiting tech review** label to the pull request.
1. In the pull request comment field, enter `@redhataccess/eud-tech-review Please review for technical completeness and accuracy`.


### The review process

Both the technical review and peer review processes take place in pull requests in GitHub.

#### Documentation review
After creating and labeling a pull request as outlined above, the developers review the pull request and add comments regarding technical accuracy. Writers receive a notification that comments have been added via email, and when all comments have been addressed, the developers change the label from **awaiting tech review** to **tech review passed**.

When technical review is complete, writers click the **Reviewers** gear icon and select the name of a team member to request peer review. The peer writer reviews the pull request for clarity, consistency, and compliance with necessary standards.
Writers receive a notification that comments have been added via email, and when all comments have been addressed, the reviewer clicks **Review changes > Approve** from the **Files changed** tab of the pull request to approve the changes and the pull request.

#### Code review

For code pull requests, one or more developers review the pull request. A contributor submits a PR and uses Github's **Reviewers** gear icon to tag `@redhataccess/pantheon-developers`. A developer comments on the code, and discusses it with the submitter, before ultimately deciding to accept or reject the PR.

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

### Environment Variables
**Project Root Directory**

The build script provided in _scripts_ directory makes use of _PANTHEON_CODEBASE_ environment variable. Set this variable in your .bashrc or .bash_profile script. _PANTHEON_CODEBASE_ should point to the project's root directory.

**Sling and MongoDB**
The scripts folder contains _pantheon_karaf.exports_ file. It contains the values required for the pantheon karaf distribution. If you are running MongoDB on a different port then
- Make a copy of _pantheon_karaf.exports_ file
- Place it in _.pantheon_ directory under your home directory
- In `~/.pantheon/pantheon_karaf.exports`, update the _MONGO_DB_URI_ variable

### Building the application
_(All commands from here on will be assumed to be run from the project's root directory)_

```sh
sh scripts/deploy_local.sh
```
The _deploy_local_ script will:
- Run maven build that creates the pantheon karaf distribution
- Extract the archive to _$PANTHEON_CODEBASE/pantheon-karaf-dist/target_. The distribution is being extracted to target, currently, because a fresh distribution is needed for changes in the pantheon-bundle codebase. In the future, that may change and accordingly script will also change.
- Start Karaf, and drop you into the karaf shell
### Using the application

Head to http://localhost:8181/pantheon for the application's entry point.

For sling's management UI, you can head to http://localhost:8181/starter/index.html

**Note:** If you plan to use git import UI locally, please follow the instructions in README under tools/git2pantheon. Also you will need to set the credentials of the user that would be used by git2pantheon to push the repository. It can be done by using environment variables (for both podman based and non-podman based).

### Other use cases...


**Debug using Karaf shell**
- To view logs: log:display
- To view exceptions: log:exception-display
- To list all bundles and view their status: bundle:list
- Find out why a bundle is in waiting state: diag _[bundle-id]_

### Run the application using Podman
First, install [podman](https://podman.io).

Then, create a pod:

```sh
podman pod create --name pantheon-karaf -p 8181 -p 5005
```

This will create a `pantheon-karaf` pod with ports 8181 (for web access) and 5005 (for
remote Java debugging) open.

Run a mongo database container in the pod.

```sh
podman run --pod pantheon-karaf --name slingmongo -d mongo
```

Build the pantheon image

```sh
buildah bud --layers -f container/Dockerfile -t pantheon-karaf-app .
```

Run the sling container in the pod.

```sh
podman run --pod pantheon-karaf -d -e  MONGO_DB_REPLICA='mongodb://localhost:27017'   -t --name pantheon-karaf-app   pantheon-karaf-app

```

The Sling launchpad can be accessed at `http://localhost:8181` and you can log in to
it using the `admin/admin` username password combo.

### Live deploy of code

This is useful when developing the application.
To deploy the code live to a running application, all you have to do is

```sh
./mvnw clean install sling:install -pl pantheon-bundle
```

This will install the code in this project on the running Sling instance, where it can
be previewed and modified.
### Developing the frontend code

If making modifications that are entirely contained within the frontend, follow the instructions to build the application in this README, then

```sh
cd pantheon-bundle/frontend
# Install/update node deps
yarn
# Build the app
yarn build
# Run React dev server/process
yarn start
```

See the pantheon-bundle/frontend/README.md for more information on the development server

NOTE: It will likely be necessary to increase your inotify limit to ensure that yarn is able to detect changes in every project file.
If you are running into issues with yarn not automatically detecting saved changes, run the following command (its effects are permenent):
```sh
echo fs.inotify.max_user_watches=65535 | sudo tee -a /etc/sysctl.conf && sudo sysctl -p
```

It might be possible to improve this technique. Suggestions are welcome.

### End user documentation

The source for the end user documentation is stored in `/pantheon-bundle/src/main/resources/SLING-INF/content/docs/`.
