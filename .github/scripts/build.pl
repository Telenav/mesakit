#!/usr/bin/perl

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

#
# Include build script from cactus-build
#

system("git clone --branch develop --quiet https://github.com/Telenav/cactus-build.git");

require "./cactus-build/.github/scripts/build-include.pl";

#
# Get build type and branch
#

my ($build_type, $branch) = @ARGV;
check_build_type($build_type);
check_branch($branch);

#
# Clone repositories
#

$github = "https://github.com/Telenav";

clone("$github/kivakit", $branch);
clone("$github/kivakit-extensions", $branch);
clone_this("$github/mesakit", $branch);

#
# Build repositories
#

build_kivakit($build_type);
build_kivakit_extensions($build_type);
build_mesakit($build_type);

