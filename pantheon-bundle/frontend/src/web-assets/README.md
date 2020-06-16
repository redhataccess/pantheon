# Front-end build for Pantheon

## Installing tools for building CSS

1. Have Yarn on your machine, version 1.21 or higher.
2. For either of the dev processes, [have AsciiDoctor CLI installed](https://asciidoctor.org/docs/install-toolchain/)

Run:
```shell
yarn install
```

## Build CSS
To build CSS use:
```shell
yarn run build
```

This builds the dev and prod versions of the CSS. Dev version is inside this folder `./rhdocs.css`, the prod version is in `GITROOT/pantheon-bundle/frontend/src/web-assets/rhdocs.css`.

## Dev Watch Process
To work on the Sass/CSS, use:
```shell
yarn run dev
```

This will start a local browser-sync server that:
* Automatically loads the `a-doc-styleguide.html`, which is generated from Pantheon's templates
* Will watch and compile the dev and prod versions of the Scss, and watch for changes in HTML
* Will watch for any changes in the adocs in the dev-assets folder
* Reload the page anything changes
* It will run on a port that will show up in command line

## Build for CSS Development
This is the same as `yarn run dev` but without the watch process.

```shell
yarn run build:dev
```
