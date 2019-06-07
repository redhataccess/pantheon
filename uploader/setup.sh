#!/usr/bin/bash

# Variables
UPLOADER_HOME=~/.pantheon
PANTHEON_BIN=~/bin/pantheon

# Create UPLOADER_HOME directory if not exists
if [ ! -d "$UPLOADER_HOME" ]; then
  echo "Creating UPLOADER_HOME directory: $UPLOADER_HOME"
  mkdir -p $UPLOADER_HOME
fi

# Download the uploader script to $UPLOADER_HOME 
echo "Downloading the uploader script to $UPLOADER_HOME"
curl -o ${UPLOADER_HOME}/pantheon.py https://raw.githubusercontent.com/redhataccess/pantheon/master/uploader/pantheon.py

# Ensure file is executable
echo "Ensure uploader script is executble"
chmod +x ${UPLOADER_HOME}/pantheon.py 

# Create a symlink to ~/bin/pantheon if not exists
if [ ! -h "$PANTHEON_BIN" ]; then
  echo "Create a symlink to $PANTHEON_BIN"
  ln -s ${UPLOADER_HOME}/pantheon.py $PANTHEON_BIN
fi

# Show full path for pantheon command
echo
echo "UPLOADER_HOME: $UPLOADER_HOME"
echo "pantheon command path: "
which pantheon

if [[ $(which pantheon) = $PANTHEON_BIN ]]; then
  echo "set up completed!"
fi  
echo
