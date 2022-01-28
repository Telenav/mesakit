#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

#
# Clone required repositories
#

echo " "
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫ Cloning Repositories"
echo " "

mkdir -p "$MESAKIT_WORKSPACE"
cd "$MESAKIT_WORKSPACE"

git clone --quiet https://github.com/Telenav/mesakit.git
git clone https://github.com/Telenav/mesakit-extensions.git
git clone https://github.com/Telenav/mesakit-examples.git
git clone --depth 1 https://github.com/Telenav/mesakit-assets.git

#
# Initialize git for each project
#

echo " "
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫ Initializing Git"
echo " "

initialize() {

    project_home=$1
    branch=$2

    cd "$project_home"
    echo " "
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━┫ Initializing $(pwd)"
    echo " "
    git checkout "$branch"
    git config pull.ff only

    if [[ $branch == "develop" ]]; then

        git flow init -d /dev/null 2>&1

        if [ "$(git flow config >/dev/null 2>&1)" ]; then
            echo " "
            echo "Please install git flow before continuing setup."
            echo "See https://mesakit.org for details."
            echo " "
            exit 1
        fi

    fi
}

initialize "$KIVAKIT_WORKSPACE"/mesakit develop
initialize "$KIVAKIT_WORKSPACE"/mesakit-extensions develop
initialize "$KIVAKIT_WORKSPACE"/mesakit-examples develop
initialize "$KIVAKIT_WORKSPACE"/mesakit-assets publish
