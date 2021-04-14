#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

cd $AONIA_WORKSPACE

AONIA_VERSION=$(cat $AONIA_HOME/project.properties | grep "project-version" | cut -d'=' -f2 | xargs echo)

read -p "┋ Remove maven repository (y/n)? " -n 1 -r
echo "┋ "
if [[ $REPLY =~ ^[Yy]$ ]]; then
    rm -rf ~/.m2
fi

bash aonia-clean.sh
