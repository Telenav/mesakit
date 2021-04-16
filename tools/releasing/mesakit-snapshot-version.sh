#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

NEW_VERSION="${1%-SNAPSHOT}-SNAPSHOT"

if [ -z "$NEW_VERSION" ]; then

    echo "Usage: mesakit-snapshot.sh [new-version-number]"
    exit 0

else

    MESAKIT_VERSION=$(cat $MESAKIT_HOME/project.properties | grep "project-version" | cut -d'=' -f2 | xargs echo)

    echo " "
    echo "Updating MesaKit version from $MESAKIT_VERSION to $NEW_VERSION"

    # Update POM versions
    update-version.pl $MESAKIT_HOME $MESAKIT_VERSION $NEW_VERSION

    echo "Updated"
    echo " "

fi
