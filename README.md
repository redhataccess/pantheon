# Pantheon 2.x

[![Code Coverage](https://img.shields.io/codecov/c/github/pvorb/property-providers/develop.svg)](https://codecov.io/github/pvorb/property-providers?branch=develop)

### Prerequsistes
Docker
Maven
Java

### Unit Tests

```sh
mvn test
```

### How to run this App

The purpose of this project is to demonstrate the capabilities of the Sling framework
on content management.

To run this demo, you need to run the Apache Sling docker container. First, build the
container image which is based off the original apache sling image.

```sh
cd src/main/dockerfiles
docker build -t cptools/sling . 
```

then run the image container

```sh
docker run -p 8080:8080 --name slingapp cptools/sling 
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

You can look around the different parts of code which mostly try to render a sample type of 
JCR resource (modules) in different ways. The application loads a sample set of data under the 
JCR path `content/modules` with three modules used for testing.

A basic module management application can be found at `http://localhost:8080/modules.html`

You can also retrieve product definitions at `http://localhost:8080/products.tidy.2.json`.
A brief explanation on the url:

`http://localhost:8080/products` is the location of the resource (the content)

`.tidy` is a selector, which pretty prints the requested json

`.2` is the recursion level, it means fetch 2 levels from the content tree

`.json` is the format in which to render the requested resource


### How to run against a Mongo datastore

Pull the mongo db image:
```sh
docker pull mongo
```

Run a mongo db container (were are calling it `slingmongo` here)
```sh
docker run --name slingmongo -d mongo
```

Run a transient container which stores everything in the linked mongo db:
```sh
docker run --rm -d -p 8080:8080 --link slingmongo:mongo -e SLING_OPTS='-Dsling.run.modes=oak_mongo -Doak.mongo.uri=mongodb://mongo:27017' cptools/sling
```

### more to come...