set -o allexport

if [ -z "$PANTHEON_CODEBASE" ]
then
      echo "\$PANTHEON_CODEBASE is empty"
      exit 1
fi

CONF_FILE=$PANTHEON_CODEBASE/scripts/pantheon_karaf.exports

if [[ -f ~/.pantheon/pantheon_karaf.exports ]]; then
    CONF_FILE=~/.pantheon/pantheon_karaf.exports
fi


source $CONF_FILE
set +o allexport

cd $PANTHEON_CODEBASE

set -e
# build the distribution
./mvnw clean install -DskipTests -U -pl pantheon-bundle,pantheon-karaf-feature,pantheon-karaf-dist 
# extract the distribution
tar -xvf pantheon-karaf-dist/target/pantheon-karaf-dist-1.0-SNAPSHOT.tar.gz -C pantheon-karaf-dist/target/ 
# run the distribution
pantheon-karaf-dist/target/pantheon-karaf-dist-1.0-SNAPSHOT/bin/karaf
set +e

