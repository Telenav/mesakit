#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

source "$MESAKIT_WORKSPACE"/mesakit/tools/library/library-functions.sh
source mesakit-projects.sh

for project_home in "${MESAKIT_PROJECT_HOMES[@]}"; do

    cd "$project_home" && echo "Updating $project_home" && git pull

done

