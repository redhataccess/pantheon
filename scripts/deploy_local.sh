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
# build the karaf-features and karaf-config along with pantheon modules
./mvnw clean install -U -pl sling-org-apache-sling-karaf-features,sling-org-apache-sling-karaf-configs,pantheon-bundle,pantheon-karaf-feature,pantheon-karaf-dist
pantheon-karaf-dist/target/assembly/bin/karaf
set +e

