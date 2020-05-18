# Pantheon 2.x

[![Build Status](https://travis-ci.org/redhataccess/pantheon.png)](https://travis-ci.org/redhataccess/pantheon)
[![Code Coverage](https://codecov.io/gh/redhataccess/pantheon/branch/master/graph/badge.svg)](https://codecov.io/github/redhataccess/pantheon?branch=master)

Pantheon 2 is a modular documentation management and publication system based on asciidoc
and built on top of Apache sling.

### Prerequsistes
Java

Clone and build _https://github.com/apache/sling-org-apache-sling-karaf-configs.git_. This is required because sling-karaf-configs is not available on any of the maven repositories.
### Environment Variables
**Project Root Directory**

The build script provided in _scripts_ directory makes use of _PANTHEON_CODEBASE_ environmet variable. Set this variable in your .bashrc or .bash_profile script. _PANTHEON_CODBASE_ should point to the project's root directory.

**Sling and MongoDB**
The scripts folder contains _pantheon_karaf.exports_ file. It contains the values required for patheon karaf distribution. If you are running MongoDB on a different port then
- Make a copy of _pantheon_karaf.exports_ file 
- Place it in _.pantheon_ directory under your home directory
- Update the _MONGO_DB_REPLICA_ variable

### Build the application
_(All commands from here on will be assumed to be ran from the project's root directory)_

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
