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

import com.telenav.kivakit.component.BaseComponent;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.network.http.HttpNetworkLocation;
import com.telenav.kivakit.network.http.HttpPostResource;
import com.telenav.kivakit.resource.packages.PackageTrait;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.overpass.internal.lexakai.DiagramOverpass;

import static com.telenav.kivakit.core.progress.reporters.BroadcastingProgressReporter.progressReporter;
import static com.telenav.kivakit.core.string.Formatter.format;
import static com.telenav.kivakit.resource.CopyMode.OVERWRITE;

/**
 * <b>Not public API</b>
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramOverpass.class)
@UmlRelation(label = "copies data to", referent = Folder.class)
class OverpassOsmResource extends BaseComponent implements PackageTrait
{
    private final Rectangle bounds;

    private final String template = packageResource("OverpassRequestTemplate.txt")
            .reader()
            .asString();

    public OverpassOsmResource(Rectangle bounds)
    {
        this.bounds = bounds;
        if (bounds.widestWidth().isGreaterThan(OverpassDataDownloader.MAXIMUM_SIZE))
        {
            throw new IllegalArgumentException("Bounds " + bounds + " is " + bounds.widestWidth()
                    + " wide, which exceeds the maximum of " + OverpassDataDownloader.MAXIMUM_SIZE);
        }
        if (bounds.heightAsDistance().isGreaterThan(OverpassDataDownloader.MAXIMUM_SIZE))
        {
            throw new IllegalArgumentException("Bounds " + bounds + " is " + bounds.heightAsDistance()
                    + " high, which exceeds the maximum of " + OverpassDataDownloader.MAXIMUM_SIZE);
        }
    }

    public void safeCopyTo(File destination)
    {
        location(bounds).safeCopyTo(destination, OVERWRITE, progressReporter(this, "bytes"));
    }

    private HttpPostResource location(Rectangle bounds)
    {
        var payload = format(template, bounds.toCommaSeparatedString());
        return new HttpNetworkLocation(require(OverpassDataDownloader.class).HOST.http().path(this, "/api/interpreter")).post(payload);
    }
}
