#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

#
# Include build script from cactus-build
#

git clone --branch "develop" --quiet "https://github.com/Telenav/cactus-build.git"
source cactus-build/.github/scripts/build-include.sh

#
# Get build type argument
#

build_type=$(check_build_type "$1")

#
# Clone repositories
#

try clone_this
try clone kivakit
try clone kivakit-extensions
clone kivakit-examples

#
# Build repositories
#

build_kivakit "$build_type"
build_kivakit_extensions "$build_type"
build_kivakit_examples "$build_type"
build "$build_type"
