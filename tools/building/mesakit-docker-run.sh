#!/bin/bash

cd "$MESAKIT_HOME"/tools/building/docker || exit

if [ "$1" = "" ]; then
    VERSION=$(echo "$MESAKIT_VERSION" | tr '[:upper:]' '[:lower:]')
else
    VERSION=$(echo "$1" | tr '[:upper:]' '[:lower:]')
fi

docker run \
    -v "$MESAKIT_WORKSPACE:/host/workspace" \
    -ti "jonathanlocke/mesakit:$VERSION" \
    /bin/bash
