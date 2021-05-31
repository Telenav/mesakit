////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.kivakit.graph.specifications.common.place;

import com.telenav.kivakit.kernel.language.object.Lazy;
import com.telenav.kivakit.graph.Place;
import com.telenav.kivakit.graph.metadata.DataSpecification;
import com.telenav.kivakit.graph.specifications.common.CommonDataSpecification;
import com.telenav.kivakit.graph.specifications.common.element.GraphElementAttributes;

public class PlaceAttributes extends GraphElementAttributes<Place>
{
    // The attributes in this class are shared (from the common data specification) so they need to have the
    // same identifiers in all subclasses. See the superclass of this class for details.

    private static final Lazy<PlaceAttributes> singleton = new Lazy<>(PlaceAttributes::new);

    public static PlaceAttributes get()
    {
        return singleton.get();
    }

    public class PlaceAttribute extends GraphElementAttribute
    {
        public PlaceAttribute(final String name)
        {
            super(name);
        }
    }

    public final PlaceAttribute LOCATION = new PlaceAttribute("LOCATION");

    public final PlaceAttribute NAME = new PlaceAttribute("NAME");

    public final PlaceAttribute TYPE = new PlaceAttribute("TYPE");

    public final PlaceAttribute POPULATION = new PlaceAttribute("POPULATION");

    protected PlaceAttributes()
    {
    }

    @Override
    protected DataSpecification dataSpecification()
    {
        return CommonDataSpecification.get();
    }
}
