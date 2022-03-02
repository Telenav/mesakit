package com.telenav.mesakit.map.data.formats.pbf.model.tags;

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.DiagramPbfModelTags;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

/**
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfModelTags.class)
@UmlRelation(label = "filters", referent = Tag.class)
public interface PbfTagFilter
{
    PbfTagFilter ALL = tag -> true;

    boolean accepts(Tag tag);
}
