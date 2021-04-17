# MesaKit - Building   ![](../images/gears-40.png)

![](../images/horizontal-line.png)

### Building MesaKit

MesaKit can be built in [IntelliJ](https://www.jetbrains.com/idea/download/) or from the command line with Maven or the convenient scripts
below.

### Key Build Scripts

Once you have completed the [Setup](setup.md) process, it is easy to build projects from the command line with **mesakit-build.sh**.

| Script | Purpose |
|--------|---------|
| *mesakit-build.sh* | Builds MesaKit using the givens build type and modifiers (see below) |
| *mesakit-build-documentation.sh* | Builds Javadoc and Lexakai documentation |
| *mesakit-version.sh* | Shows the version of MesaKit you are building |

MesaKit scripts are named so that you can easily discover them with command-line completion.

To see what scripts are available, type "mesakit" and hit tab.

### Build Parameters

The **mesakit-build.sh** script takes a build-type parameter and zero or more build-modifier parameters.
These parameters are translated into a particular set of maven switches and arguments. To see what
build types are available, run *mesakit-build.sh help*:

Usage: mesakit-build.sh *[build-type] [build-modifiers]*

**Build Types**:

           [default] - compile, shade and run quick tests

                 all - all-clean, compile, shade, run tests, build tools and javadoc

               tools - compile, shade, run tests, build tools

             compile - compile and shade (no tests)

             javadoc - compile and build javadoc

**Build Modifiers**:

               clean - prompt to remove cached and temporary files

           all-clean - prompt to remove cached and temporary files and mesakit artifacts from ~/.m2

               debug - turn maven debug mode on

         debug-tests - stop in debugger on surefire tests

          no-javadoc - do not build javadoc

            no-tests - do not run tests

    single-threaded - build with only one thread

         quick-tests - run only quick tests

               quiet - build with minimal output

                show - show maven command line but don't build

           sparkling - prompt to remove entire .m2 repository and all cached and temporary files

               tests - run all tests

<br/> 

![](../images/horizontal-line.png)
