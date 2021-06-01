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

package com.telenav.mesakit.graph.specifications.common.element;

import com.telenav.kivakit.kernel.language.objects.Lazy;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.specifications.common.CommonDataSpecification;
import com.telenav.mesakit.graph.specifications.library.attributes.Attribute;
import com.telenav.mesakit.graph.specifications.library.properties.GraphElementPropertySet;

@SuppressWarnings("rawtypes")
public class GraphElementProperties<T extends GraphElement> extends GraphElementPropertySet<T>
{
    private static final Lazy<GraphElementProperties> singleton = Lazy.of(GraphElementProperties::new);

    public static GraphElementProperties get()
    {
        return singleton.get();
    }

    public abstract class GraphElementProperty extends com.telenav.mesakit.graph.specifications.library.properties.GraphElementProperty<T>
    {
        protected GraphElementProperty(final String name, final Attribute<?> attribute)
        {
            super(name, attribute, CommonDataSpecification.get());
            add(this);
        }
    }

    public final GraphElementProperty PBF_CHANGE_SET_IDENTIFIER = new GraphElementProperty("pbf-change-set", GraphElementAttributes.get().PBF_CHANGE_SET_IDENTIFIER)
    {
        @Override
        public Object value(final T element)
        {
            if (element.supports(GraphElementAttributes.get().PBF_CHANGE_SET_IDENTIFIER))
            {
                return element.pbfChangeSetIdentifier();
            }
            return null;
        }
    };

    public final GraphElementProperty LAST_MODIFIED = new GraphElementProperty("last-modified", GraphElementAttributes.get().LAST_MODIFIED)
    {
        @Override
        public Object value(final T element)
        {
            if (element.supports(GraphElementAttributes.get().LAST_MODIFIED))
            {
                return element.lastModificationTime();
            }
            return null;
        }
    };

    public final GraphElementProperty PBF_REVISION_NUMBER = new GraphElementProperty("pbf-revision", GraphElementAttributes.get().PBF_REVISION_NUMBER)
    {
        @Override
        public Object value(final T element)
        {
            if (element.supports(GraphElementAttributes.get().PBF_REVISION_NUMBER))
            {
                return element.pbfRevisionNumber();
            }
            return null;
        }
    };

    public final GraphElementProperty PBF_USER_IDENTIFIER = new GraphElementProperty("pbf-user-identifier", GraphElementAttributes.get().PBF_USER_IDENTIFIER)
    {
        @Override
        public Object value(final T element)
        {
            if (element.supports(GraphElementAttributes.get().PBF_USER_IDENTIFIER))
            {
                return element.pbfUserIdentifier();
            }
            return null;
        }
    };

    public final GraphElementProperty PBF_USER_NAME = new GraphElementProperty("pbf-user-name", GraphElementAttributes.get().PBF_USER_NAME)
    {
        @Override
        public Object value(final T element)
        {
            if (element.supports(GraphElementAttributes.get().PBF_USER_NAME))
            {
                return element.pbfUserName();
            }
            return null;
        }
    };

    public final GraphElementProperty TAGS = new GraphElementProperty("tags", GraphElementAttributes.get().TAGS)
    {
        @Override
        public Object value(final T element)
        {
            return element.tagList();
        }
    };

    public final GraphElementProperty IDENTIFIER = new GraphElementProperty("identifier", GraphElementAttributes.get().IDENTIFIER)
    {
        @Override
        public Object value(final T element)
        {
            return element.identifier();
        }
    };

    public final GraphElementProperty INDEX = new GraphElementProperty("index", GraphElementAttributes.get().NONE)
    {
        @Override
        public Object value(final T point)
        {
            return point.index();
        }
    };

    public final GraphElementProperty MAP_IDENTIFIER = new GraphElementProperty("map-identifier", GraphElementAttributes.get().NONE)
    {
        @Override
        public Object value(final T element)
        {
            return element.mapIdentifier();
        }
    };

    protected GraphElementProperties()
    {
    }
}
