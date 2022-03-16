#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

source mesakit-library-functions.sh
source mesakit-projects.sh

help="[branch]"

branch=$1

require_variable branch "$help"

for project_home in "${MESAKIT_PROJECT_HOMES[@]}"; do

    cd "$project_home" && echo "Updating $project_home" && git checkout "$branch"

done

