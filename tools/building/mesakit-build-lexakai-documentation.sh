#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

source mesakit-library-functions.sh
source mesakit-projects.sh

for project_home in "${MESAKIT_PROJECT_HOMES[@]}"; do

    project_name=$(project_name "$project_home")

    lexakai -project-version="$MESAKIT_VERSION" -output-folder="$MESAKIT_ASSETS_HOME"/docs/"$MESAKIT_VERSION"/lexakai/"$project_name" "$project_home"

done
