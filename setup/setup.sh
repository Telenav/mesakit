#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

if [ -z "$MESAKIT_HOME" ]; then
    echo "You must set up your environment to use MesaKit."
    echo "See https://tinyurl.com/3pn9huv6 for details."
    exit 1
fi

cd $MESAKIT_WORKSPACE
git clone git@github.com:Telenav/mesakit-data.git

cd $MESAKIT_HOME
git checkout develop
mesakit-build.sh all clean
