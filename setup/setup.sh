#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

echo " "
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫ Preparing for Setup"
echo " "

if [ -z "$MESAKIT_WORKSPACE" ]; then
    echo " "
    echo "Please set up your .profile before setting up MesaKit."
    echo "See https://mesakit.org for details."
    echo " "
    exit 1
fi

cd "$MESAKIT_WORKSPACE"/mesakit
git checkout -q develop

if [ ! -e "$MESAKIT_WORKSPACE/mesakit/setup.properties" ]; then
    echo " "
    echo "Please restart your shell before continuing MesaKit setup."
    echo "See https://mesakit.org for details."
    echo " "
    exit 1
fi

#
# Check out required repositories
#

echo " "
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫ Cloning Repositories"
echo " "

cd "$MESAKIT_WORKSPACE"

git clone https://github.com/Telenav/mesakit.git
git clone https://github.com/Telenav/mesakit-extensions.git
git clone https://github.com/Telenav/mesakit-examples.git
git clone https://github.com/Telenav/mesakit-assets.git

#
# Initialize git flow for each project
#

echo " "
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫ Initializing Git Flow"
echo " "

initialize() {

    project_home=$1
    branch=$2

    cd "$project_home"
    echo "Initializing $(pwd)"
    git checkout "$branch"
    git config pull.ff only

    if [[ $branch == "develop" ]]; then

        git flow init -d /dev/null 2>&1

        if [ "$(git flow config >/dev/null 2>&1)" ]; then
            echo " "
            echo "Please install git flow before continuing MesaKit setup."
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

#
# Install Maven super POM
#

mesakit-maven-setup.sh

#
# Build
#

echo " "
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫ Building Projects"
echo " "

cd "$MESAKIT_HOME"
mesakit-build.sh setup

echo " "
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫ Setup Complete"
echo " "
