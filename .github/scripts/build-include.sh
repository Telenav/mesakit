#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

WORKSPACE="$(pwd)"
KIVAKIT_HOME="$WORKSPACE/kivakit"
MESAKIT_HOME="$WORKSPACE/mesakit"
PASSPHRASE="${{ secrets.OSSRH_GPG_SECRET_KEY_PASSWORD }}"

#
# Print error message and exit
#

die() {

    echo " "
    echo "Fatal problem: $1"
    echo " "
    exit 1
}

#
# Print the given string
#

say() {

    echo "===> $1"
}

#
# Checks the given branch name
#

check_branch() {

    if [[ -z "$1" ]]; then
        die "Must supply a branch"
    fi

    echo "$1"
}

#
# Checks the build type
#

check_build_type() {

    build_type=$1

    case "${build_type}" in

    "normal")
        ;;

    "publish")
        ;;

    "*")
        die "Unrecognized build type: $build_type"
        ;;

    esac

    echo "$1"
}

#
# Check the given repository name, returning the full GitHub URL if it is valid
#

check_repository() {

    if [[ -z "$1" ]]; then
        die "Must supply a repository"
    fi

    echo "https://github.com/Telenav/$1.git"
}

#
# Get any branch name for this build
#

github_branch() {

    echo "${GITHUB_REF/refs\/heads\//}"
}

#
# Get any pull-request identifier for this build
#

github_pull_request_identifier() {

    PATTERN='.*\/([0-9)]+)/.*'
    if [[ "$GITHUB_REF" =~ $PATTERN ]]
    then
        echo "${BASH_REMATCH[1]}"
    fi
}

#
# Returns true if a pull-request is being built
#

is_github_pull_request() {

    if [[ -z "$(github_pull_request_identifier)" ]]; then
        0
    else
        1
    fi
}

#
# Install the pom.xml file in the current folder
#

install_pom() {

    cd "$1"
    mvn --batch-mode --no-transfer-progress clean install
}

#
# Clones the given repository and branch
#

clone() {

    repository=$(check_repository "$1")
    branch=$(check_branch "$2")

    cd "$WORKSPACE"
    say "Cloning $repository ($branch)"
    git clone --branch "$branch" --quiet "$repository"
}

#
# Clones the given pull request identifier in the given repository into the given branch
#

clone_pull_request() {

    repository=$(check_repository "$1")
    pull_request_identifier=$2
    branch=$(check_branch "$3")

    cd "$WORKSPACE"
    say "Cloning pull request $pull_request_identifier into branch $branch"
    clone "$repository" master
    git fetch origin "pull/$pull_request_identifier/head:$branch"
    git checkout "$branch"
}

clone_this() {

    repository=$(check_repository "$1")
    branch=$(check_branch "$1")

    # shellcheck disable=SC2091
    if $(is_github_pull_request); then

        # shellcheck disable=SC2046
        clone_pull_request "$repository" $(github_pull_request_identifier) "$branch"

    else

        clone "$repository" "$branch"

    fi
}

#
# Build project
#

build() {

    build_type=$(check_build_type "$1")

    case "${build_type}" in

    "normal")
        mvn -Dmaven.javadoc.skip=true -DKIVAKIT_DEBUG="!Debug" -P shade -P tools --no-transfer-progress --batch-mode clean install
        ;;

    "publish")
        mvn -P attach-jars -P sign-artifacts -P shade -P tools --no-transfer-progress --batch-mode -Dgpg.passphrase="$PASSPHRASE" clean deploy
        ;;

    "*")
        die "Unrecognized build type: $build_type"
        ;;

    esac
}

build_kivakit() {

    build_type=$(check_build_type "$1")

    say "Installing kivakit super POM"
    install_pom "$KIVAKIT_HOME/superpom"

    say "Building kivakit ($build_type)"
    cd "$KIVAKIT_HOME"
    build "$build_type"
}

build_kivakit_extensions() {

    build_type=$(check_build_type "$1")

    say "Building kivakit-extensions ($build_type)"
    cd "$WORKSPACE"/kivakit-extensions
    build "$build_type"
}

build_kivakit_examples() {

    build_type=$(check_build_type "$1")

    say "Building kivakit-examples ($build_type)"
    cd "$WORKSPACE"/kivakit-examples
    build "$build_type"
}

build_mesakit() {

    build_type=$(check_build_type "$1")

    say "Installing mesakit super POM"
    install_pom "$MESAKIT_HOME/superpom"

    say "Installing shape file reader"
    mvn install:install-file -Dfile="$WORKSPACE/mesakit/mesakit-map/geography/libraries/shapefilereader-1.0.jar" -DgroupId=org.nocrala -DartifactId=shapefilereader -Dversion=1.0 -Dpackaging=jar

    say "Building mesakit ($build_type)"
    cd "$MESAKIT_HOME"
    build "$build_type"
}

build_mesakit_extensions() {

    build_type=$(check_build_type "$1")

    say "Building mesakit-extensions ($build_type)"
    cd "$WORKSPACE"/mesakit-extensions
    build "$build_type"
}

build_mesakit_examples() {

    build_type=$(check_build_type "$1")

    say "Building mesakit-examples ($build_type)"
    cd "$WORKSPACE"/mesakit-examples
    build "$build_type"
 }

