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

package com.telenav.mesakit.map.geography.project;

import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.Precision;
import com.telenav.mesakit.map.geography.indexing.polygon.Leaf;
import com.telenav.mesakit.map.geography.indexing.polygon.LocationArray;
import com.telenav.mesakit.map.geography.indexing.polygon.PolygonSpatialIndex;
import com.telenav.mesakit.map.geography.indexing.polygon.QuadrantStore;
import com.telenav.mesakit.map.geography.indexing.quadtree.QuadTreeSpatialIndex;
import com.telenav.mesakit.map.geography.indexing.rtree.InteriorNode;
import com.telenav.mesakit.map.geography.indexing.rtree.Node;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSettings;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSpatialIndex;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSpatialIndexKryoSerializer;
import com.telenav.mesakit.map.geography.indexing.rtree.UncompressedLeaf;
import com.telenav.mesakit.map.geography.shape.polyline.Polygon;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.polyline.compression.differential.CompressedPolyline;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.kivakit.core.serialization.kryo.KryoTypes;

public class MapGeographyKryoTypes extends KryoTypes
{
    public MapGeographyKryoTypes()
    {
        //----------------------------------------------------------------------------------------------
        // NOTE: To maintain backward compatibility of serialization, registration groups and the types
        // in each registration group must remain in the same order.
        //----------------------------------------------------------------------------------------------

        group("geography", () ->
        {
            register(Precision.class);
            register(Latitude.class);
            register(Longitude.class);
            register(Location.class);
            register(LocationArray.class);
            register(Rectangle.class);
            register(Polygon.class);
            register(Polyline.class);
            register(CompressedPolyline.class);
        });

        group("r-tree-spatial-index", () ->
        {
            register(RTreeSpatialIndex.class);
            register(RTreeSettings.class);
            register(com.telenav.mesakit.map.geography.indexing.rtree.Leaf.class);
            register(Node.class);
        });

        group("quad-tree-spatial-index", () ->
        {
            register(QuadTreeSpatialIndex.class);
            register(QuadTreeSpatialIndex.Quadrant.class);
            register(QuadTreeSpatialIndex.Quadrant[].class);
            register(InteriorNode.class);
            register(UncompressedLeaf.class);
            register(RTreeSpatialIndexKryoSerializer.NodeType.class);
        });

        group("polygon-spatial-index", () ->
        {
            register(PolygonSpatialIndex.class);
            register(Leaf.class);
            register(com.telenav.mesakit.map.geography.indexing.polygon.Node.class);
            register(QuadrantStore.class);
            register(Polygon.SegmentShapeLocationIndex.class);
        });
    }
}
