#!/bin/bash
if command -v yarn ; then
	if [ -e frontend/node_modules/.bin/jest ] ; then
		yarn --cwd frontend test -u
	fi
fi

