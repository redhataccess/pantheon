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

Builds the dev and prod versions of the CSS. Dev version is inside this folder `./rhdocs.css`, the prod version is in `GITROOT/pantheon-bundle/frontend/src/web-assets/rhdocs.css`.

To work on the Sass/CSS, use:
```shell
yarn run watch
```

This will start a local browser-sync server that:
* Automatically loads the `a-doc-styleguide.html`, which is generated from Pantheon's templates
* Will watch and compile the dev and prod versions of the Scss, and watch for changes in HTML
* Reload the page anything changes
* It will run on a port that will show up in command line
