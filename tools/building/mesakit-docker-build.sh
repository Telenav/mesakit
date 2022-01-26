#!/bin/bash

cd "$MESAKIT_HOME"/tools/building/docker || exit

LOWERCASE_VERSION=$(echo "$MESAKIT_VERSION" | tr '[:upper:]' '[:lower:]')

docker build \
    --progress=plain \
    --no-cache \
    --build-arg ENV_MESAKIT_VERSION="$MESAKIT_VERSION" \
    -t "jonathanlocke/mesakit:$LOWERCASE_VERSION" .
