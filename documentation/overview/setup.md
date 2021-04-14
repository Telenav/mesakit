# Aonia - Setup   ![](../images/box-40.png)

![](../images/horizontal-line.png)

## Setting Up Aonia

Whether you plan to use Aonia or help to develop it, this page will help you get rolling in 3 easy steps.

### Prerequisites

*You will need to set up [KivaKit](https://github.com/Telenav/aonia) for development.*

![](../images/horizontal-line.png)

### 1. Checking Out the Project  ![](../images/down-arrow-32.png)

The Aonia project will look roughly like this when we're done with the setup process:
 
> * Workspace
>   * aonia
>     * aonia-map
>     * aonia-graph
>     * [...]

Notice how the *aonia* project (**AONIA_HOME**) is checked out in the IDE workspace called  
*Workspace* (**AONIA_WORKSPACE**).

To check out the *aonia* project:

1. If you're running macOS, and you want to switch your shell from *zsh* to *bash*, type:

       chsh -s /bin/bash

   If you don't like macOS complaining that you're not using *zsh*, add this line to your *.bash_profile*:

       export BASH_SILENCE_DEPRECATION_WARNING=1

2. Open a *bash* shell and go to your IDE workspace (the folder *Workspace* above)
3. Clone the *aonia* git repository into your workspace
   
       cd Workspace 
       git clone git@github.com:Telenav/aonia.git

![](../images/horizontal-line.png)

### 2. Setting Up Your Environment   ![](../../documentation/images/bluebook-32.png)

To configure your environment, you will need several environment variables set.

> Without the correct environment variables set, the setup script in Step 3 won't work.

1. Copy the provided aonia-profile to your root folder 
   
        cp aonia/setup/aonia-profile ~

2.  *If you have checked Aonia out in a different workspace* from KivaKit, you will need to  
    change the **AONIA_WORKSPACE** variable to the workspace where you checked it out.
    
        export AONIA_WORKSPACE=$HOME/Workspace

   > **AONIA_WORKSPACE** must point to your *workspace* **NOT** the *aonia* project in the workspace

3. Add to the end of ~/.profile (which should include KivaKit setup) this command:

        source ~/.aonia-profile

4. Start a new shell window

![](../images/horizontal-line.png)

### 3. Finishing the Job  ![](../images/stars-32.png)

Once you have cloned the project into your workspace and set up your environment,  
you can complete your set up with one final command:

    $AONIA_HOME/setup/setup.sh

![](../images/horizontal-line.png)

### Done!   ![](../images/rocket-40.png)

Congratulations! You're set up and ready to build or help to develop Aonia.

![](../images/horizontal-line.png)

### Next Steps &nbsp; &nbsp;  ![](../images/footprints-40.png)

[I want to build Aonia](building.md)

[I want to work on Aonia](../developing/index.md)

![](../images/horizontal-line.png)
