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

package com.telenav.mesakit.map.data.formats.library;

import com.telenav.kivakit.resource.Resource;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.library.internal.lexakai.DiagramDataFormat;

import static com.telenav.kivakit.core.ensure.Ensure.fail;

/**
 * The (binary or text) format of an input {@link Resource}.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramDataFormat.class)
public enum DataFormat
{
    Graph, // MesaKit graph file format
    CSV, // Comma-separated variables format
    Database, // The data came directly from database
    XML, // OSM XML format
    PBF, // OpenStreetMap protobuf format
    ;

    public static DataFormat of(Resource input)
    {
        switch (input.extension().toString())
        {
            case ".graph":
                return Graph;

            case ".pbf":
            case ".osm.pbf":
                return PBF;

            case ".csv":
                return CSV;

            case ".osm":
                return XML;

            default:
                return fail("Data format of '$' is not recognized", input);
        }
    }

    public boolean isCsv()
    {
        return this == CSV;
    }

    public boolean isDatabase()
    {
        return this == Database;
    }

    public boolean isGraph()
    {
        return this == Graph;
    }

    public boolean isOsm()
    {
        return this == XML;
    }

    public boolean isPbf()
    {
        return this == PBF;
    }
}
