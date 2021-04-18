#―――― MesaKit ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――

system_variable MESAKIT_WORKSPACE $KIVAKIT_WORKSPACE
system_variable MESAKIT_HOME "$MESAKIT_WORKSPACE/mesakit"
system_variable MESAKIT_VERSION "$(project_version $MESAKIT_HOME)"
system_variable MESAKIT_BUILD "$(project_build $MESAKIT_HOME)"
system_variable MESAKIT_TOOLS "$MESAKIT_HOME/tools"

append_path "$MESAKIT_TOOLS/building"
append_path "$MESAKIT_TOOLS/developing"
append_path "$MESAKIT_TOOLS/library"
append_path "$MESAKIT_TOOLS/releasing"

source $MESAKIT_TOOLS/library/mesakit-projects.sh

echo " "
echo "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫ MesaKit Environment ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓"
echo "┋"
echo -e "┋          MESAKIT_WORKSPACE: ${ATTENTION}$MESAKIT_WORKSPACE${NORMAL}"
echo -e "┋               MESAKIT_HOME: ${ATTENTION}$MESAKIT_HOME${NORMAL}"
echo -e "┋            MESAKIT_VERSION: ${ATTENTION}$MESAKIT_VERSION${NORMAL}"
echo -e "┋              MESAKIT_BUILD: ${ATTENTION}$MESAKIT_BUILD${NORMAL}"
echo "┋"
echo "┋          MESAKIT_DATA_HOME: $MESAKIT_WORKSPACE"
echo "┋"
echo "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛"
echo " "

cd $MESAKIT_HOME
