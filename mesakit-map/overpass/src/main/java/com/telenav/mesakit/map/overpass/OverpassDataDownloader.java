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

package com.telenav.mesakit.map.overpass;

import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.overpass.pbf.OsmToPbfConverter;
import com.telenav.mesakit.map.overpass.project.lexakai.diagrams.DiagramOverpass;
import com.telenav.kivakit.core.filesystem.File;
import com.telenav.kivakit.core.filesystem.Folder;
import com.telenav.kivakit.core.kernel.messaging.Debug;
import com.telenav.kivakit.core.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.core.network.core.Host;
import com.telenav.kivakit.core.resource.path.Extension;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.lexakai.annotations.associations.UmlRelation;

/**
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramOverpass.class)
@UmlRelation(label = "downloads data with", referent = OverpassOsmResource.class)
@UmlRelation(label = "converts data with", referent = OsmToPbfConverter.class)
public class OverpassDataDownloader extends BaseRepeater
{
    public static final Host HOST = Host.parse("overpass-api.de");

    public static final Distance MAXIMUM_SIZE = Distance.kilometers(50);

    private final Debug DEBUG = new Debug(this);

    @UmlAggregation(label = "caches data in")
    private final Folder cache;

    public OverpassDataDownloader(final Folder cache)
    {
        this.cache = cache.mkdirs();
    }

    public File osm(final String descriptor, final Rectangle bounds)
    {
        final var osm = file(descriptor, bounds).withExtension(Extension.OSM);
        if (osm.exists())
        {
            trace("Using cached overpass data for $ from $", bounds, osm);
            return osm;
        }
        else
        {
            try
            {
                trace("Downloading overpass data for $ to $", bounds, osm);
                listenTo(new OverpassOsmResource(bounds)).safeCopyTo(osm);
                trace("Downloaded $ bytes to $", osm.bytes(), osm);
                return osm;
            }
            catch (final Exception e)
            {
                problem(e, "Unable to extract $ to $", bounds, osm);
            }
            return null;
        }
    }

    public File pbf(final String baseName, final Rectangle bounds)
    {
        final var pbf = file(baseName, bounds).withExtension(Extension.OSM_PBF);
        if (pbf.exists())
        {
            trace("Using cached pbf data for $ from $", bounds, pbf);
            return pbf;
        }
        else
        {
            try
            {
                final var osm = osm(baseName, bounds);
                if (osm != null)
                {
                    new OsmToPbfConverter(this).convert(osm, pbf);
                    if (!DEBUG.isDebugOn())
                    {
                        osm.delete();
                    }
                    return pbf;
                }
            }
            catch (final Exception e)
            {
                problem(e, "Unable to extract $ to $", bounds, pbf);
            }
            return null;
        }
    }

    private File file(final String descriptor, final Rectangle bounds)
    {
        return cache.file(descriptor + "-" + bounds.toFileString());
    }
}