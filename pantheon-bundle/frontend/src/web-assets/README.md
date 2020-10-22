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

This process builds the Sass specific to Pantheon's previews and brings in rhdocs.css from @cp-elements/cp-documentation and puts it in the right places for the preview to pick up.

## Dev Watch Process
To work on the Sass/CSS, use:
```shell
yarn run dev
```

This will start a local browser-sync server that:
* Automatically loads the `a-doc-styleguide.html`, which is generated from Pantheon's templates
* Will watch and compile the dev and prod versions of the Scss, and watch for changes in HTML (from Haml updates)
* Will watch for any changes in the adocs in the dev-preview folder
* Reload the page if anything changes
* It will run on a port from your localhost that will show up in command line

## Build for CSS Development
This is the same as `yarn run dev` but without the watch process.

```shell
yarn run build:dev
```
