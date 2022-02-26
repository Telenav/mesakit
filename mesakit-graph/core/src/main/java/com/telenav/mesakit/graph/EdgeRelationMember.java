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

package com.telenav.mesakit.graph;

import com.telenav.kivakit.kernel.language.objects.Hash;
import com.telenav.kivakit.kernel.language.strings.conversion.AsIndentedString;
import com.telenav.kivakit.kernel.language.strings.conversion.AsStringIndenter;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapIdentifier;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.geography.Location;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensure;

public class EdgeRelationMember implements AsIndentedString
{
    /**
     * A map identifier for this relation member, either a {@link PbfWayIdentifier} if the member is a way or a {@link
     * MapNodeIdentifier} if the member is a via node
     */
    private final MapIdentifier identifier;

    /** The relation */
    private final EdgeRelation relation;

    /** The role of this member */
    private final String role;

    public EdgeRelationMember(EdgeRelation relation, MapIdentifier identifier, String role)
    {
        ensure(relation != null);
        ensure(identifier != null);

        this.relation = relation;
        this.identifier = identifier;
        this.role = role;
    }

    @Override
    public AsStringIndenter asString(Format format, AsStringIndenter indenter)
    {
        indenter.add(role().toLowerCase() + " " + type().name().toLowerCase() + " " + identifier());
        return indenter;
    }

    public GraphElement element()
    {
        return relation.graph().elementForMapIdentifier(identifier);
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof EdgeRelationMember)
        {
            var that = (EdgeRelationMember) object;
            return relation.equals(that.relation) && identifier.equals(that.identifier);
        }
        return false;
    }

    public boolean exists()
    {
        if (isNode())
        {
            return vertex() != null;
        }
        if (isWay())
        {
            return route() != null;
        }
        return false;
    }

    public Edge firstEdge()
    {
        var route = route();
        if (route != null)
        {
            return route.first();
        }
        return null;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(relation, identifier);
    }

    public MapIdentifier identifier()
    {
        return identifier;
    }

    public boolean isNode()
    {
        return type() == MapIdentifier.Type.NODE;
    }

    public boolean isRelation()
    {
        return type() == MapIdentifier.Type.RELATION;
    }

    public boolean isWay()
    {
        return type() == MapIdentifier.Type.WAY;
    }

    public Location location()
    {
        var vertex = vertex();
        if (vertex != null)
        {
            return vertex.location();
        }
        return null;
    }

    public EdgeRelation relation()
    {
        return relation;
    }

    public String role()
    {
        return role;
    }

    public Route route()
    {
        var wayIdentifier = (PbfWayIdentifier) identifier;
        var route = relation.graph().routeForWayIdentifier(wayIdentifier.forward());
        if (route != null && wayIdentifier.isReverse())
        {
            route = route.reversed();
        }
        return route;
    }

    @Override
    public String toString()
    {
        return "[EdgeRelationMember identifier = " + identifier() + ", type = " + type() + ", role = " + role() + "]";
    }

    public MapIdentifier.Type type()
    {
        return identifier.type();
    }

    public Vertex vertex()
    {
        return relation.graph().vertexForNodeIdentifier((MapNodeIdentifier) identifier);
    }
}
