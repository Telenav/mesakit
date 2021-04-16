#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

MESAKIT_VERSION=$(cat $MESAKIT_HOME/project.properties | grep "project-version" | cut -d'=' -f2 | xargs echo)

MESAKIT_BUILD_NAME=$(cat $MESAKIT_HOME/build.properties | grep "build-name" | cut -d'=' -f2 | xargs echo)
MESAKIT_BUILD_NUMBER=$(cat $MESAKIT_HOME/build.properties | grep "build-number" | cut -d'=' -f2 | xargs echo)
MESAKIT_BUILD_DATE=$(cat $MESAKIT_HOME/build.properties | grep "build-date" | cut -d'=' -f2 | xargs echo)

echo "MesaKit $MESAKIT_VERSION (#$MESAKIT_BUILD_NUMBER $MESAKIT_BUILD_DATE \"$MESAKIT_BUILD_NAME\")"
