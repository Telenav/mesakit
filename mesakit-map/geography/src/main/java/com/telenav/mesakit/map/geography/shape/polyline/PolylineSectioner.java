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

package com.telenav.mesakit.map.geography.shape.polyline;

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.mesakit.map.geography.lexakai.DiagramPolyline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;

@UmlClassDiagram(diagram = DiagramPolyline.class)
public class PolylineSectioner
{
    @UmlAggregation(label = "cuts")
    private final Polyline polyline;

    private final List<Integer> cuts = new ArrayList<>();

    public PolylineSectioner(Polyline polyline)
    {
        this.polyline = polyline;
    }

    public void cutAtIndex(int index)
    {
        checkIndex(index);
        cuts.add(index);
    }

    @UmlRelation(label = "creates")
    public List<PolylineSection> sections()
    {
        List<PolylineSection> sections = new ArrayList<>();

        // Sort cuts so we go through them in polyline order
        Collections.sort(cuts);

        // Loop through cuts
        var last = 0;
        for (int cut : cuts)
        {
            // If we're cutting in a new place (cut indexes could stutter),
            if (last != cut)
            {
                // add a section from the last cut to this cut.
                checkIndex(last);
                checkIndex(cut);
                ensure(last < cut);
                sections.add(polyline.section(last, cut));
            }

            // This cut is the last cut now.
            last = cut;
        }

        // Add the final section between the last cut and the end of the polyline.
        sections.add(polyline.section(last, polyline.size() - 1));

        return sections;
    }

    private void checkIndex(int index)
    {
        ensure(index >= 0);
        ensure(index < polyline.size());
    }
}
