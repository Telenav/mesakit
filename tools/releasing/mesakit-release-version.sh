#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

NEW_VERSION=$1

if [ -z "$NEW_VERSION" ]; then

    echo "Usage: mesakit-release-version.sh [new-version-number]"
    exit 0

else

    CURRENT_VERSION=$(cat $MESAKIT_HOME/project.properties | grep "project-version" | cut -d'=' -f2 | xargs echo)

    echo " "
    echo "Updating MesaKit version from $CURRENT_VERSION to $NEW_VERSION"

    # Update POM versions and .md files
    update-version.pl $MESAKIT_HOME $CURRENT_VERSION $NEW_VERSION

    # Update project.properties file
    perl -pi -e "s/$CURRENT_VERSION/$NEW_VERSION/g" $MESAKIT_HOME/project.properties

    echo "Updated"
    echo " "

fi
