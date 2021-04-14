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

package com.telenav.aonia.map.overpass;

import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;
import com.telenav.aonia.map.overpass.project.lexakai.diagrams.DiagramOverpass;
import com.telenav.kivakit.core.filesystem.File;
import com.telenav.kivakit.core.filesystem.Folder;
import com.telenav.kivakit.core.kernel.language.progress.reporters.Progress;
import com.telenav.kivakit.core.kernel.messaging.messages.MessageFormatter;
import com.telenav.kivakit.core.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.core.network.http.HttpNetworkLocation;
import com.telenav.kivakit.core.network.http.HttpPostResource;
import com.telenav.kivakit.core.resource.resources.packaged.PackageResource;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;

import static com.telenav.kivakit.core.resource.CopyMode.OVERWRITE;

/**
 * <b>Not public API</b>
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramOverpass.class)
@UmlRelation(label = "copies data to", referent = Folder.class)
class OverpassOsmResource extends BaseRepeater
{
    private final Rectangle bounds;

    private final String template = PackageResource.of(getClass(), "OverpassRequestTemplate.txt")
            .reader()
            .string();

    public OverpassOsmResource(final Rectangle bounds)
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

    public void safeCopyTo(final File destination)
    {
        location(bounds).safeCopyTo(destination, OVERWRITE, Progress.create(this, "bytes"));
    }

    private HttpPostResource location(final Rectangle bounds)
    {
        final var payload = new MessageFormatter().format(template, bounds.toCommaSeparatedString());
        return new HttpNetworkLocation(OverpassDataDownloader.HOST.http().path("/api/interpreter")).post(payload);
    }
}
