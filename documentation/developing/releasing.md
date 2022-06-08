
<a href="https://github.com/Telenav/mesakit">
<img src="https://telenav.github.io/telenav-assets/images/icons/github-32.png" srcset="https://telenav.github.io/telenav-assets/images/icons/github-32-2x.png 2x"/>
</a>
&nbsp;
<a href="https://twitter.com/openmesakit">
<img src="https://telenav.github.io/telenav-assets/images/logos/twitter/twitter-32.png" srcset="https://telenav.github.io/telenav-assets/images/logos/twitter/twitter-32-2x.png 2x"/>
</a>
&nbsp;
<a href="https://mesakit.zulipchat.com">
<img src="https://telenav.github.io/telenav-assets/images/logos/zulip/zulip-32.png" srcset="https://telenav.github.io/telenav-assets/images/logos/zulip/zulip-32-2x.png 2x"/>
</a>


## Releasing MesaKit &nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/rocket-32.png" srcset="https://telenav.github.io/telenav-assets/images/icons/rocket-32-2x.png 2x"></img>

<img src="https://telenav.github.io/telenav-assets/images/icons/horizontal-line-512.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512-2x.png 2x"></img>

### Step-by-Step Instructions &nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/footprints-32.png" srcset="https://telenav.github.io/telenav-assets/images/icons/footprints-32-2x.png 2x"></img>

This section documents how to release a new version of MesaKit, step by step.

In the text below *\[mesakit-version\]* refers to a [semantic versioning](https://semver.org) identifier, such
as 2.1.7 or 1.0.0-beta.

MesaKit adheres to the standard [Git Flow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) branching model.

### 1. Creating the Release Branch <img src="https://telenav.github.io/telenav-assets/images/icons/branch-32.png" srcset="https://telenav.github.io/telenav-assets/images/icons/branch-32-2x.png 2x"></img>

Start a new release branch with the following command:

    mesakit-release-start.sh [mesakit-version]

This script does the following:

1. Creates the release branch *release/[mesakit-version\]* using git flow
2. Updates *$MESAKIT_HOME/project.properties* file
3. Updates the version of all pom.xml files to *[mesakit-version]*

Restart your terminal window to ensure all environment variables are updated.

### 2. Preparing the Release &nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/box-32.png" srcset="https://telenav.github.io/telenav-assets/images/icons/box-32-2x.png 2x"></img>

Once the release branch has been created, several steps need to be performed manually to prepare
the branch for publication.

#### 2.1 Building the Release

In order to ensure that the build will work on the build server, it is a good idea to completely
clean out your maven repository and cache folders by building the project completely from scratch:

    mesakit-build.sh all sparkling

This will remove (after prompting) the following before building:

1. Maven repository *~/.m2*
2. MesaKit cache folder *~/.mesakit/\[mesakit-version\]*
3. Temporary files, logs, etc. in the source tree

#### 2.2 Building the Documentation

The following command will build Javadoc, UML diagrams and update project README.md indexes.

    mesakit-build-documentation.sh

#### 2.3 Updating Code Flowers

To publish code flowers for the build:

1. Run *mesakit-build-codeflowers.sh* to build codeflowers for MesaKit
2. Insert in *$MESAKIT\_HOME/tools/codeflowers/site/index.html* the HTML code output by the build process
   This will change the options in the dropdown to include any new projects
3. Run *mesakit-upload-codeflowers.sh* to install the codeflowers on their web server

#### 2.4 Commit Changes

Commit any changes to the release branch.

### 3. Finishing and Publishing the Release Branch &nbsp;  <img src="https://telenav.github.io/telenav-assets/images/icons/stars-32.png" srcset="https://telenav.github.io/telenav-assets/images/icons/stars-32-2x.png 2x"></img>

The release is finished and merged into master with another script that uses git flow:

    mesakit-release-finish.sh [mesakit-version]
