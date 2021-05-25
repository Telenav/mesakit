package com.telenav.mesakit.map.data.formats.pbf.model.entities;

import com.telenav.kivakit.kernel.language.collections.list.StringList;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfModelEntities;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfModelEntities.class)
public class PbfWay extends PbfEntity<Way>
{
    public PbfWay(final Way way)
    {
        super(way);
    }

    public String highway()
    {
        return tagValue("highway");
    }

    public StringList highways()
    {
        return tagValueSplit("highway");
    }

    @Override
    public PbfWayIdentifier identifier()
    {
        return new PbfWayIdentifier(identifierAsLong());
    }

    public List<WayNode> nodes()
    {
        return get().getWayNodes();
    }

    public PbfWay withTags(final Collection<Tag> tags)
    {
        final var way = get();

        if (way == null)
        {
            final var common = new CommonEntityData(1L, 1, new Date(), new OsmUser(0, "shibo"), 1L, tags);
            return new PbfWay(new Way(common, Collections.emptyList()));
        }
        else
        {
            final var common = new CommonEntityData(
                    way.getId(),
                    way.getVersion(),
                    way.getTimestamp(),
                    way.getUser(),
                    way.getChangesetId(),
                    tags);
            return new PbfWay(new Way(common, way.getWayNodes()));
        }
    }
}
