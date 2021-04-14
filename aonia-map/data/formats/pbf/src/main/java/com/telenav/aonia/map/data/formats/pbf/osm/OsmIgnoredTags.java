package com.telenav.aonia.map.data.formats.pbf.osm;

import com.telenav.aonia.map.data.formats.pbf.model.tags.PbfTagFilter;
import com.telenav.aonia.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfOsm;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.HashSet;

/**
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfOsm.class)
@UmlExcludeSuperTypes(HashSet.class)
public class OsmIgnoredTags extends HashSet<String> implements PbfTagFilter
{
    public OsmIgnoredTags()
    {
        add("attribution");
        add("created_by");
        add("import_uuid");
        add("length");
        add("length_unit");
        add("llid");
        add("waterway:llid");
        add("geobase:datasetName");
        add("geobase:uuid");
        add("KSJ2:ADS");
        add("KSJ2:ARE");
        add("KSJ2:AdminArea");
        add("KSJ2:COP_label");
        add("KSJ2:DFD");
        add("KSJ2:INT");
        add("KSJ2:INT_label");
        add("KSJ2:LOC");
        add("KSJ2:LPN");
        add("KSJ2:OPC");
        add("KSJ2:PubFacAdmin");
        add("KSJ2:RAC");
        add("KSJ2:RAC_label");
        add("KSJ2:RIC");
        add("KSJ2:RIN");
        add("KSJ2:WSC");
        add("KSJ2:coordinate");
        add("KSJ2:curve_id");
        add("KSJ2:curve_type");
        add("KSJ2:filename");
        add("KSJ2:lake_id");
        add("KSJ2:lat");
        add("KSJ2:long");
        add("KSJ2:river_id");
        add("odbl");
        add("odbl:note");
        add("SK53_bulk:load");
        add("source");
        add("sub_sea:type");
        add("tiger:source");
        add("tiger:separated");
        add("tiger:tlid");
        add("tiger:upload_uuid");
        add("yh:LINE_NAME");
        add("yh:LINE_NUM");
        add("yh:STRUCTURE");
        add("yh:TOTYUMONO");
        add("yh:TYPE");
        add("yh:WIDTH_RANK");
    }

    @Override
    public boolean accepts(final Tag value)
    {
        final var key = value.getKey();
        if (key.startsWith("tiger:") || key.startsWith("gnis:"))
        {
            return true;
        }
        return contains(key);
    }
}
