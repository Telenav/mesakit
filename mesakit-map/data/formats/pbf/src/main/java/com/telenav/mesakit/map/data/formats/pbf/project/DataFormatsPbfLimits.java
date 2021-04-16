////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.data.formats.pbf.project;

import com.telenav.kivakit.core.kernel.language.values.count.Maximum;

public class DataFormatsPbfLimits
{
    // Maximum per non-world PBF file (world PBF files have no limits)
    public static final Maximum MAXIMUM_ENTITIES = Maximum.maximum(10_000_000);

    public static final Maximum MAXIMUM_USERS = Maximum.maximum(1_000_000);

    public static final Maximum MAXIMUM_EDGES_PER_WAY = Maximum.maximum(10_000);

    public static final Maximum MAXIMUM_WAYS_PER_RELATION = Maximum.maximum(10_000);
}
