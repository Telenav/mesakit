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

import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.specifications.common.CommonDataSpecification;
import com.telenav.mesakit.graph.specifications.library.attributes.Attribute;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeList;
import com.telenav.kivakit.kernel.language.object.Lazy;

/**
 * Attributes that are common to all {@link GraphElement}s. Note that there is not yet any abstraction for the revision
 * information that's used in PBF data, but in the future an abstraction could be added if need be
 *
 * @param <T> The type of graph element
 */
public class GraphElementAttributes<T extends GraphElement> extends AttributeList
{
    // GraphElement attributes need to have the same identifier, because they are held in different subclasses of
    // GraphElementAttributes and so the attributes themselves will have different instances, but we need a fast
    // way to compare. Using the same identifier for shared attributes like this make this possible.

    private static final Lazy<GraphElementAttributes<?>> singleton = Lazy.of(GraphElementAttributes::new);

    public static GraphElementAttributes<?> get()
    {
        return singleton.get();
    }

    public class GraphElementAttribute extends Attribute<T>
    {
        public GraphElementAttribute(final String name)
        {
            super(name);
            add(this);
        }
    }

    public final GraphElementAttribute NONE = new GraphElementAttribute("NONE");

    public final GraphElementAttribute TAGS = new GraphElementAttribute("TAGS");

    public final GraphElementAttribute LAST_MODIFIED = new GraphElementAttribute("LAST_MODIFICATION_TIME");

    public final GraphElementAttribute PBF_REVISION_NUMBER = new GraphElementAttribute("PBF_REVISION_NUMBER");

    public final GraphElementAttribute PBF_CHANGE_SET_IDENTIFIER = new GraphElementAttribute("PBF_CHANGE_SET_IDENTIFIER");

    public final GraphElementAttribute PBF_USER_NAME = new GraphElementAttribute("PBF_USER_NAME");

    public final GraphElementAttribute PBF_USER_IDENTIFIER = new GraphElementAttribute("PBF_USER_IDENTIFIER");

    public final GraphElementAttribute IDENTIFIER_TO_INDEX = new GraphElementAttribute("IDENTIFIER_TO_INDEX");

    public final GraphElementAttribute IDENTIFIER = new GraphElementAttribute("IDENTIFIER");

    protected GraphElementAttributes()
    {
    }

    protected DataSpecification dataSpecification()
    {
        return CommonDataSpecification.get();
    }
}
