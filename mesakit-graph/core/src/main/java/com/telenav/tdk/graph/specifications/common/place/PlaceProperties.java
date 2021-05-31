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
import com.telenav.kivakit.graph.specifications.common.CommonDataSpecification;
import com.telenav.kivakit.graph.specifications.library.attributes.Attribute;
import com.telenav.kivakit.graph.specifications.library.properties.GraphElementProperty;
import com.telenav.kivakit.graph.specifications.library.properties.GraphElementPropertySet;

public class PlaceProperties extends GraphElementPropertySet<Place>
{
    private static final Lazy<PlaceProperties> singleton = new Lazy<>(PlaceProperties::new);

    public static PlaceProperties get()
    {
        return singleton.get();
    }

    public abstract class PlaceProperty extends GraphElementProperty<Place>
    {
        protected PlaceProperty(final String name, final Attribute<?> attribute)
        {
            super(name, attribute, CommonDataSpecification.get());
            add(this);
        }
    }

    public PlaceProperty LOCATION = new PlaceProperty("location", PlaceAttributes.get().LOCATION)
    {
        @Override
        public Object value(final Place place)
        {
            return place.location();
        }
    };

    public PlaceProperty NAME = new PlaceProperty("name", PlaceAttributes.get().NAME)
    {
        @Override
        public Object value(final Place place)
        {
            return place.name();
        }
    };

    public PlaceProperty TYPE = new PlaceProperty("type", PlaceAttributes.get().TYPE)
    {
        @Override
        public Object value(final Place place)
        {
            return place.type();
        }
    };

    public PlaceProperty POPULATION = new PlaceProperty("population", PlaceAttributes.get().POPULATION)
    {
        @Override
        public Object value(final Place place)
        {
            return place.population();
        }
    };

    protected PlaceProperties()
    {
    }
}
