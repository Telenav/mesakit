package com.telenav.mesakit.map.data.formats.pbf.model.entities;

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfRelationIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.lexakai.DiagramPbfModelEntities;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import java.util.List;

/**
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfModelEntities.class)
public class PbfRelation extends PbfEntity<Relation>
{
    public PbfRelation(Relation relation)
    {
        super(relation);
    }

    @Override
    public PbfRelationIdentifier identifier()
    {
        return new PbfRelationIdentifier(identifierAsLong());
    }

    public List<RelationMember> members()
    {
        return get().getMembers();
    }
}
