#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

cd $AONIA_WORKSPACE

AONIA_VERSION=$(cat $AONIA_HOME/project.properties | grep "project-version" | cut -d'=' -f2 | xargs echo)

if [ -d "$HOME/.aonia/$AONIA_VERSION" ]; then
    read -p "┋ Remove ALL cached files in ~/.aonia/$AONIA_VERSION (y/n)? " -n 1 -r
    echo "┋ "
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        rm -rf ~/.aonia/$AONIA_VERSION
    fi
fi

read -p "┋ Remove temporary files (.DS_Store, .metadata, .classpath, .project, *.hprof, *~) from source tree (y/n)? " -n 1 -r
echo " "
echo "┋ "
if [[ $REPLY =~ ^[Yy]$ ]]; then
    find $AONIA_HOME \( -name \.DS_Store -o -name \.metadata -o -name \.classpath -o -name \.project -o -name \*\.hprof -o -name \*~ \) | xargs rm
fi
