#
# Recreates the Sling container ONLY. It will recompile the bundle and the slingstart
# module, and will drop the existing container and recreate it.
# Useful when the recreateSlingPod.sh script has been run beforehand.
#
# This script assumes that a database is already running. See the SLING_DB variable below
# for details.
#
echo "Building Pantheon bundle and slingstart..."
./mvnw clean package -DskipTests

echo "Building Pantheon image..."
buildah bud --layers -f container/Dockerfile -t pantheon-app .

echo "Stopping existing Pantheon container..."
podman stop pantheon-app

echo "Removing previosuly stopped Pantheon container..."
podman rm -f pantheon-app

echo "Running new Pantheon container..."
# Use this db property to connect to a running replica set pod (see recreateReplicaSetpod.sh)
SLING_DB='mongodb://localhost:30001,localhost:30002,localhost:30003'
# Use this db property to connect to a simple DB
#SLING_DB='mongodb://localhost:27017'
podman run --pod pantheon -d -e SLING_OPTS="-Dsling.run.modes=oak_mongo -Doak.mongo.uri=$SLING_DB" --name pantheon-app pantheon-app
