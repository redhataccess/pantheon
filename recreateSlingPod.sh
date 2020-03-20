#
# Recreates a full Sling pod locally for Pantheon development.
# Creates the database, recompiles and rebuilds the Pantheon bundle and Slingstart.
# End result is a running Pantheon instance at http://localhost:8080
#
# There are two ways to create the database, see the specific lines to uncomment below.
#
echo "Removing Pantheon pod..."
podman pod rm -f pantheon

echo "Creating new Pantheon pod..."
podman pod create --name pantheon -p 8080 -p 5005 -p 30001 -p 30002 -p 30003

# Uncomment these lines if using a local sling database
#echo "Running single database container..."
#SLING_DB='mongodb://localhost:27017'
#podman run --pod pantheon --name slingmongo -d mongo

# Uncomment these lines if using a replica set. 
echo "Running database container replica set..."
SLING_DB='mongodb://localhost:30001,localhost:30002,localhost:30003'
podman run -d --pod pantheon --name mongo1 mongo:3.6 mongod --port 30001 --replSet mongo-repl-set
podman run -d --pod pantheon --name mongo2 mongo:3.6 mongod --port 30002 --replSet mongo-repl-set
podman run -d --pod pantheon --name mongo3 mongo:3.6 mongod --port 30003 --replSet mongo-repl-set
echo "Waiting for Mongo replica set containers to come up..."
sleep 5s
echo "Configuring Mongo replica set..."
MONGO_COMMANDS=$(cat <<-END
    db = (new Mongo('localhost:30001')).getDB('test')
    config = {
      "_id" : "mongo-repl-set",
      "members" : [
        {
           "_id" : 0,
           "host" : "localhost:30001"
        },
	{
           "_id" : 1,
           "host" : "localhost:30002"
        },
	{
           "_id" : 2,
           "host" : "localhost:30003"
        }]
    }
    rs.initiate(config)
END
)
mongo --port 30001 --eval  "$MONGO_COMMANDS"

echo "Building Pantheon bundle and slingstart..."
./mvnw clean package -DskipTests

echo "Building Pantheon container image..."
buildah bud --layers -f container/Dockerfile -t pantheon-app .

echo "Running new Pantheon container..."
podman run --pod pantheon -d -e SLING_OPTS="-Dsling.run.modes=oak_mongo -Doak.mongo.uri=$SLING_DB" --name pantheon-app pantheon-app

