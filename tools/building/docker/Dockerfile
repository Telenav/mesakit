
FROM jonathanlocke/kivakit:1.2.3-snapshot

# Get MesaKit version

ARG ENV_MESAKIT_VERSION

# Locations

ENV MESAKIT_VERSION=${ENV_MESAKIT_VERSION}
ENV MESAKIT_WORKSPACE=$HOME/workspace
ENV MESAKIT_HOME="${MESAKIT_WORKSPACE}/mesakit"

RUN mkdir -p $DEVELOPER/.mesakit && \
    ln -s $DEVELOPER/.mesakit $HOME/.mesakit

# Clone mesakit:develop

RUN cd "$MESAKIT_WORKSPACE" && \
    git clone https://github.com/Telenav/mesakit.git && \
    cd "$MESAKIT_HOME" && \
    git checkout develop

# Run setup script

RUN date +setup-time=%Y.%m.%d-%I.%M%p > ${MESAKIT_HOME}/setup.properties

RUN bash -c "source $HOME/.profile && $MESAKIT_HOME/setup/setup.sh docker"

COPY mesakit-entrypoint.sh $HOME/mesakit-entrypoint.sh
RUN chmod +x $HOME/mesakit-entrypoint.sh
ENTRYPOINT $HOME/mesakit-entrypoint.sh
