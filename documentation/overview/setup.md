<a href="https://github.com/Telenav/mesakit">
<img src="https://www.kivakit.org/images/github-32.png" srcset="https://www.kivakit.org/images/github-32-2x.png 2x"/>
</a>
&nbsp;
<a href="https://twitter.com/openmesakit">
<img src="https://www.kivakit.org/images/twitter-32.png" srcset="https://www.kivakit.org/images/twitter-32-2x.png 2x"/>
</a>
&nbsp;
<a href="https://mesakit.zulipchat.com">
<img src="https://www.kivakit.org/images/zulip-32.png" srcset="https://www.kivakit.org/images/zulip-32-2x.png 2x"/>
</a>

# MesaKit - Setup   <img src="https://www.kivakit.org/images/box-32.png" srcset="https://www.kivakit.org/images/box-32-2x.png 2x"></img>

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512-2x.png 2x"></img>

## Setting Up MesaKit

Whether you plan to use MesaKit or help to develop it, this page will help you get rolling in 3 easy steps.

### Prerequisites

*You will need to set up [KivaKit](https://github.com/Telenav/mesakit) for development.*

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512-2x.png 2x"></img>

### 1. Checking Out the Project  <img src="https://www.kivakit.org/images/down-arrow-32.png" srcset="https://www.kivakit.org/images/down-arrow-32-2x.png 2x"></img>

The MesaKit project will look roughly like this when we're done with the setup process:

> * Workspace
    >
* mesakit
  >
* mesakit-map
>     * mesakit-graph
>     * [...]

Notice how the *mesakit* project (**MESAKIT_HOME**) is checked out in the IDE workspace called
*Workspace* (**MESAKIT_WORKSPACE**).

To check out the *mesakit* project:

1. If you're running macOS, and you want to switch your shell from *zsh* to *bash*, type:

       chsh -s /bin/bash

   If you don't like macOS complaining that you're not using *zsh*, add this line to your *.bash_profile*:

       export BASH_SILENCE_DEPRECATION_WARNING=1

2. Open a *bash* shell and go to your IDE workspace (the folder *Workspace* above)
3. Clone the *mesakit* git repository into your workspace

       cd Workspace 
       git clone https://github.com/Telenav/mesakit.git

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512-2x.png 2x"></img>

### 2. Setting Up Your Environment   <img src="https://www.kivakit.org/images/bluebook-32.png" srcset="https://www.kivakit.org/images/bluebook-32-2x.png 2x"/>

To configure your environment, you will need several environment variables set.

> Without the correct environment variables set, the setup script in Step 3 won't work.

1. Copy the provided mesakit-profile to your root folder

        cp mesakit/setup/mesakit-profile ~/.mesakit-profile

2. *If you have checked MesaKit out in a different workspace* from KivaKit, you will need to change the **MESAKIT_WORKSPACE** variable to the workspace where you checked it out.

       export MESAKIT_WORKSPACE=$HOME/Workspace

> **MESAKIT_WORKSPACE** must point to your *workspace* **NOT** the *mesakit* project in the workspace

3. Start a new shell window

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512-2x.png 2x"></img>

### 3. Finishing the Job  <img src="https://www.kivakit.org/images/stars-32.png" srcset="https://www.kivakit.org/images/stars-32-2x.png 2x"></img>

Once you have cloned the project into your workspace and set up your environment, you can complete your set up with one final command:

    $MESAKIT_HOME/setup/setup.sh

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512-2x.png 2x"></img>

### Done!   <img src="https://www.kivakit.org/images/rocket-32.png" srcset="https://www.kivakit.org/images/rocket-32-2x.png 2x"></img>

Congratulations! You're set up and ready to build or help to develop MesaKit.

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512-2x.png 2x"></img>

### Next Steps &nbsp; &nbsp;  <img src="https://www.kivakit.org/images/footprints-32.png" srcset="https://www.kivakit.org/images/footprints-32-2x.png 2x"></img>

[I want to build MesaKit](building.md)

[I want to work on MesaKit](../developing/index.md)

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512-2x.png 2x"></img>
