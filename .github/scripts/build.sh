#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

source build-include.sh

build_type=$(check_build_type "$1")

clone_this
clone kivakit
clone kivakit-extensions
clone kivakit-examples

build_kivakit "$build_type"
build_kivakit_extensions "$build_type"
build_kivakit_examples "$build_type"
build "$build_type"
