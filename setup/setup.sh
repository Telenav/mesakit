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

cd "$MESAKIT_WORKSPACE"/mesakit || exit
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

cd "$MESAKIT_WORKSPACE" || exit

bash "$MESAKIT_WORKSPACE"/mesakit/setup/setup-repositories.sh

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

cd "$MESAKIT_HOME" || exit
mesakit-build.sh setup

echo " "
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫ Setup Complete"
echo " "
