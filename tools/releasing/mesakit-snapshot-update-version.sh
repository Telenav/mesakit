#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

source mesakit-library-functions.sh
source mesakit-projects.sh

version="$1"

export help="[version]"

require_variable version "[version]"

export snapshot_version="${version%-SNAPSHOT}-SNAPSHOT"

for project_home in "${MESAKIT_REPOSITORY_HOMES[@]}"; do

    update_version "$project_home" "$snapshot_version"

done

