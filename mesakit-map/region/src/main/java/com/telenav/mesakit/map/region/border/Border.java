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

package com.telenav.mesakit.map.region.border;

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.polyline.Polygon;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.project.lexakai.diagrams.DiagramBorder;
import com.telenav.kivakit.core.kernel.language.objects.Objects;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensureNotNull;

@UmlClassDiagram(diagram = DiagramBorder.class)
public class Border<T extends Region<T>> implements Bounded, Intersectable
{
    private transient T region;

    private RegionIdentity identity;

    @UmlAggregation(label = "bounded by")
    private Polygon border;

    @UmlAggregation(label = "bounding rectangle")
    private Rectangle bounds;

    /**
     * Constructor called when creating borders from a PBF file
     */
    public Border(final T region, final Polygon border)
    {
        ensure(region != null);
        ensure(border != null);

        this.region = region;
        identity = region.identity();
        this.border = border;
        bounds = border.bounds();
    }

    private Border()
    {
    }

    @Override
    public Rectangle bounds()
    {
        return bounds;
    }

    public boolean contains(final Location location)
    {
        return border.contains(location);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Border)
        {
            final var that = (Border<T>) object;
            return Objects.equal(identity, that.identity);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return identity().hashCode();
    }

    public RegionIdentity identity()
    {
        return identity;
    }

    public void identity(final RegionIdentity identity)
    {
        this.identity = ensureNotNull(identity);
    }

    @Override
    public boolean intersects(final Rectangle rectangle)
    {
        return bounds.intersects(rectangle);
    }

    public boolean isValid()
    {
        return border != null || bounds != null || region != null;
    }

    public Polygon polygon()
    {
        return border;
    }

    public T region()
    {
        return region;
    }

    public void region(final T region)
    {
        this.region = region;
    }

    @Override
    public String toString()
    {
        return identity.toString();
    }
}
