#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

if [ -z "$AONIA_HOME" ]; then
    echo "You must set up your environment to use Aonia."
    echo "See https://tinyurl.com/3pn9huv6 for details."
    exit 1
fi

cd $AONIA_WORKSPACE
git clone git@github.com:Telenav/aonia-data.git

cd $AONIA_HOME
git checkout develop
aonia-build.sh all clean
