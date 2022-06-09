
if [ -z "$TELENAV_WORKSPACE" ]; then
    system_variable TELENAV_WORKSPACE "$TELENAV_WORKSPACE"
fi

source "$TELENAV_WORKSPACE"/mesakit/tools/library/mesakit-library-functions.sh

export BASH_SILENCE_DEPRECATION_WARNING=1

system_variable MESAKIT_HOME "$TELENAV_WORKSPACE/mesakit"
system_variable MESAKIT_EXTENSIONS_HOME "$TELENAV_WORKSPACE/mesakit-extensions"
system_variable MESAKIT_EXAMPLES_HOME "$TELENAV_WORKSPACE/mesakit-examples"
system_variable MESAKIT_VERSION "$(project_version "$MESAKIT_HOME")"
system_variable MESAKIT_BUILD "$(project_build "$MESAKIT_HOME")"
system_variable MESAKIT_TOOLS "$MESAKIT_HOME/tools"
system_variable MESAKIT_JAVA_OPTIONS "-Xmx12g"

if [ -z "$TELENAV_WORKSPACE" ]; then

    prepend_path "$M2_HOME/bin"
    prepend_path "$JAVA_HOME/bin"

fi

append_path "$MESAKIT_TOOLS/building"
append_path "$MESAKIT_TOOLS/developing"
append_path "$MESAKIT_TOOLS/library"
append_path "$MESAKIT_TOOLS/releasing"
append_path "$MESAKIT_EXTENSIONS_HOME/tools/applications"

system_variable MESAKIT_ASSETS_HOME "$TELENAV_WORKSPACE/mesakit-assets"
system_variable MESAKIT_CACHE_HOME "$HOME/.mesakit/$MESAKIT_VERSION"

source "$MESAKIT_TOOLS"/library/mesakit-projects.sh

if [ -z "$TELENAV_WORKSPACE" ]; then

    source_project_profile "cactus-build"

fi

echo " "
echo "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫ MesaKit Environment ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓"
echo "┋"
echo -e "┋        TELENAV_WORKSPACE: ${ATTENTION}$TELENAV_WORKSPACE${NORMAL}"
echo -e "┋             MESAKIT_HOME: ${ATTENTION}$MESAKIT_HOME${NORMAL}"
echo -e "┋          MESAKIT_VERSION: ${ATTENTION}$MESAKIT_VERSION${NORMAL}"
echo -e "┋            MESAKIT_BUILD: ${ATTENTION}$MESAKIT_BUILD${NORMAL}"
echo "┋"
echo "┋       MESAKIT_CACHE_HOME: $MESAKIT_CACHE_HOME"
echo "┋      MESAKIT_ASSETS_HOME: $MESAKIT_ASSETS_HOME"
echo "┋  MESAKIT_EXTENSIONS_HOME: $MESAKIT_EXTENSIONS_HOME"
echo "┋    MESAKIT_EXAMPLES_HOME: $MESAKIT_EXAMPLES_HOME"
echo "┋"
echo "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛"
echo " "

date +setup-time=%Y.%m.%d-%I.%M%p > "$TELENAV_WORKSPACE"/mesakit/setup.properties

cd "$TELENAV_WORKSPACE"

if [ "$SHOW_SYSTEM_ENVIRONMENT" != "false" ]; then

    echo " "
    echo "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫ System Environment ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓"
    echo "┋"
    echo "┋                     Java: $JAVA_HOME"
    echo "┋                    Maven: $M2_HOME"
    perl -e 'print "┋                     Path: " . join("\n┋                           ", split(":", $ENV{"PATH"})) . "\n"'
    echo "┋"
    echo "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛"
    echo " "

fi
