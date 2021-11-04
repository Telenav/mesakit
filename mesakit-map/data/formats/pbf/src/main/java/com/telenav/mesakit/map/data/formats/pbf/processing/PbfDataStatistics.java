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

package com.telenav.mesakit.map.data.formats.pbf.processing;

import com.telenav.kivakit.kernel.language.values.count.Bytes;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.resource.Resource;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfProcessing;

/**
 * A simple container for statistics accumulated while processing data from a {@link PbfDataSource}
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfProcessing.class)
public final class PbfDataStatistics
{
    private long nodes;

    private long ways;

    private long relations;

    private final Bytes dataSize;

    public PbfDataStatistics(Resource resource)
    {
        dataSize = resource.sizeInBytes();
    }

    public Bytes dataSize()
    {
        return dataSize;
    }

    public void incrementNodes()
    {
        nodes++;
    }

    public void incrementRelations()
    {
        relations++;
    }

    public void incrementWays()
    {
        ways++;
    }

    public boolean isValid()
    {
        return nodes().isGreaterThan(Count._2) && ways().isGreaterThan(Count._0);
    }

    public Count nodes()
    {
        return Count.count(nodes);
    }

    public Count relations()
    {
        return Count.count(relations);
    }

    public Count ways()
    {
        return Count.count(ways);
    }
}
