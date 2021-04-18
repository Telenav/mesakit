#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

source library-functions.sh
source mesakit-projects.sh

project_home=$1
version="${2%-SNAPSHOT}-SNAPSHOT"

ARGUMENT_HELP="[version]"

require_variable version

for project_home in "${MESAKIT_PROJECT_HOMES[@]}"; do

    update_version $project_home $version

done
