#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

source mesakit-library-functions.sh
source mesakit-library-build.sh
source mesakit-projects.sh

cd "$MESAKIT_HOME"/superpom
mvn clean install

mvn install:install-file -Dfile="$MESAKIT_HOME/mesakit-map/geography/libraries/shapefilereader-1.0.jar" -DgroupId=org.nocrala -DartifactId=shapefilereader -Dversion=1.0 -Dpackaging=jar

export ALLOW_CLEANING=true

for project_home in "${MESAKIT_PROJECT_HOMES[@]}"; do

    build "$project_home" "$*"

    export ALLOW_CLEANING=false

done
