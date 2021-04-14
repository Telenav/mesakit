#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

AONIA_VERSION=$(cat $AONIA_HOME/project.properties | grep "project-version" | cut -d'=' -f2 | xargs echo)

AONIA_BUILD_NAME=$(cat $AONIA_HOME/build.properties | grep "build-name" | cut -d'=' -f2 | xargs echo)
AONIA_BUILD_NUMBER=$(cat $AONIA_HOME/build.properties | grep "build-number" | cut -d'=' -f2 | xargs echo)
AONIA_BUILD_DATE=$(cat $AONIA_HOME/build.properties | grep "build-date" | cut -d'=' -f2 | xargs echo)

echo "Aonia $AONIA_VERSION (#$AONIA_BUILD_NUMBER $AONIA_BUILD_DATE \"$AONIA_BUILD_NAME\")"
