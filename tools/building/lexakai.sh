#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

if [ -z "$AONIA_PROJECT_HOME" ]; then
    AONIA_PROJECT_HOME=$AONIA_HOME
fi

PROJECT_VERSION=$(cat $AONIA_PROJECT_HOME/project.properties | grep "project-version" | cut -d'=' -f2 | xargs echo)
AONIA_VERSION=$(cat $AONIA_HOME/project.properties | grep "project-version" | cut -d'=' -f2 | xargs echo)

# -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044

java -jar $AONIA_HOME/tools/building/bin/lexakai-0.9.3.jar -project-version=$PROJECT_VERSION -update-readme=true $@
