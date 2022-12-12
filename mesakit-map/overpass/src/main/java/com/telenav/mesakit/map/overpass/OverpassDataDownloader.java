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

import com.telenav.kivakit.core.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.network.core.Host;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.overpass.internal.lexakai.DiagramOverpass;
import com.telenav.mesakit.map.overpass.pbf.OsmToPbfConverter;

import static com.telenav.kivakit.core.ensure.Ensure.illegalState;
import static com.telenav.kivakit.resource.Extension.OSM;
import static com.telenav.kivakit.resource.Extension.OSM_PBF;

/**
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramOverpass.class)
@UmlRelation(label = "downloads data with", referent = OverpassOsmResource.class)
@UmlRelation(label = "converts data with", referent = OsmToPbfConverter.class)
public class OverpassDataDownloader extends BaseRepeater
{
    public static final Distance MAXIMUM_SIZE = Distance.kilometers(50);

    public final Host HOST = Host.parseHost(this, "overpass-api.de");

    @UmlAggregation(label = "caches data in")
    private final Folder cache;

    public OverpassDataDownloader(Folder cache)
    {
        this.cache = cache.mkdirs();
    }

    /**
     * Downloads the given .osm file
     *
     * @param descriptor The data descriptor
     * @param bounds The bounds of the request
     * @return The .osm file
     * @throws IllegalStateException Thrown if the data cannot be downloaded
     */
    public File osm(String descriptor, Rectangle bounds)
    {
        var osm = file(descriptor, bounds).withExtension(OSM);
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
                trace("Downloaded $ bytes to $", osm.sizeInBytes(), osm);
                return osm;
            }
            catch (Exception e)
            {
                return illegalState(e, "Unable to download $ to $", bounds, osm);
            }
        }
    }

    /**
     * Returns a PBF file for the given base name and bounds
     *
     * @param baseName The base name of the PBF file
     * @param bounds The bounds of the request
     * @return The PBF file
     * @throws IllegalStateException Thrown if the PBF file cannot be produced
     */
    public File pbf(String baseName, Rectangle bounds)
    {
        var pbf = file(baseName, bounds).withExtension(OSM_PBF);
        if (pbf.exists())
        {
            trace("Using cached pbf data for $ from $", bounds, pbf);
            return pbf;
        }
        else
        {
            File osm = null;
            try
            {
                osm = osm(baseName, bounds);
                new OsmToPbfConverter(this).convert(osm, pbf);
                if (isDebugOn())
                {
                    osm.delete();
                }
                return pbf;
            }
            catch (Exception e)
            {
                return illegalState(e, "Unable to convert osm file $ ($) to $", osm, bounds, pbf);
            }
        }
    }

    private File file(String descriptor, Rectangle bounds)
    {
        return cache.file(descriptor + "-" + bounds.toFileString());
    }
}
