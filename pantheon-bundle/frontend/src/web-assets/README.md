# Front-end build for Pantheon

## Installing tools for building CSS

Have Yarn on your machine, version 1.21 or higher.

Run:
```shell
yarn install
```

To build CSS for production, use:
```shell
yarn run build
```

CSS should always be built for production before comitting to the repo.

To work on the Sass/CSS, use:
```shell
yarn run watch
```
This will start a local browser-sync server that:
* Automatically loads the `a-doc-styleguide.html`, which is generated from Pantheon's templates
* Will watch and compile Scss, and watch for changes in HTML
* Reload the page anything changes
* It will run on a port that will show up in command line

This will compile CSS in dev mode which should not be committed to the codebase. Unfortunately source-maps don't seem to work with some of the post-processing we're doing, so it's currently disabled.

To build CSS for development without running the server, run:
```shell
yarn run build:dev
```
