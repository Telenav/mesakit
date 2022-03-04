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

package com.telenav.mesakit.graph.project;

import com.telenav.kivakit.core.value.count.Estimate;
import com.telenav.kivakit.core.value.count.Maximum;

public class GraphLimits
{
    public static class Estimated
    {
        // Edges
        public static final Estimate EDGES_PER_WAY = Estimate._2;

        public static final Estimate EDGE_TYPES = Estimate._8;

        public static final Estimate RELATIONS_PER_EDGE = Estimate._1;

        public static final Estimate TMCS_PER_EDGE = Estimate._128;

        public static final Estimate REGION_CODES_PER_EDGE = Estimate._8;

        // Vertexes
        public static final Estimate EDGES_PER_VERTEX = Estimate._4;

        public static final Estimate VERTEXES_PER_EDGE = Estimate._1;

        // Edge Relations
        public static final Estimate EDGES_PER_RELATION = Estimate._4;

        public static final Estimate WAYS_PER_RELATION = Estimate._8;

        public static final Estimate MEMBERS_PER_RELATION = Estimate._4;

        // Edge Lists
        public static final Estimate EDGES_PER_EDGE_LIST = Estimate._8;

        // Routes
        public static final Estimate EDGES_PER_ROUTE = Estimate._32;

        // Places
        public static final Estimate PLACES = Estimate._256;

        // UniDb
        public static final Estimate UNIDB_ADAS_Z_COORDINATES_PER_EDGE = Estimate._4;

        public static final Estimate UNIDB_CURVATURE_HEADING_SLOPE_PER_EDGE = Estimate._4;

        public static final Estimate UNIDB_LANE_ONE_WAYS_PER_EDGE = Estimate._2;

        public static final Estimate UNIDB_LANE_TYPES_PER_EDGE = Estimate._2;

        public static final Estimate UNIDB_LANE_DIVIDERS_PER_EDGE = Estimate._1;
    }

    // The maximum entities in a single graph (not per world graph)
    public static class Limit
    {
        // Nodes
        public static final Maximum NODES = Maximum.maximum(200_000_000);

        // GraphElements
        public static final Maximum GRAPH_ELEMENTS = Maximum.maximum(250_000_000);

        // Edges
        public static final Maximum EDGES = Maximum.maximum(500_000_000);

        public static final Maximum EDGES_PER_WAY = Maximum.maximum(5_000);

        public static final Maximum EDGE_TYPES = Maximum.maximum(10);

        public static final Maximum RELATIONS_PER_EDGE = Maximum.maximum(50);

        public static final Maximum TMCS_PER_EDGE = Maximum.maximum(100);

        public static final Maximum ADAS_Z_COORDINATES_PER_EDGE = Maximum.maximum(10_000);

        public static final Maximum CURVATURE_HEADING_SLOPE_PER_EDGE = Maximum.maximum(10_000);

        public static final Maximum REGION_CODES_PER_EDGE = Maximum.maximum(10);

        // Vertexes
        public static final Maximum VERTEXES = Maximum.maximum(100_000_000);

        public static final Maximum EDGES_PER_VERTEX = Maximum.maximum(32);

        // Edge Relations
        public static final Maximum RELATIONS = Maximum.maximum(100_000_000);

        public static final Maximum EDGES_PER_RELATION = Maximum.maximum(1_000);

        public static final Maximum WAYS_PER_RELATION = Maximum.maximum(10_000);

        // Edge Lists
        public static final Maximum EDGES_PER_EDGE_LIST = Maximum.maximum(1_000);

        // Routes
        public static final Maximum EDGES_PER_ROUTE = Maximum.maximum(20_000);

        // Places
        public static final Maximum PLACES = Maximum.maximum(20_000_000);

        // Shape Points
        public static final Maximum SHAPE_POINTS = Maximum.maximum(1_000_000_000);

        // Crosses
        public static final Maximum EDGES_PER_CROSS = Maximum.maximum(200);

        public static final Maximum VERTEXES_PER_CROSS = Maximum.maximum(12);

        // Intersections
        public static final Maximum TURNS_PER_INTERSECTION = Maximum.maximum(40);
    }
}
