#!/bin/bash
while true
do
  STATUS=$(curl --max-time 4 -s -o /dev/null -w '%{http_code}' http://0.0.0.0:8080/is_alive)
  if [ $STATUS -eq 404 ]; then
    echo "Got 404! Sling should be ready!"
    mv /pantheon-1.0-SNAPSHOT.jar /install/
    break
  else
    echo "Got $STATUS :( Not done yet..."
  fi
  sleep 10
done