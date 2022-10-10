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

import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.language.primitive.Longs;
import com.telenav.kivakit.core.language.reflection.property.KivaKitExcludeProperty;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.string.CaseFormat;
import com.telenav.kivakit.core.string.Differences;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.core.time.Duration;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.primitive.collections.LongKeyed;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.kivakit.interfaces.string.StringFormattable;
import com.telenav.kivakit.primitive.collections.map.PrimitiveMap;
import com.telenav.kivakit.validation.ValidationType;
import com.telenav.kivakit.validation.Validator;
import com.telenav.mesakit.graph.analytics.classification.classifiers.turn.TurnType;
import com.telenav.mesakit.graph.analytics.classification.classifiers.turn.TwoHeadingTurnClassifier;
import com.telenav.mesakit.graph.collections.EdgePair;
import com.telenav.mesakit.graph.collections.EdgeSequence;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.identifiers.VertexIdentifier;
import com.telenav.mesakit.graph.io.load.GraphLoader;
import com.telenav.mesakit.graph.map.MapEdgeIdentifier;
import com.telenav.mesakit.graph.map.MapWay;
import com.telenav.mesakit.graph.matching.conflation.EdgeConflater;
import com.telenav.mesakit.graph.matching.snapping.EdgeSnapper;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.navigation.Navigator;
import com.telenav.mesakit.graph.navigation.RouteLimiter;
import com.telenav.mesakit.graph.navigation.limiters.EdgeCountRouteLimiter;
import com.telenav.mesakit.graph.navigation.limiters.LengthRouteLimiter;
import com.telenav.mesakit.graph.navigation.navigators.WayNavigator;
import com.telenav.mesakit.graph.specifications.common.edge.EdgeAttributes;
import com.telenav.mesakit.graph.specifications.common.edge.EdgeProperties;
import com.telenav.mesakit.graph.specifications.common.edge.HeavyWeightEdge;
import com.telenav.mesakit.graph.specifications.common.edge.store.EdgeStore;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementAttributes;
import com.telenav.mesakit.graph.specifications.library.attributes.Attribute;
import com.telenav.mesakit.graph.specifications.library.properties.GraphElementProperty;
import com.telenav.mesakit.graph.specifications.library.properties.GraphElementPropertySet;
import com.telenav.mesakit.graph.specifications.osm.OsmDataSpecification;
import com.telenav.mesakit.graph.specifications.osm.graph.loader.sectioner.EdgeSection;
import com.telenav.mesakit.graph.specifications.osm.graph.loader.sectioner.EdgeSectioner;
import com.telenav.mesakit.graph.specifications.osm.graph.loader.sectioner.WaySectioningGraphLoader;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapIdentifier;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagMap;
import com.telenav.mesakit.map.geography.Located;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.LocationSequence;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.polyline.PolylineSnapper;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Heading;
import com.telenav.mesakit.map.measurements.motion.Speed;
import com.telenav.mesakit.map.region.locale.MapLocale;
import com.telenav.mesakit.map.region.regions.Continent;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.County;
import com.telenav.mesakit.map.region.regions.MetropolitanArea;
import com.telenav.mesakit.map.region.regions.State;
import com.telenav.mesakit.map.road.model.BridgeType;
import com.telenav.mesakit.map.road.model.GradeSeparation;
import com.telenav.mesakit.map.road.model.RoadFunctionalClass;
import com.telenav.mesakit.map.road.model.RoadName;
import com.telenav.mesakit.map.road.model.RoadState;
import com.telenav.mesakit.map.road.model.RoadSubType;
import com.telenav.mesakit.map.road.model.RoadSurface;
import com.telenav.mesakit.map.road.model.RoadType;
import com.telenav.mesakit.map.road.model.SpeedCategory;
import com.telenav.mesakit.map.road.name.standardizer.RoadNameStandardizer;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.telenav.kivakit.core.ensure.Ensure.unsupported;
import static com.telenav.kivakit.core.messaging.Listener.throwingListener;

/**
 * An edge in a {@link Graph} connecting two {@link Vertex}es. In the case of PBF graphs, edges are sections of ways
 * between intersections and other features (see {@link EdgeSectioner} and {@link WaySectioningGraphLoader} for details
 * on how PBF ways are sectioned by the PBF loader).
 * <p>
 * An {@link Edge} is "flyweight" object, having only three fields: a {@link Graph} reference, an {@link EdgeIdentifier}
 * and an index value that is used to efficiently retrieve {@link EdgeAttributes} from an {@link EdgeStore}. This design
 * makes edges very cheap to construct, allowing a truly object-oriented style of programming.
 * <p>
 * The {@link Edge} class is central to the Graph API and has a large number of methods. While this can seem
 * overwhelming, the functionality breaks down into categories that are easier to digest:
 * <p>
 * <b>Categories of Functionality</b>
 * <ul>
 *     <li><i>Graph</i> - Find edges in a graph</li>
 *     <li><i>Identity</i> - Edge identifiers</li>
 *     <li><i>Attributes</i> - Discover edge attributes and properties</li>
 *     <li><i>Edges</i> - Find related edges</li>
 *     <li><i>Routes</i> - Construct routes from edges</li>
 *     <li><i>Vertexes, Nodes and ShapePoints</i> - Edge end-points and the nodes in-between</li>
 *     <li><i>Metrics</i> - Edge measurements</li>
 *     <li><i>Geometry</i> - The shape of edges</li>
 *     <li><i>Spatial Search</i> - Locate edges with spatial searches</li>
 *     <li><i>Metadata</i> - Metadata associated with all edges</li>
 *     <li><i>Tags</i> - Key / value pairs associated with edges</li>
 *     <li><i>Road Classification</i> - Determine the type of an edge</li>
 *     <li><i>Road PropertyMap</i> - Attributes of the road that an edge belongs to</li>
 *     <li><i>Turns</i> - Information regarding different types of turns between edges</li>
 *     <li><i>Traffic</i> - Traffic information identifiers for an edge</li>
 *     <li><i>Region and Locale</i> - Administrative areas and locale information</li>
 *     <li><i>Speed and Travel Time</i> - Historical and free-flow travel information</li>
 *     <li><i>Testing and Debugging</i> - Methods helpful in testing and debugging</li>
 *     <li><i>OSM-Specific</i> - Methods that are specific to the {@link OsmDataSpecification} (for convenience)</li>
 * </ul>
 * <p>
 * A few methods that have internal uses have been omitted. Some methods also show up under more than one category to
 * make it easier to find functionality.
 * <p>
 * <b>Graph</b>
 * <ul>
 *     <li>{@link #graph()} - The {@link Graph} that this edge belongs to</li>
 *     <li>{@link Graph#edgeCount()} - The number of edges in a graph</li>
 *     <li>{@link Graph#edgeNearest(Location)} - The edge nearest to the given location</li>
 *     <li>{@link Graph#edgeNearest(Location, Distance)} - The edge nearest to the given location, within the given maximum distance</li>
 *     <li>{@link Graph#edgeNearest(Location, Distance, TransportMode)} - The edge navigable in the given {@link TransportMode} that is nearest to the given location, within the given maximum distance </li>
 *     <li>{@link Graph#edgeForIdentifier(EdgeIdentifier)} - The edge for the given identifier</li>
 *     <li>{@link Graph#edgeForIdentifier(long)} - The edge for the given identifier</li>
 *     <li>{@link Graph#edgeForIdentifier(MapEdgeIdentifier)} - The edge corresponding to the given {@link MapEdgeIdentifier} of the form "[way-identifier]:[from-node-identifier]:[to-node-identifier]"</li>
 *     <li>{@link Graph#edges()} - The sequence of all edges in a graph</li>
 *     <li>{@link Graph#edgesIntersecting(Rectangle)} - The sequence of all edges in the graph that intersect (by definition of {@link Intersectable#intersects(Rectangle)}) the given rectangle</li>
 * </ul>
 * <p>
 * <b>Identity</b>
 * <ul>
 *     <li>{@link #identifier()} - The {@link EdgeIdentifier} for this edge</li>
 *     <li>{@link #identifierAsLong()} - The edge identifier as a long rather than as an object</li>
 *     <li>{@link #wayIdentifier()} - The {@link PbfWayIdentifier} for the way that this edge came from</li>
 *     <li>{@link #mapEdgeIdentifier()} - An identifier for the edge in terms of {@link MapIdentifier}s taking the form "[way-identifier]:[from-node-identifier]:[to-node-identifier]"</li>
 *     <li>{@link #directionalWayIdentifier()} - The {@link PbfWayIdentifier} for this edge, but negative if the edge is reversed</li>
 * </ul>
 * <p>
 * <b>Attributes</b>
 * <ul>
 *     <li>{@link #attributes()} - The {@link EdgeAttributes} this edge supports according to its {@link DataSpecification}</li>
 *     <li>{@link #supports(Attribute)} - True if this edge supports the given {@link Attribute}</li>
 *     <li>{@link #differencesFrom(Edge)} - Determines the differences between the attributes of this edge and the given edge</li>
 *     <li>{@link #properties()} - A list of each display {@link GraphElementProperty} this edge supports according to its {@link DataSpecification}</li>
 * </ul>
 * <p>
 * <b>Edges</b>
 * <ul>
 *     <li>{@link #relations()} - The set of relations that reference this edge</li>
 *     <li>{@link #forward()} - The forward edge for this edge, having a non-negative identifier</li>
 *     <li>{@link #isForward()} - True if this is a forward edge with a positive identifier</li>
 *     <li>{@link #isForwardOrReverseOf(Edge)} - True if this is the forward or reverse edge of the given edge</li>
 *     <li>{@link #reverse()} - The reverse edge corresponding to this edge (whether forward or not) and having a non-negative identifier</li>
 *     <li>{@link #reversed()} - This edge, reversed, if it is a two-way road. A forward edge becomes a reverse edge and a reverse edge a forward edge.</li>
 *     <li>{@link #isReverse()} - True if this is a reverse edge (on a two-way road) with a negative identifier</li>
 *     <li>{@link #next()} - The edge that this edge leads to. If there is more than one, an arbitrary next edge is returned</li>
 *     <li>{@link #previous()} - The edge that this edge comes from. If there is more than one, an arbitrary previous edge is returned</li>
 *     <li>{@link #toEdgesWithoutThisEdge()} - Set of ALL edges connected to the "to" vertex of this edge, but not including this edge or its reverse</li>
 *     <li>{@link #fromEdgesWithoutThisEdge()} - Set of ALL edges connected to the "from" vertex of this edge, but not including this edge or its reverse</li>
 *     <li>{@link #inEdgeSequence()} - Decoded of edges inbound to the "from" vertex of this edge</li>
 *     <li>{@link #inEdges()} - Set of edges inbound to the "from" vertex of this edge</li>
 *     <li>{@link #inEdgesWithoutReversed()} - Set of edges inbound to the "from" vertex of this edge, not including the reverse of this edge if it is two-way</li>
 *     <li>{@link #outEdgeSequence()} - Decoded of edges outbound to the "to" vertex of this edge</li>
 *     <li>{@link #outEdges()} - Set of edges outbound to the "to" vertex of this edge</li>
 *     <li>{@link #outEdgesWithoutReversed()} - Set of edges outbound to the "to" vertex of this edge, not including the reverse of this edge if it is two-way</li>
 *     <li>{@link #nearbyEdges(Distance, Matcher)} ()} - Edges within the given distance of this edge that match the given matcher</li>
 *     <li>{@link #leadsTo(Edge)} - True if this edge leads to the given edge</li>
 *     <li>{@link #isConnectedTo(Edge)} - True if this edge is connected to the given edge</li>
 *     <li>{@link #isConnectedTo(Vertex)} - True if this edge is connected to the given vertex</li>
 *     <li>{@link #connectedEdges()} - The set of all edges connected to this edge, not including this edge</li>
 *     <li>{@link #connectedEdgesWithoutReversed()} - The set of all edges connected to this edge, not including this edge or its reverse if it is a two-way road</li>
 *     <li>{@link #otherEdges(Vertex)} - All the edges other than this one that are connected to this edge at the given vertex</li>
 *     <li>{@link #mapWay()} - This edge as a way in the map data</li>
 * </ul>
 * <p>
 * <b>Routes</b>
 * <ul>
 *     <li>{@link Edge#intersecting(Rectangle)} - A matcher for edges that intersect the given bounds</li>
 *     <li>{@link Edge#within(Rectangle)} - A matcher for edges within the given bounds</li>
 *     <li>{@link #asRoute()} - This edge as a one-edge route</li>
 *     <li>{@link #route(Navigator, Maximum)} - The route from this edge discovered by the given {@link Navigator} up to the given maximum number of edges</li>
 *     <li>{@link #route(Navigator, Distance)} - The route from this edge discovered by the given {@link Navigator} up to the given maximum length</li>
 *     <li>{@link #route(Navigator, RouteLimiter)} - The route from this edge discovered by the given {@link Navigator}, restricted by the given {@link RouteLimiter}</li>
 *     <li>{@link #route(Matcher)} - The route from this edge of edges matching the given matcher</li>
 *     <li>{@link #leadsTo(Route)} - True if this edge leads to the given route</li>
 *     <li>{@link #inRoute(Navigator, RouteLimiter)} - An inbound route as discovered by a navigator within the limits of the route limiter</li>
 *     <li>{@link #outRoute(Navigator, RouteLimiter)} - An outbound route as discovered by a navigator within the limits of the route limiter</li>
 *     <li>{@link #nonBranchingRoute(Maximum)} - A route from this edge with no more than the given number of edges, which does not branch</li>
 *     <li>{@link #nonBranchingRouteWithSameName(Maximum)} - A non-branching route from this edge limited to edges with the same base road name</li>
 *     <li>{@link #wayAsRoute()} - The way that this edge belongs to as a route</li>
 *     <li>{@link #vertexConnecting(Edge)} - The vertex that connects this edge to the given edge, or null if none exists</li>
 * </ul>
 * <p>
 * <b>Vertexes, Nodes and ShapePoints</b>
 * <ul>
 *     <li>{@link #from()} -The "from" vertex of the edge</li>
 *     <li>{@link #to()} - The "to" vertex of the edge</li>
 *     <li>{@link #fromLocation()} - The location of the "from" vertex of the edge</li>
 *     <li>{@link #toLocation()} - The location of the "to" vertex of the edge</li>
 *     <li>{@link #fromNodeIdentifier()} - The {@link MapNodeIdentifier} of the "from" end of the edge</li>
 *     <li>{@link #toNodeIdentifier()} - The {@link MapNodeIdentifier} of the "to" end of the edge</li>
 *     <li>{@link #fromVertexIdentifier()} - The {@link VertexIdentifier} of the "from" end of the edge</li>
 *     <li>{@link #toVertexIdentifier()} - The {@link VertexIdentifier} of the "to" end of the edge</li>
 *     <li>{@link #locationSequence()} - The locations from the beginning of this edges road shape {@link Polyline} to the end</li>
 *     <li>{@link #oppositeVertex(Vertex)} - The opposite vertex to the given vertex on this edge</li>
 *     <li>{@link #shapePoints()} - The edge's shape points, if it #supportsFullPbfNodeInformation</li>
 *     <li>{@link #shapePointsWithoutVertexes()} - The edge's shape points without vertexes, if it #supportsFullPbfNodeInformation</li>
 *     <li>{@link #shapePointNodeIdentifiers()} - The edge's node identifiers, if it #supportsFullPbfNodeInformation</li>
 *     <li>{@link #supportsNodeIdentifiers()} - True if the edge is in a graph supporting full node information. This is very space inefficient and is generally only used by tools to improve the map, like the map enhancer (Cygnus)</li>
 * </ul>
 * <p>
 * <b>Metrics</b>
 * <ul>
 *     <li>{@link #bounds()} - The bounding area of the edge, based on its road shape, not its endpoints</li>
 *     <li>{@link #distanceTo(Edge)} - The shortest distance to the given edge</li>
 *     <li>{@link #length()} - The length of this edge along its road shape {@link Polyline}</li>
 *     <li>{@link #lengthInMillimeters()} - The edge length in millimeters</li>
 *     <li>{@link #turnAngleTo(Edge, Chirality)} - The turn angle from this edge to the given edge using the given {@link Chirality}</li>
 * </ul>
 * <p>
 * <b>Geometry</b>
 * <ul>
 *     <li>{@link #asSegment()} - This edge as a segment from the "from" vertex to the "to" vertex, ignoring any road shape</li>
 *     <li>{@link #finalHeading()} - The heading of the last segment of this edge's road shape</li>
 *     <li>{@link #initialHeading()} - The heading of the first segment of this edge's road shape</li>
 *     <li>{@link #heading()} - Same as {@link #finalHeading()}</li>
 *     <li>{@link #roadShape()} - The shape of this edge as a {@link Polyline}</li>
 *     <li>{@link #isShaped()} - True if this edge has a polyline road shape</li>
 *     <li>{@link #isSegment()} - True if this edge is just a single segment and does not have a polyline road shape</li>
 *     <li>{@link #turnAngleTo(Edge, Chirality)} - The turn angle from this edge to the given edge using the given {@link Chirality}</li>
 *     <li>{@link #rightTurnAngleTo(Edge)} - The clockwise (right) turn angle from this edge to the given edge</li>
 *     <li>{@link #leftTurnAngleTo(Edge)} - The counter-clockwise (left) angle from this edge to the given edge</li>
 *     <li>{@link #straightOnTurnAngleTo(Edge)} - The smallest turn angle from this edge to the given edge</li>
 *     <li>{@link #firstSegment()} - The first segment in this edge's road shape</li>
 *     <li>{@link #lastSegment()} - The last segment in this edge's road shape</li>
 *     <li>{@link #intersects(Rectangle)} - True if this edge's road shape intersects the given rectangle</li>
 *     <li>{@link #endNearestTo(Located)} - The end of this edge nearest to the given location</li>
 *     <li>{@link #isParallelTo(Edge)} - True if this edge is parallel to the given edge</li>
 *     <li>{@link #isPerpendicularTo(Edge)} - True if this edge is perpendicular to the given edge</li>
 *     <li>{@link #crosses(Edge)} - True if this edge crosses the given edge</li>
 * </ul>
 * <p>
 * <b>Spatial Search</b>
 * <ul>
 *     <li>{@link Graph#edgesIntersecting(Rectangle)} - The sequence of all edges in the graph that intersect (by definition of {@link Intersectable#intersects(Rectangle)}) the given rectangle</li>
 *     <li>{@link Graph#edgeNearest(Location)} - The edge nearest to the given location</li>
 *     <li>{@link Graph#edgeNearest(Location, Distance)} - The edge nearest to the given location, within the given maximum distance</li>
 *     <li>{@link Graph#edgeNearest(Location, Distance, TransportMode)} - The edge navigable in the given {@link TransportMode} that is nearest to the given location, within the given maximum distance </li>
 *     <li>{@link #intersects(Rectangle)} - True if this edge's road shape intersects the given rectangle</li>
 *     <li>{@link #endNearestTo(Located)} - The end of this edge nearest to the given location</li>
 *     <li>{@link #isInside(Rectangle)} - True if this edge is inside the given rectangle</li>
 * </ul>
 * <p>
 * <b>Metadata</b>
 * <ul>
 *     <li>{@link #metadata()} - The {@link Metadata} of the {@link Graph} that this edge belongs to</li>
 *     <li>{@link #dataSpecification()} - The {@link DataSpecification} for this edge</li>
 *     <li>{@link #lastModificationTime()} - The last time the way that this edge belongs to was changed</li>
 *     <li>{@link #pbfUserName()} - The user who most recently changed this edge's way</li>
 *     <li>{@link #pbfUserIdentifier()} - The identifier of the user who most recently changed this edge's way</li>
 *     <li>{@link #pbfChangeSetIdentifier()} - The most recent change set identifier of the way that this edge belongs to</li>
 *     <li>{@link #pbfRevisionNumber()} - The current revision number of the way that this edge belongs to</li>
 * </ul>
 * <p>
 * <b>Tags</b>
 * <ul>
 *     <li>{@link #hasTag(String)} - True if this edge has a tag with the given key</li>
 *     <li>{@link #tag(String)} - The {@link Tag} for the given key</li>
 *     <li>{@link #tagMap()} - A {@link PbfTagMap} of the tags that belong to this edge</li>
 *     <li>{@link #tagList()} - A {@link PbfTagList} of the tags that belong to this edge</li>
 *     <li>{@link #tagValue(String)} - The value of any tag with the given key or null if there is no such tag</li>
 * </ul>
 * <p>
 * <b>Road Classification</b>
 * <ul>
 *     <li>{@link #isLink()} - True if this edge is a link</li>
 *     <li>{@link #isIntersectionEdge()} - True if this edge is in an intersection</li>
 *     <li>{@link #isJunctionEdge()} - True if this edge is junction edge in an intersection</li>
 *     <li>{@link #isConnector()} - True if this edge connects roadways</li>
 *     <li>{@link #leadsToDeadEnd()} - True if this edge does not lead anywhere (except turning around)</li>
 *     <li>{@link #isJoiningEdge()} - True if this edge merges with another edge</li>
 *     <li>{@link #isClipped()} - True if this edge was clean-cut as part of a WorldGraph</li>
 *     <li>{@link #isMainRoad()} - True if this edge is considered a main road</li>
 *     <li>{@link #isNavigable(TransportMode)} - True if this edge is navigable in the given transport mode: car, foot, bike</li>
 *     <li>{@link #isOffRamp()} - True if this edge is a freeway off ramp</li>
 *     <li>{@link #isOnRamp()} - True if this edge is a freeway on ramp</li>
 *     <li>{@link #isLink()} - True if this edge is a link from one road to another</li>
 *     <li>{@link #isDrivable()} - True if this edge supports {@link TransportMode#DRIVE}</li>
 *     <li>{@link #isSoftCut()} - True if this edge was soft-cut</li>
 *     <li>{@link #isUnderConstruction()} - True if this edge is under construction</li>
 *     <li>{@link #isTwoWay()} - True if this edge has a reverse edge</li>
 *     <li>{@link #isTollRoad()} - True if this edge is part of a toll road</li>
 *     <li>{@link #leadsToFork()} - True if the "to" vertex of this edge forks to two or more edges</li>
 *     <li>{@link #isLoop()} - True if this edge connects back to itself in a loop</li>
 *     <li>{@link #isRoundabout()} - True if this edge is part of a roundabout</li>
 *     <li>{@link #isOneWay()} - True if this edge has no reverse edge</li>
 *     <li>{@link #isRamp()} - True if this edge is part of a freeway ramp</li>
 *     <li>{@link #isClosedToThroughTraffic()} - True if this edge is closed</li>
 *     <li>{@link #entersRoundabout()} - True if this edge enters a roundabout</li>
 *     <li>{@link #isNormal()} - True if this edge has RoadType#NORMAL</li>
 *     <li>{@link #isRoutingShortCut()} - True if this edge has RoadType#ROUTING_SHORTCUT</li>
 * </ul>
 * <p>
 * <b>Road PropertyMap</b>
 * <ul>
 *     <li>{@link #hasRoadName()} - True if this edge has at least one name</li>
 *     <li>{@link #isNamed()} - True if this road has a name</li>
 *     <li>{@link #isNameless()} - True if this road has no name</li>
 *     <li>{@link #roadName()} - The most important name of this road</li>
 *     <li>{@link #roadName(RoadName.Type)} - The name for this road of the given type</li>
 *     <li>{@link #roadNames()} - All the names of this road</li>
 *     <li>{@link #roadNames(RoadName.Type)} - All the names of the given type for this road</li>
 *     <li>{@link #hasSameRoadNameAs(Edge)} - True if this edge has the same road name as the given edge</li>
 *     <li>{@link #isOnSameRoadWithSameTypeAs(Edge)} - True if this edge has the same road name as the given edge and has the same road type and sub-type</li>
 *     <li>{@link #isOnSameRoadAs(Edge)} - True if this edge is on the same road as the given edge, as determined by the road names</li>
 *     <li>{@link #safeRoadName()} - A name for this edge that is never null</li>
 *     <li>{@link #displayRoadName()} - A name for this edge that is suitable for display to an end-user</li>
 *     <li>{@link #hasSameStandardizedBaseNameAs(Edge)} - True if this edge has the same base name as the given edge</li>
 *     <li>{@link #roadFunctionalClass()} - The {@link RoadFunctionalClass} of this edge</li>
 *     <li>{@link #isSameFunctionalClassAs(Edge)} - True if this edge has the same functional class as the given edge</li>
 *     <li>{@link #isMoreImportantThan(Edge)} - True if this edge has a higher functional class than the given edge. If the classes are the same, the road type is used to break the tie</li>
 *     <li>{@link #roadShape()} - The {@link Polyline} shape of this edge</li>
 *     <li>{@link #roadState()} - The state of this edge: one-way, two-way or closed</li>
 *     <li>{@link #roadType()} - The type of this edge: freeway, highway, local road, etc.</li>
 *     <li>{@link #roadSubType()} - The sub-type of this edge: main road, intersection link, ramp, roundabout, etc.</li>
 *     <li>{@link #roadSurface()} - The surface of this edge: paved, unpaved or poor condition</li>
 *     <li>{@link #laneCount()} - The number of lanes on this edge</li>
 *     <li>{@link #hovLaneCount()} - The number of HOV lanes on this edge</li>
 *     <li>{@link #isShaped()} - True if this edge has a road shape with more than one segment</li>
 *     <li>{@link #bridgeType()} - The type of bridge for this edge: none, tunnel, overpass or underpass</li>
 *     <li>{@link #isOnSameWay(Edge)} - True if this edge part of the same way as the given edge</li>
 *     <li>{@link #signPostSupport()} - The support for sign posts on this edge</li>
 * </ul>
 * <p>
 * <b>Turns</b>
 * <ul>
 *     <li>{@link #turnAngleTo(Edge, Chirality)}</li>
 *     <li>{@link #turnRestrictions()}</li>
 *     <li>{@link #turnType(Edge)}</li>
 *     <li>{@link #turnType(Edge, TwoHeadingTurnClassifier)}</li>
 *     <li>{@link #hardestRight(Angle)} - The hardest right-turn edge from this edge within the given tolerance</li>
 *     <li>{@link #hardestLeft(Angle)} - The hardest left-turn edge from this edge within the given tolerance</li>
 *     <li>{@link #mostStraightOn(Angle)} - The most straight-on edge from this edge within the given tolerance</li>
 *     <li>{@link #rightTurnAngleTo(Edge)} - The right-turn angle from the last segment of this edge to the first segment of the given edge</li>
 *     <li>{@link #leftTurnAngleTo(Edge)} - The left-turn angle from the last segment of this edge to the first segment of the given edge</li>
 *     <li>{@link #straightOnTurnAngleTo(Edge)} - The smallest angle from the last segment of this edge to the first segment of the given edge</li>
 * </ul>
 * <p>
 * <b>Speed and Travel Time</b>
 * <ul>
 *     <li>{@link #speedLimit()} - The speed limit on this edge</li>
 *     <li>{@link #freeFlowSpeed()} - The speed of normal traffic flow on this edge</li>
 *     <li>{@link #freeFlowForFunctionalClass()} - Typical speed of traffic on edges of this functional class</li>
 *     <li>{@link #travelTime()} - The amount of time required to travel this edge at free flow speed</li>
 *     <li>{@link #travelTime(Speed)} - The time required to travel this edge at the given speed</li>
 *     <li>{@link #travelTimeForFunctionalClass()} - The time required to travel this edge based on the typical speed on this edge's functional class</li>
 *     <li>{@link #travelTimeInMilliseconds()} - The travel time for this edge at free flow speed in milliseconds</li>
 * </ul>
 * <p>
 * <b>Region and Locale</b>
 * <ul>
 *     <li>{@link #continent()} - The continent where this edge exists</li>
 *     <li>{@link #country()} - The country that owns this edge</li>
 *     <li>{@link #state()} - The state where this edge exists</li>
 *     <li>{@link #metropolitanArea()} - The most important metropolitan area that owns this edge</li>
 *     <li>{@link #locale()} - The {@link MapLocale} for this edge (which is distinct from java.util.{@link Locale}</li>
 * </ul>
 * <p>
 * <b>Testing and Debugging</b>
 * <ul>
 *     <li>{@link #asHeavyWeight()} - This edge as a {@link HeavyWeightEdge}</li>
 *     <li>{@link #isHeavyWeight()} - True if this edge is a {@link HeavyWeightEdge}</li>
 *     <li>{@link #populateWithTestValues()} - Populates this {@link HeavyWeightEdge} with test values</li>
 *     <li>{@link #asString()} - A string representation of the {@link EdgeProperties} of this edge accessible through {@link StringFormattable#asString()}</li>
 * </ul>
 * <p>
 * <b>OSM-Specific</b>
 * <ul>
 *     <li>{@link #isOsm()} - True if this is an OSM edge</li>
 *     <li>{@link #osmIsLink()} - True if this edge is an OSM link</li>
 *     <li>{@link #osmIsOneWay()} - True if this edge has been labeled as an OSM one-way</li>
 *     <li>{@link #osmIsRoundabout()} - True if this edge is on a roundabout based on OSM tags</li>
 *     <li>{@link #osmIsServiceWay()} - True if this edge is an OSM service way</li>
 *     <li>{@link #osmIsExitToTagged()} - True if this edge is tagged with OSM exit-to</li>
 *     <li>{@link #osmIsDestinationTagged()} - True this edge has an OSM destination tag</li>
 *     <li>{@link #osmIsMotorwayJunctionReferenceTagged()} - True this edge has an OSM junction reference tag</li>
 *     <li>{@link #osmIsDoubleDigitized()} - True this edge has been detected in OSM as being double-digitized.</li>
 *     <li>{@link #osmMaximumDoubleDigitizationSeparation()} - The maximum separation of detected OSM double-digitized ways</li>
 *     <li>{@link #fromGradeSeparation()} - The {@link GradeSeparation} for the "from" end of this edge</li>
 *     <li>{@link #toGradeSeparation()} - The {@link GradeSeparation} for the "to" end of this edge </li>
 *     <li>{@link #osmCouldBeDoubleDigitized()} - True if this edge could be detected as OSM double-digitized</li>
 * </ul>
 *
 * @author jonathanl (shibo)
 * @see EdgeIdentifier
 * @see EdgeSequence
 * @see EdgeSet
 * @see EdgeRelation
 * @see EdgeAttributes
 * @see HeavyWeightEdge
 * @see EdgePair
 * @see EdgeProperties
 * @see EdgeConflater
 * @see EdgeSnapper
 * @see EdgeSectioner
 * @see EdgeSection
 * @see PbfWayIdentifier
 * @see Graph
 * @see Vertex
 * @see Route
 * @see Distance
 * @see Speed
 * @see Angle
 */
@SuppressWarnings({ "DuplicatedCode", "SpellCheckingInspection", "unused" })
public abstract class Edge extends GraphElement implements
        Bounded,
        Intersectable,
        Road,
        LocationSequence
{
    /**
     * Default tolerance for parallel edges
     */
    public static final Angle PARALLEL_TOLERANCE = Angle.degrees(45);

    /**
     * Default tolerance for perpendicular edges
     */
    public static final Angle PERPENDICULAR_TOLERANCE = Angle.degrees(30);

    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(Edge.LOGGER);

    /**
     * Returns a matcher for edges intersecting the given bounds
     */
    public static Matcher<Edge> intersecting(Rectangle bounds)
    {
        return edge -> edge.intersects(bounds);
    }

    /**
     * Returns a matcher for edges inside the given bounds
     */
    public static Matcher<Edge> within(Rectangle bounds)
    {
        return edge -> edge.isInside(bounds);
    }

    public enum SignPostSupport
    {
        MOTORWAY_JUNCTION_REFERENCE_TAGGED,
        EXIT_TO_TAGGED,
        DESTINATION_TAGGED
    }

    /**
     * Different ways of getting around
     *
     * @author jonathanl (shibo)
     */
    public enum TransportMode
    {
        ANY,
        DRIVE,
        WALK,
        BIKE
    }

    /**
     * The type of edge, of which there are two types: {@link Type#NORMAL} and {@link Type#ROUTING_SHORTCUT}.
     * <p>
     * Outside certain special navigation applications, all edges are of type {@link Type#NORMAL}.
     *
     * @author jonathanl (shibo)
     */
    public enum Type
    {
        NULL,
        NORMAL,
        ROUTING_SHORTCUT
    }

    public static class Converter extends BaseStringConverter<Edge>
    {
        private final Graph graph;

        public Converter(Graph graph, Listener listener)
        {
            super(listener);
            this.graph = graph;
        }

        @Override
        protected String onToString(Edge edge)
        {
            return Long.toString(edge.identifierAsLong());
        }

        @Override
        protected Edge onToValue(String value)
        {
            switch (EdgeIdentifier.Type.forString(value))
            {
                case LONG_IDENTIFIER:
                    return edgeForLongIdentifier(value);

                case EDGE_IDENTIFIER:
                    return edgeForMapIdentifier(value);

                default:
                    return unsupported();
            }
        }

        private Edge edgeForLongIdentifier(String value)
        {
            var identifierAsLong = Longs.parseFast(value);
            if (identifierAsLong != Longs.INVALID)
            {
                var identifier = new EdgeIdentifier(identifierAsLong);
                if (graph.contains(identifier))
                {
                    return graph.edgeForIdentifier(identifier);
                }
            }
            return null;
        }

        private Edge edgeForMapIdentifier(String value)
        {
            var identifier = new MapEdgeIdentifier.Converter(this).convert(value);
            return graph.edgeForIdentifier(identifier);
        }
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    protected Edge(Graph graph, EdgeIdentifier identifier)
    {
        graph(graph);
        identifier(identifier.asLong());
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    protected Edge(Graph graph, long identifier)
    {
        graph(graph);
        identifier(identifier);
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    protected Edge(Graph graph, long identifier, int index)
    {
        graph(graph);
        identifier(identifier);
        index(index);
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    protected Edge(Edge that)
    {
        graph(that.graph());
        identifier(that.identifierAsLong());
    }

    /**
     * Returns a copy of this edge as a mutable {@link HeavyWeightEdge}. If this edge is already a {@link
     * HeavyWeightEdge}, there is no expense incurred.
     */
    @Override
    public HeavyWeightEdge asHeavyWeight()
    {
        if (this instanceof HeavyWeightEdge)
        {
            return (HeavyWeightEdge) this;
        }
        var copy = dataSpecification().newHeavyWeightEdge(graph(), identifierAsLong());
        copy.copy(this);
        return copy;
    }

    /**
     * Returns this edge as a one-edge {@link Route}
     */
    @Override
    public Route asRoute()
    {
        return Route.fromEdge(this);
    }

    /**
     * Returns this edge as a {@link Segment} along the direct distance from the "from" {@link Vertex} to the "to"
     * {@link Vertex}.
     */
    public Segment asSegment()
    {
        return new Segment(from().location(), to().location());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphElementAttributes<?> attributes()
    {
        return EdgeAttributes.get();
    }

    /**
     * Returns the bounding rectangle for this edge's {@link #roadShape()}.
     */
    @Override
    public Rectangle bounds()
    {
        return store().retrieveBounds(this);
    }

    /**
     * Returns the type of bridge for this edge, if any
     */
    public BridgeType bridgeType()
    {
        return store().retrieveBridgeType(this);
    }

    /**
     * Returns the set of all edges (both in and out edges) connected to both ends of this edge (at "from" vertex and
     * "to" vertex), but not this edge itself.
     */
    public EdgeSet connectedEdges()
    {
        return inEdges().union(outEdges()).without(this);
    }

    /**
     * Returns in edges of "from" vertex and out edges of "to" vertex, but not this edge or the reversed edge.
     */
    public EdgeSet connectedEdgesWithoutReversed()
    {
        return connectedEdges().without(reversed());
    }

    /**
     * Returns the {@link Continent} where this edge exists
     */
    public Continent continent()
    {
        return country().continent();
    }

    /**
     * Returns the {@link Country} where this edge exists
     */
    public Country country()
    {
        return store().retrieveCountry(this);
    }

    /**
     * Returns the {@link County} where this edge exists
     */
    public County county()
    {
        return County.forLocation(bounds().center());
    }

    /**
     * Returns true if this edge's road shape crosses that edge's road shape
     */
    public boolean crosses(Edge that)
    {
        return roadShape().crosses(that.roadShape());
    }

    /**
     * Returns the differences between this edge and that edge
     */
    public Differences differencesFrom(Edge that)
    {
        return dataSpecification().compare(this, that);
    }

    /**
     * Returns the {@link PbfWayIdentifier} for this edge, but <i>negative</i> if the edge is reversed
     */
    public MapWayIdentifier directionalWayIdentifier()
    {
        return identifier().asDirectionalWayIdentifier();
    }

    /**
     * Returns the primary road name of this edge formatted for display to an end user
     */
    public String displayRoadName()
    {
        return roadName() == null ? "unnamed" : "'" + roadName() + "'";
    }

    /**
     * Returns the shortest distance between any node on this edge and the road shape of that edge
     */
    public Distance distanceTo(Edge that)
    {
        var snapper = new PolylineSnapper();
        var distance = Distance.MAXIMUM;
        for (var location : locationSequence())
        {
            var snap = snapper.snap(that, location);
            if (snap != null)
            {
                distance = distance.minimum(snap.distanceToSource());
            }
        }
        return distance;
    }

    /**
     * @param located The location
     * @return The end vertex (to or from) of this edge nearest to the given location
     */
    public Vertex endNearestTo(Located located)
    {
        var location = located.location();
        var toDistance = location.distanceTo(toLocation());
        var fromDistance = location.distanceTo(fromLocation());
        return fromDistance.isGreaterThan(toDistance) ? from() : to();
    }

    /**
     * Returns true if this edge leads into a roundabout
     */
    public boolean entersRoundabout()
    {
        for (var edge : outEdgeSequence())
        {
            if (edge.isRoundabout())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the heading of the final segment in this edge's road shape
     */
    public Heading finalHeading()
    {
        return roadShape().finalHeading();
    }

    /**
     * Returns the first segment of this edge's road shape
     */
    public Segment firstSegment()
    {
        return roadShape().firstSegment();
    }

    /**
     * Returns the forward edge (always having a positive identifier) for this edge. There is always a forward edge,
     * whether the road is one-way or two-way. There is only a reverse edge (with a negative identifier) if the edge is
     * two-way.
     * @see #reversed
     * @see #isReverse()
     * @see #reversed()
     */
    public Edge forward()
    {
        if (isReverse())
        {
            return reversed();
        }
        return this;
    }

    /**
     * Returns typical free flow speed for this edge's functional class
     */
    public Speed freeFlowForFunctionalClass()
    {
        switch (roadFunctionalClass())
        {
            case MAIN:
                return Speed.SIXTY_FIVE_MILES_PER_HOUR;

            case FIRST_CLASS:
                return Speed.FIFTY_FIVE_MILES_PER_HOUR;

            case SECOND_CLASS:
                return Speed.FORTY_FIVE_MILES_PER_HOUR;

            case THIRD_CLASS:
                return Speed.THIRTY_FIVE_MILES_PER_HOUR;

            case FOURTH_CLASS:
            default:
                return Speed.TWENTY_FIVE_MILES_PER_HOUR;
        }
    }

    /**
     * Returns the free flow speed category (a quantized range of speeds)
     */
    public SpeedCategory freeFlowSpeed()
    {
        return store().retrieveFreeFlow(this);
    }

    /**
     * Returns the "from" vertex of this (directed) edge
     */
    @Override
    public Vertex from()
    {
        return dataSpecification().newVertex(graph(), store().retrieveFromVertexIdentifier(this));
    }

    /**
     * Returns the set of edges attached to the "from" vertex of this edge, not including the reverse of this edge if it
     * is a two-way road
     */
    public EdgeSet fromEdgesWithoutThisEdge()
    {
        return from().edges().without(this).without(reversed());
    }

    /**
     * The grade separation level of this edge, if it happens to go under or over another edge
     */
    public GradeSeparation fromGradeSeparation()
    {
        return from().gradeSeparation();
    }

    /**
     * Returns the starting location of the edge
     * <p>
     * <b>Note</b>: If the edge doesn't yet have a "from" vertex, the first coordinate in the road shape will be used.
     * This method is necessary when loading edges into a graph because vertexes are not determined and added to the
     * vertex store until the end of graph loading.
     */
    public Location fromLocation()
    {
        var from = from();
        if (from == null)
        {
            return roadShape().start();
        }
        return from.location();
    }

    /**
     * Returns the "from" location of this edge as a DM7 long value
     */
    public long fromLocationAsLong()
    {
        return from().locationAsLong();
    }

    /**
     * Returns the node identifier of the "from" vertex
     */
    public MapNodeIdentifier fromNodeIdentifier()
    {
        return store().retrieveFromNodeIdentifier(this);
    }

    /**
     * Returns the identifier of the "from" vertex
     */
    @KivaKitExcludeProperty
    public VertexIdentifier fromVertexIdentifier()
    {
        return new VertexIdentifier(store().retrieveFromVertexIdentifier(this));
    }

    /**
     * Returns the hardest left-turn edge from this edge where a left turn is determined by a counter-clockwise angle
     * between the two edges, within the given tolerance
     */
    public Edge hardestLeft(Angle tolerance)
    {
        // Go through "out" edges from the "to" end of this edge
        var hardestAngle = Angle._0_DEGREES;
        Edge hardestEdge = null;
        for (var out : to().outEdges())
        {
            var angle = finalHeading().difference(out.initialHeading(), Chirality.COUNTERCLOCKWISE);
            if (angle.isClose(Angle._90_DEGREES, tolerance))
            {
                if (angle.isGreaterThan(hardestAngle))
                {
                    hardestAngle = angle;
                    hardestEdge = out;
                }
            }
        }
        return hardestEdge;
    }

    /**
     * Returns the hardest right-turn edge from this edge within the given tolerance
     */
    public Edge hardestRight(Angle tolerance)
    {
        // Go through "out" edges from the "to" end of this edge
        var hardestAngle = Angle._0_DEGREES;
        Edge hardestEdge = null;
        for (var out : to().outEdges())
        {
            var angle = finalHeading().difference(out.initialHeading(), Chirality.CLOCKWISE);
            if (angle.isClose(Angle._90_DEGREES, tolerance))
            {
                if (angle.isGreaterThan(hardestAngle))
                {
                    hardestAngle = angle;
                    hardestEdge = out;
                }
            }
        }
        return hardestEdge;
    }

    /**
     * Returns true if this edge has a road name
     */
    public boolean hasRoadName()
    {
        return roadName() != null;
    }

    /**
     * Returns true if this edge has the same base name as the given edge
     */
    public boolean hasSameRoadNameAs(Edge that)
    {
        var thisName = roadName();
        var thatName = that.roadName();
        if (thisName != null && thatName != null)
        {
            return thisName.extractNameOnly().equals(thatName.extractNameOnly());
        }
        return false;
    }

    /**
     * Returns true if the road has the same standardized root name as the given edge. For example, "Main St" and "Main
     * Street NE" would match.
     */
    @SuppressWarnings("SpellCheckingInspection")
    public boolean hasSameStandardizedBaseNameAs(Edge that)
    {
        var thisName = roadName();
        var thatName = that.roadName();
        if (thisName != null && thatName != null)
        {
            var standardizer = RoadNameStandardizer.get(locale(), RoadNameStandardizer.Mode.MESAKIT_STANDARDIZATION);
            return standardizer.standardize(thisName).baseName().equals(standardizer.standardize(thatName).baseName());
        }
        return false;
    }

    /**
     * Returns the heading of this edge.
     */
    public Heading heading()
    {
        return finalHeading();
    }

    /**
     * Returns the number of HOV lanes on this edge
     */
    public Count hovLaneCount()
    {
        return store().retrieveHovLaneCount(this);
    }

    /**
     * Returns the identifier for this edge. Edge identifiers are normally derived from an underlying way identifier by
     * adding a suffix of extra digits, identifying which part of the way the identifier represents. For example, the
     * way with identifier 1234 might be sectioned into 3 edges with identifiers 1234000001, 1234000002 and 1234000003.
     * @see EdgeIdentifier
     */
    @Override
    public EdgeIdentifier identifier()
    {
        return new EdgeIdentifier(identifierAsLong());
    }

    /**
     * Returns the sequence of "in" edges attached to the "from" vertex of this edge
     */
    public EdgeSequence inEdgeSequence()
    {
        return from().inEdgeSequence();
    }

    /**
     * Returns the set of "in" edges attached to the "from" vertex of this edge
     */
    public EdgeSet inEdges()
    {
        return from().inEdges();
    }

    /**
     * Returns the set of "in" edges attached to the "from" vertex of this edge, not including the reverse of this edge
     */
    public EdgeSet inEdgesWithoutReversed()
    {
        return inEdges().without(reversed());
    }

    /**
     * Returns a continuous non-branching route ending with this directed edge and extended using the given navigator
     * for as long as the route limiter allows
     */
    public Route inRoute(Navigator navigator, RouteLimiter limiter)
    {
        // Create the route with only this edge
        var route = Route.fromEdge(this);

        // Extend the route starting in the direction this edge is pointing
        var edge = navigator.next(this, Navigator.Direction.IN);

        while (route != null && edge != null && limiter.canExtendRoute(route, edge))
        {
            route = route.connect(edge);
            edge = navigator.next(edge, Navigator.Direction.IN);
        }
        return route;
    }

    /**
     * Returns the heading of the first segment in the road shape for this edge
     */
    public Heading initialHeading()
    {
        return roadShape().initialHeading();
    }

    /**
     * Returns true if this edge intersects the given rectangle. Intersects in this context means that it intersects or
     * is contained by the given rectangle. Used in spatial indexing.
     * @see Intersectable
     */
    @Override
    public boolean intersects(Rectangle bounds)
    {
        // If the given bounds completely contains this edge's bounding rectangle, then we don't need to do the more
        // expensive tests involving the road shape
        return bounds.contains(bounds()) || (bounds.intersects(bounds()) && roadShape().intersects(bounds));
    }

    /**
     * Returns true if either end of this way was clipped (clean cut) by a graph loader. Clean cutting is used by
     * composite graphs like WorldGraphs to break a large area of graph data down into cells containing sub-graphs.
     */
    public boolean isClipped()
    {
        return from().isClipped() || to().isClipped();
    }

    /**
     * Returns true if this edge is closed to traffic
     */
    public boolean isClosedToThroughTraffic()
    {
        return store().retrieveIsClosedToThroughTraffic(this);
    }

    /**
     * Returns true if this edge is connected to that one (at either vertex)
     */
    public boolean isConnectedTo(Edge that)
    {
        return vertexConnecting(that) != null;
    }

    /**
     * Returns true if this edge is connected to the given vertex (the vertex must be the "from" vertex or the "to"
     * vertex of the edge)
     */
    public boolean isConnectedTo(Vertex vertex)
    {
        return from().equals(vertex) || to().equals(vertex);
    }

    /**
     * Returns true if this edge is a connector (if it has the road subtype "connecting road"
     */
    public boolean isConnector()
    {
        return roadSubType() == RoadSubType.CONNECTING_ROAD;
    }

    /**
     * Returns true if this edge can be driven on
     */
    public boolean isDrivable()
    {
        return isNavigable(TransportMode.DRIVE);
    }

    /**
     * Returns true if this edge is a forward edge
     */
    public boolean isForward()
    {
        return !isReverse();
    }

    /**
     * Returns true if this edge is either the forward or the reverse of the given edge
     */
    public boolean isForwardOrReverseOf(Edge that)
    {
        return equals(that) || equals(that.reversed());
    }

    /**
     * Returns true if this edge is fully contained within the given bounds
     */
    @Override
    public final boolean isInside(Rectangle bounds)
    {
        if (bounds == Rectangle.MAXIMUM)
        {
            return true;
        }
        return bounds.contains(bounds());
    }

    /**
     * Returns true if this edge is in an intersection
     */
    public boolean isIntersectionEdge()
    {
        return unsupported();
    }

    /**
     * Returns true if this edge joins two incoming edges.
     */
    public boolean isJoiningEdge()
    {
        // If the edge is a dead-end or one way
        if (from().inEdgeCount().isLessThanOrEqualTo(Count._1))
        {
            // we don't join
            return false;
        }

        // otherwise, we join if there's not a single in edge when navigating with a non-branching
        // no uturn navigator
        return Navigator.NON_BRANCHING_NO_UTURN.in(this) == null;
    }

    /**
     * Returns true if this is a junction edge (an intersection link)
     */
    public boolean isJunctionEdge()
    {
        return roadSubType() == RoadSubType.INTERSECTION_LINK;
    }

    /**
     * Returns true if this edge is a connecting road or a ramp
     */
    public boolean isLink()
    {
        return isRamp() || isConnector();
    }

    /**
     * Returns true if this edge is a loop (a circle where the "from" vertex and "to" vertex are the same, and you can go
     * around and around).
     */
    public boolean isLoop()
    {
        return fromVertexIdentifier().equals(toVertexIdentifier());
    }

    /**
     * Returns true if this edge is considered part of a main road. In addition, bridges, tunnels and underpasses are
     * included.
     */
    public boolean isMainRoad()
    {
        switch (roadSubType())
        {
            case MAIN_ROAD:
            case SEPARATED_MAIN_ROAD:
            case BRIDGE:
            case OVERBRIDGE:
            case TUNNEL:
            case UNDERPASS:
                return true;

            default:
                return false;
        }
    }

    /**
     * Returns whichever edge is more important this edge or that edge.  Importance based on functional class, and on
     * road type if the two edges have the same functional class.
     */
    public boolean isMoreImportantThan(Edge that)
    {
        if (roadFunctionalClass().equals(that.roadFunctionalClass()))
        {
            if (roadType().equals(that.roadType()))
            {
                // Yield an arbitrary yet consistent ordering for equal importance edges
                return identifierAsLong() > that.identifierAsLong();
            }
            return roadType().isMoreImportantThan(that.roadType());
        }
        return roadFunctionalClass().isMoreImportantThan(that.roadFunctionalClass());
    }

    /**
     * Returns true if this edge has a road name
     */
    public boolean isNamed()
    {
        return !isNameless();
    }

    /**
     * Returns true if this edge doesn't have a road name
     */
    public boolean isNameless()
    {
        var name = roadName();
        return name == null || Strings.isEmpty(name.name()) || "Unnamed".equalsIgnoreCase(name.name());
    }

    /**
     * Returns true if this edge can be navigated in the given transport mode (walking, driving, biking, etc)
     * @see TransportMode
     */
    public boolean isNavigable(TransportMode mode)
    {
        switch (mode)
        {
            case ANY:
                return true;

            case DRIVE:
                switch (roadType())
                {
                    case FREEWAY:
                    case URBAN_HIGHWAY:
                    case HIGHWAY:
                    case THROUGHWAY:
                    case LOCAL_ROAD:
                    case FRONTAGE_ROAD:
                    case LOW_SPEED_ROAD:
                    case PRIVATE_ROAD:
                    case FERRY:
                        return true;

                    default:
                        return false;
                }

            case WALK:
                switch (roadType())
                {
                    case HIGHWAY:
                    case THROUGHWAY:
                    case LOCAL_ROAD:
                    case FRONTAGE_ROAD:
                    case LOW_SPEED_ROAD:
                    case PRIVATE_ROAD:
                    case WALKWAY:
                    case NON_NAVIGABLE:
                    case FERRY:
                        return true;

                    default:
                        return false;
                }

            case BIKE:
                switch (roadType())
                {
                    case HIGHWAY:
                    case THROUGHWAY:
                    case LOCAL_ROAD:
                    case FRONTAGE_ROAD:
                    case LOW_SPEED_ROAD:
                    case PRIVATE_ROAD:
                    case WALKWAY:
                    case NON_NAVIGABLE:
                    case RESERVED_1:
                        return true;

                    default:
                        return false;
                }

            default:
                return unsupported();
        }
    }

    /**
     * Returns true if this edge is a {@link Type#NORMAL} edge. This is true unless the edge is a "routing shortcut"
     * used to enhance the performance of navigation.
     */
    @KivaKitExcludeProperty
    public boolean isNormal()
    {
        return type() == Type.NORMAL;
    }

    /**
     * Returns true if this edge is a freeway off-ramp
     */
    public boolean isOffRamp()
    {
        return isRamp() && from().isOnFreeway();
    }

    /**
     * Returns true if this edge is a freeway on-ramp
     */
    public boolean isOnRamp()
    {
        return isRamp() && to().isOnFreeway();
    }

    /**
     * Returns true if this edge is on the same logical road, as determined by name (not considering N, S, E, NW, etc.),
     * as the given edge
     */
    public boolean isOnSameRoadAs(Edge that)
    {
        return new EdgePair(this, that).isSameRoad();
    }

    /**
     * Returns true if this edge and that edge have the same road type and subtype and are on the same road as
     * determined by the road name
     */
    public boolean isOnSameRoadWithSameTypeAs(Edge that)
    {
        return roadType() == that.roadType() && roadSubType() == that.roadSubType() && isOnSameRoadAs(that);
    }

    /**
     * Returns true if this edge is on the same way as the given edge
     */
    public boolean isOnSameWay(Edge that)
    {
        return wayIdentifier().equals(that.wayIdentifier());
    }

    /**
     * Returns true if this edge is on a one-way road.
     */
    public boolean isOneWay()
    {
        return store().isOneWay(this);
    }

    /**
     * Returns true if this edge is considered parallel to the given edge
     */
    public boolean isParallelTo(Edge that)
    {
        return new EdgePair(this, that).isParallel();
    }

    /**
     * Returns true if this edge is considered perpendicular to the given edge
     */
    public boolean isPerpendicularTo(Edge that)
    {
        return new EdgePair(this, that).isPerpendicular();
    }

    /**
     * Returns true if this edge is a ramp
     */
    public boolean isRamp()
    {
        return roadSubType() == RoadSubType.RAMP;
    }

    /**
     * Returns true if this edge is a reverse edge. Reversed edges have an identifier that is the negative of the
     * forward identifier, which is always positive.
     */
    public boolean isReverse()
    {
        return identifierAsLong() < 0;
    }

    /**
     * Returns true if this edge is part of a roundabout
     */
    public boolean isRoundabout()
    {
        return roadSubType().equals(RoadSubType.ROUNDABOUT);
    }

    /**
     * Returns true if this edge is a {@link Type#ROUTING_SHORTCUT} edge
     */
    @KivaKitExcludeProperty
    public boolean isRoutingShortCut()
    {
        return type() == Type.ROUTING_SHORTCUT;
    }

    /**
     * Returns true if this edge has the same {@link RoadFunctionalClass} as the given edge
     */
    public boolean isSameFunctionalClassAs(Edge that)
    {
        return roadFunctionalClass().equals(that.roadFunctionalClass());
    }

    /**
     * Returns true if this edge is a single segment (it is straight from the "from" node to the "to" node and therefore
     * has no road shape polyline in the edge store)
     */
    public boolean isSegment()
    {
        return store().retrieveRoadShape(this) == null;
    }

    /**
     * Returns true if this edge has a road shape with more than one segment
     */
    public boolean isShaped()
    {
        return !isSegment();
    }

    /**
     * Returns true if this edge was soft-cut. See SoftCut.
     */
    public boolean isSoftCut()
    {
        return tagValue("telenav:softcut") != null;
    }

    /**
     * Returns true if this edge is on a toll road
     */
    public boolean isTollRoad()
    {
        return store().retrieveIsTollRoad(this);
    }

    /**
     * Returns true if this edge represents one direction on a two-way road. If this method returns true, you can call
     * {@link #reversed()} to get edge in the opposite direction.
     */
    public boolean isTwoWay()
    {
        return store().isTwoWay(this);
    }

    /**
     * Returns true if this edge is under construction
     */
    public boolean isUnderConstruction()
    {
        return store().retrieveIsUnderConstruction(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid()
    {
        return super.isValid(throwingListener());
    }

    /**
     * Returns a key to use for this edge when putting it in a {@link PrimitiveMap}
     * @see LongKeyed
     */
    @Override
    public long key()
    {
        return identifierAsLong() * (isReverse() ? -1 : 1);
    }

    /**
     * Returns the number of lanes this edge has
     */
    @Override
    public Count laneCount()
    {
        return store().retrieveLaneCount(this);
    }

    /**
     * Returns the final segment of this edge's road shape
     */
    public Segment lastSegment()
    {
        return roadShape().lastSegment();
    }

    /**
     * Returns true if this edge leads to that edge
     */
    public boolean leadsTo(Edge that)
    {
        return to().equals(that.from());
    }

    /**
     * Returns true if this edge leads to that route
     */
    public boolean leadsTo(Route that)
    {
        return leadsTo(that.first());
    }

    /**
     * Returns true if this directional edge leads to a dead end
     */
    public boolean leadsToDeadEnd()
    {
        return to().isDeadEnd();
    }

    /**
     * Returns true if this edge forks. This is different from whether the "to" vertex is a decision point because it
     * <i>does not</i> consider uturns.
     */
    public boolean leadsToFork()
    {
        // If the edge is a dead-end or one way
        if (to().outEdgeCount().isLessThanOrEqualTo(Count._1))
        {
            // we don't fork
            return false;
        }

        // otherwise, we fork if there's not a single out edge when navigating with a non-branching,
        // no uturn navigator
        return Navigator.NON_BRANCHING_NO_UTURN.out(this) == null;
    }

    /**
     * Returns the counter-clockwise (left) turn angle to the given edge
     */
    public Angle leftTurnAngleTo(Edge that)
    {
        return turnAngleTo(that, Chirality.COUNTERCLOCKWISE);
    }

    /**
     * Returns the length of this edge
     */
    @Override
    public Distance length()
    {
        return Distance.millimeters(store().retrieveLengthInMillimeters(this));
    }

    /**
     * Returns the length of this edge in millimeters
     */
    public long lengthInMillimeters()
    {
        return store().retrieveLengthInMillimeters(this);
    }

    /**
     * Returns the locale for this edge
     */
    public MapLocale locale()
    {
        return country().locale();
    }

    /**
     * Returns the locations in this edge's road shape.
     */
    @Override
    public Iterable<Location> locationSequence()
    {
        return roadShape();
    }

    /**
     * This edge as a map identifier of the form "[way-identifier]:[from-node-identifier]:[to-node-identifier]"
     *
     * @see MapEdgeIdentifier
     */
    public MapEdgeIdentifier mapEdgeIdentifier()
    {
        return new MapEdgeIdentifier(wayIdentifier(), fromNodeIdentifier(), toNodeIdentifier());
    }

    /**
     * Returns this edge as a {@link MapIdentifier}, in this case a {@link PbfWayIdentifier}
     * @see MapIdentifier
     */
    @Override
    public PbfWayIdentifier mapIdentifier()
    {
        return wayIdentifier();
    }

    /**
     * Returns this edge as a way in the map data
     */
    public MapWay mapWay()
    {
        return MapWay.forEdge(this);
    }

    /**
     * Returns the {@link MetropolitanArea} where this edge exists
     */
    public MetropolitanArea metropolitanArea()
    {
        return MetropolitanArea.forLocation(fromLocation());
    }

    /**
     * Returns the most straight-on edge from this edge within the given tolerance
     */
    public Edge mostStraightOn(Angle tolerance)
    {
        // Go through "out" edges from the "to" end of this edge
        var mostStraightOnAngle = Angle._180_DEGREES;
        Edge mostStraightOnEdge = null;
        for (var out : to().outEdges())
        {
            // and if the smallest difference between the headings is close to zero,
            var angle = finalHeading().difference(out.initialHeading(), Chirality.SMALLEST);
            if (angle.isClose(Angle._0_DEGREES, tolerance))
            {
                // possibly update the most straight-on we've found
                if (angle.isLessThan(mostStraightOnAngle))
                {
                    mostStraightOnAngle = angle;
                    mostStraightOnEdge = out;
                }
            }
        }

        // Return the most straight-on edge we found
        return mostStraightOnEdge;
    }

    /**
     * Returns all edges within the given <i>rectangular</i> distance (not radius) that match the given matcher
     */
    public EdgeSet nearbyEdges(Distance range, Matcher<Edge> matcher)
    {
        var surrounded = new EdgeSet();
        for (var segment : roadShape().segments())
        {
            var polygon = segment.surroundingBox(range);
            if (polygon != null)
            {
                for (var edge : graph().edgesIntersecting(segment.bounds().expanded(range), matcher))
                {
                    if (polygon.intersectsOrContains(edge.roadShape()))
                    {
                        surrounded.add(edge);
                    }
                }
            }
        }
        return surrounded;
    }

    /**
     * Returns the edge that this edge leads to. If there is a fork, an arbitrary edge will be returned.
     */
    public Edge next()
    {
        return outEdges().first();
    }

    /**
     * Returns the longest non-branching route or null if the maximum number of edges is exceeded.
     */
    public Route nonBranchingRoute(Maximum maximumEdges)
    {
        var route = Route.fromEdge(this);
        while (route.first().inEdgesWithoutReversed().size() == 1
                && route.first().inEdgesWithoutReversed().first().outEdgesWithoutReversed().size() == 1)
        {
            route = route.prepend(route.first().inEdgesWithoutReversed().first());
            if (route.size() > maximumEdges.asInt())
            {
                return null;
            }
        }
        while (route.last().outEdgesWithoutReversed().size() == 1
                && route.last().outEdgesWithoutReversed().first().inEdgesWithoutReversed().size() == 1)
        {
            route = route.append(route.last().outEdgesWithoutReversed().first());
            if (route.size() > maximumEdges.asInt())
            {
                return null;
            }
        }
        return route;
    }

    /**
     * Returns the non-branching route that contains maximum number edges which have same road name as this edge
     */
    @SuppressWarnings("SameParameterValue")
    public Route nonBranchingRouteWithSameName(Maximum extensionNumber)
    {
        var route = Route.fromEdge(this);
        if (supports(EdgeAttributes.get().ROAD_NAMES))
        {
            var name = roadName();
            var count = Count._1;
            while (count.isLessThanOrEqualTo(extensionNumber)
                    && route.first().inEdgesWithoutReversed().withRoadName(name).size() == 1
                    && route.first().inEdgesWithoutReversed().withRoadName(name).first().outEdgesWithoutReversed()
                    .withRoadName(name).size() == 1)
            {
                route = route.prepend(route.first().inEdgesWithoutReversed().withRoadName(name).first());
                count = count.incremented();
                if (count.isGreaterThan(extensionNumber))
                {
                    break;
                }
            }

            count = Count._1;
            while (route.last().outEdgesWithoutReversed().withRoadName(name).size() == 1
                    && route.last().outEdgesWithoutReversed().withRoadName(name).first().inEdgesWithoutReversed()
                    .withRoadName(name).size() == 1)
            {
                route = route.append(route.last().outEdgesWithoutReversed().withRoadName(name).first());
                count = count.incremented();
                if (count.isGreaterThan(extensionNumber))
                {
                    break;
                }
            }
        }
        return route;
    }

    /**
     * Returns the opposite vertex on this edge from the given vertex
     */
    public Vertex oppositeVertex(Vertex vertex)
    {
        var from = from();
        var to = to();
        if (vertex.equals(from))
        {
            return to;
        }
        else if (vertex.equals(to))
        {
            return from;
        }
        throw invalidVertex(vertex);
    }

    /**
     * Specific to {@link OsmDataSpecification}. This method is for convenience to avoid down-casts.
     */
    public boolean osmCouldBeDoubleDigitized()
    {
        return unsupported();
    }

    /**
     * Returns true if this edge is on a way that is tagged with a destination tag for a sign-post.
     */
    public boolean osmIsDestinationTagged()
    {
        for (var tag : tagList())
        {
            var key = tag.getKey();
            if ("destination".equals(key) || "destination_sign".equals(key) || key.startsWith("destination:"))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if this edge is part of a double digitized road, also known as a separated road, where a separate
     * road runs parallel to the edge, usually separated by a median or divider.
     */
    public Boolean osmIsDoubleDigitized()
    {
        return unsupported();
    }

    /**
     * Returns true if this edge is tagged with "exit to" in OSM.
     */
    public boolean osmIsExitToTagged()
    {
        return from().tag("exit_to") != null;
    }

    /**
     * Returns true if this edge is an OSM link. This is specific to the {@link OsmDataSpecification}.
     */
    public boolean osmIsLink()
    {
        var highway = tagValue("highway");
        return highway != null && highway.contains("_link");
    }

    /**
     * Returns true if the edge has a reference tag. This is only relevant to the {@link OsmDataSpecification}.
     */
    public boolean osmIsMotorwayJunctionReferenceTagged()
    {
        if ("motorway_junction".equalsIgnoreCase(from().tagValue("highway")))
        {
            return from().hasTag("ref") || "yes".equals(from().tagValue("noref"));
        }
        return false;
    }

    /**
     * Returns true if this edge is labeled as one-way in OSM. This is specific to the {@link OsmDataSpecification}.
     */
    public boolean osmIsOneWay()
    {
        var value = tagValue("oneway");
        return ("yes".equals(value) || "1".equals(value) || "-1".equals(value));
    }

    public boolean osmIsProposed()
    {
        return "proposed".equalsIgnoreCase(tagValue("highway"));
    }

    /**
     * Returns true if this edge labeled as a roundabout in OSM. This is specific to the {@link OsmDataSpecification}.
     */
    public boolean osmIsRoundabout()
    {
        return hasTag("junction") && "roundabout".equalsIgnoreCase(tagValue("junction"));
    }

    /**
     * Returns true if this edge is an OSM service way. This is specific to the {@link OsmDataSpecification}.
     */
    public boolean osmIsServiceWay()
    {
        for (var tag : tagList())
        {
            if ("highway".equalsIgnoreCase(tag.getKey()) && "service".equalsIgnoreCase(tag.getValue()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Specific to {@link OsmDataSpecification}. This method is for convenience to avoid down-casts.
     */
    public Distance osmMaximumDoubleDigitizationSeparation()
    {
        return unsupported();
    }

    /**
     * Returns all the edges other than this one that are connected to this edge at the given vertex
     */
    public EdgeSet otherEdges(Vertex vertex)
    {
        if (isConnectedTo(vertex))
        {
            return vertex.edges().without(this);
        }
        throw invalidVertex(vertex);
    }

    /**
     * Returns the sequence of out edges connected to the "to" vertex of this edge
     */
    public EdgeSequence outEdgeSequence()
    {
        return to().outEdgeSequence();
    }

    /**
     * Returns the set of outbound edges from this edge's "to" vertex, including two-way roads.
     */
    public EdgeSet outEdges()
    {
        return to().outEdges();
    }

    /**
     * Returns the set of out edges from this edge's "to" vertex, not including the reverse of this edge if it is a
     * two-way road
     */
    public EdgeSet outEdgesWithoutReversed()
    {
        return outEdges().without(reversed());
    }

    /**
     * Returns a continuous non-branching route starting with this directed edge and extended using the given navigator
     * for as long as the route limiter allows
     */
    public Route outRoute(Navigator navigator, RouteLimiter limiter)
    {
        // Create the route with only this edge
        var route = Route.fromEdge(this);

        // Extend the route starting in the direction this edge is pointing
        var edge = navigator.next(this, Navigator.Direction.OUT);

        while (edge != null && limiter.canExtendRoute(route, edge))
        {
            route = route.connect(edge);
            edge = navigator.next(edge, Navigator.Direction.OUT);
        }
        return route;
    }

    /**
     * This method populates the properties of this object with dummy values so that the edge can be added to a
     * compressed graph for testing purposes. Only {@link HeavyWeightEdge}s implement this method, but it is available
     * here to avoid down-casting.
     */
    public void populateWithTestValues()
    {
        unsupported();
    }

    /**
     * Returns the edge that this edge leads to. If there is a fork, an arbitrary edge will be returned.
     */
    public Edge previous()
    {
        return inEdges().first();
    }

    /**
     * Returns the properties of this edge from the edge's {@link DataSpecification}, for use in producing a debug
     * string for the edge
     * @see GraphElementPropertySet
     * @see StringFormattable
     */
    @Override
    public GraphElementPropertySet<Edge> properties()
    {
        return dataSpecification().edgeProperties();
    }

    /**
     * <b>Internal API</b>
     *
     * @return The raw edge identifier. Used only when a {@link GraphLoader} is loading raw data.
     */
    public EdgeIdentifier rawIdentifier()
    {
        return unsupported();
    }

    /**
     * Returns the set of relations that reference this edge
     */
    public Set<EdgeRelation> relations()
    {
        return store().retrieveRelations(this);
    }

    /**
     * Returns the reverse edge for this edge, if it is a two-way road, otherwise the edge itself
     * @see #forward()
     * @see #isForward()
     */
    public Edge reverse()
    {
        if (isForward())
        {
            return reversed();
        }
        return this;
    }

    /**
     * Returns the reverse of this edge if the road is two-way, or null if it is not.
     */
    @Override
    public Edge reversed()
    {
        if (isTwoWay())
        {
            return graph().edgeForIdentifier(-identifierAsLong());
        }
        return null;
    }

    /**
     * Returns the clockwise (right) turn angle to the given edge
     */
    public Angle rightTurnAngleTo(Edge that)
    {
        return turnAngleTo(that, Chirality.CLOCKWISE);
    }

    /**
     * Returns the functional class of this edge
     * @see RoadFunctionalClass
     */
    @Override
    public RoadFunctionalClass roadFunctionalClass()
    {
        return store().retrieveRoadFunctionalClass(this);
    }

    /**
     * Returns the most important road name of this edge, either official, alternate or route, in that order
     */
    @Override
    public RoadName roadName()
    {
        var name = roadName(RoadName.Type.OFFICIAL);
        if (name == null)
        {
            name = roadName(RoadName.Type.ALTERNATE);
            if (name == null)
            {
                name = roadName(RoadName.Type.ROUTE);
            }
        }
        return name;
    }

    /**
     * Returns the first road name of the given type or null if none exists
     */
    @Override
    public RoadName roadName(RoadName.Type type)
    {
        var names = roadNames(type);
        if (names != null)
        {
            var iterator = names.iterator();
            if (iterator.hasNext())
            {
                return iterator.next();
            }
        }
        return null;
    }

    /**
     * Returns set of all unique road names of all types
     */
    public Set<RoadName> roadNames()
    {
        Set<RoadName> names = new HashSet<>();
        for (var type : RoadName.Type.values())
        {
            names.addAll(roadNames(type));
        }
        return names;
    }

    /**
     * Returns all road names of the given type
     */
    public List<RoadName> roadNames(RoadName.Type type)
    {
        return store().retrieveRoadNames(this, type);
    }

    /**
     * Returns the shape of this directed edge on the map as a {@link Polyline}
     */
    @Override
    @KivaKitExcludeProperty
    public Polyline roadShape()
    {
        var polyline = store().retrieveRoadShape(this);
        if (polyline == null)
        {
            return Polyline.fromLocations(fromLocationAsLong(), toLocationAsLong());
        }

        // If the edge is reversed return the reversed polyline
        return isReverse() ? polyline.reversed() : polyline;
    }

    /**
     * Returns the road state of this edge: one-way, two-way or closed.
     */
    @Override
    public RoadState roadState()
    {
        return store().retrieveRoadState(this);
    }

    /**
     * Returns the road subtype for this edge, such as roundabout, service road, main road, etc.
     */
    @Override
    public RoadSubType roadSubType()
    {
        return store().retrieveRoadSubType(this);
    }

    /**
     * Returns the surface of this edge: paved, unpaved or poor condition.
     */
    public RoadSurface roadSurface()
    {
        return store().retrieveRoadSurface(this);
    }

    /**
     * Returns the road type of this edge such as: freeway, highway, local road, etc.
     */
    @Override
    public RoadType roadType()
    {
        return store().retrieveRoadType(this);
    }

    /**
     * Returns a route starting with this edge and extending as far as possible via "in" and "out" edges that match the
     * given matcher. Loops are detected to avoid non-termination.
     */
    public Route route(Matcher<Edge> matcher)
    {
        var builder = new RouteBuilder();
        builder.append(this);
        boolean moved;
        var at = this;
        var visited = new EdgeSet();
        visited.add(this);
        do
        {
            moved = false;
            for (var out : at.outEdgesWithoutReversed())
            {
                if (matcher.matches(out))
                {
                    if (visited.contains(out))
                    {
                        break;
                    }
                    visited.add(out);
                    builder.append(out);
                    at = out;
                    moved = true;
                    break;
                }
            }
        }
        while (moved);

        at = this;
        do
        {
            moved = false;
            for (var in : at.inEdgesWithoutReversed())
            {
                if (matcher.matches(in))
                {
                    if (visited.contains(in))
                    {
                        break;
                    }
                    visited.add(in);
                    builder.prepend(in);
                    at = in;
                    moved = true;
                    break;
                }
            }
        }
        while (moved);
        return builder.route();
    }

    /**
     * Returns a route, as navigated by the given {@link Navigator} for up to the maximum distance. The final edge in
     * the route may extend beyond the maximum distance.
     */
    public Route route(Navigator navigator, Distance maximum)
    {
        RouteLimiter limiter = new LengthRouteLimiter(maximum, LengthRouteLimiter.Type.LENIENT);

        return route(navigator, limiter);
    }

    /**
     * Returns a route, as navigated by the given {@link Navigator} for up to the maximum number of edges.
     */
    public Route route(Navigator navigator, Maximum maximum)
    {
        return route(navigator, new EdgeCountRouteLimiter(maximum));
    }

    /**
     * Returns a route, as navigated by the given {@link Navigator} as limited by the given {@link RouteLimiter}
     */
    public Route route(Navigator navigator, RouteLimiter routeLimiter)
    {
        var out = outRoute(navigator, routeLimiter);
        if (out != null && (out.isLoop() || out.size() == 1))
        {
            return out;
        }

        var in = inRoute(navigator, routeLimiter);
        if (in != null && (in.isLoop() || in.size() == 1))
        {
            return in;
        }

        if (in != null && out != null)
        {
            return in.connect(out.withoutFirst());
        }

        return in != null ? in : out;
    }

    /**
     * Returns the road name or "unnamed" if there is no road name
     */
    public String safeRoadName()
    {
        return roadName() == null ? "unnamed" : roadName().name();
    }

    /**
     * Returns the list of {@link ShapePoint}s as {@link MapNodeIdentifier}s. This method is only meaningful if full
     * node information is available, as determined by {@link Graph#supportsFullPbfNodeInformation()}.
     */
    public List<MapNodeIdentifier> shapePointNodeIdentifiers()
    {
        List<MapNodeIdentifier> identifiers = new ArrayList<>();
        for (var point : shapePoints())
        {
            identifiers.add(point.mapIdentifier());
        }
        return identifiers;
    }

    /**
     * Returns the shape points for this edge.  This method is only meaningful if full node information is available, as
     * determined by {@link Graph#supportsFullPbfNodeInformation()}.
     */
    public List<ShapePoint> shapePoints()
    {
        return graph().shapePoints(roadShape());
    }

    /**
     * Returns the shape points for this edge, not including vertexes.  This method is only meaningful if full node
     * information is available, as determined by {@link Graph#supportsFullPbfNodeInformation()}.
     */
    public List<ShapePoint> shapePointsWithoutVertexes()
    {
        var shapePoints = shapePoints();
        if (shapePoints.size() >= 2)
        {
            shapePoints.remove(0);
            shapePoints.remove(shapePoints.size() - 1);
        }
        return shapePoints;
    }

    /**
     * Returns available support for sign posts
     */
    public Set<SignPostSupport> signPostSupport()
    {
        Set<SignPostSupport> support = new HashSet<>();
        if (osmIsDestinationTagged())
        {
            support.add(SignPostSupport.DESTINATION_TAGGED);
        }
        if (osmIsMotorwayJunctionReferenceTagged())
        {
            support.add(SignPostSupport.MOTORWAY_JUNCTION_REFERENCE_TAGGED);
        }
        if (osmIsExitToTagged())
        {
            support.add(SignPostSupport.EXIT_TO_TAGGED);
        }
        return support;
    }

    /**
     * Returns the speed limit on this edge
     */
    public Speed speedLimit()
    {
        return store().retrieveSpeedLimit(this);
    }

    /**
     * Returns the {@link State} where this edge exists
     */
    public State state()
    {
        return State.forLocation(fromLocation());
    }

    /**
     * Returns the smallest turn angle from this edge to the given edge
     */
    public Angle straightOnTurnAngleTo(Edge that)
    {
        return turnAngleTo(that, Chirality.SMALLEST);
    }

    /**
     * Returns true if this edge supports node identifiers
     */
    public boolean supportsNodeIdentifiers()
    {
        return supports(EdgeAttributes.get().FROM_NODE_IDENTIFIER);
    }

    /**
     * Returns the "to" end-point of this edge (in terms of traffic flow)
     */
    @Override
    public Vertex to()
    {
        return dataSpecification().newVertex(graph(), store().retrieveToVertexIdentifier(this));
    }

    /**
     * Returns the set of edges attached to the "to" vertex of this edge, not including this edge or the reverse of this
     * edge if it is a two-way road
     */
    public EdgeSet toEdgesWithoutThisEdge()
    {
        return to().edges().without(this).without(reversed());
    }

    /**
     * Returns the grade separation level at the "to" vertex of this edge
     */
    public GradeSeparation toGradeSeparation()
    {
        return to().gradeSeparation();
    }

    /**
     * Returns the ending location of the edge. If the edge doesn't yet have a "to" vertex, the last coordinate in the
     * road shape will be used. This method is useful when loading edges into a graph because the vertexes are not
     * determined until the end of graph loading, so getTo().getLocation() will not work during the loading process to
     * determine which edges are inside a given bounding rectangle.
     */
    public Location toLocation()
    {
        var to = to();
        if (to == null)
        {
            return roadShape().end();
        }
        return to.location();
    }

    /**
     * Returns the "to" location of this edge as a DM7 long value
     */
    public long toLocationAsLong()
    {
        return to().locationAsLong();
    }

    /**
     * Returns the node identifier of the "to" vertex of this edge
     */
    public MapNodeIdentifier toNodeIdentifier()
    {
        return store().retrieveToNodeIdentifier(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        if (Edge.DEBUG.isDebugOn())
        {
            return asString();
        }
        else
        {
            return CaseFormat.hyphenatedName(getClass()) + " " + identifierAsLong() + " " + fromVertexIdentifier() + " -> " + toVertexIdentifier();
        }
    }

    /**
     * Returns the identifier of the "to" vertex
     */
    @KivaKitExcludeProperty
    public VertexIdentifier toVertexIdentifier()
    {
        return new VertexIdentifier(store().retrieveToVertexIdentifier(this));
    }

    /**
     * Returns the amount of time it should take to travel the length of this edge at the average free-flow speed
     */
    public Duration travelTime()
    {
        return travelTime(freeFlowSpeed().average());
    }

    /**
     * Returns the amount of time it would take to travel the length of this edge at the given speed
     */
    public Duration travelTime(Speed speed)
    {
        return speed.timeToTravel(length());
    }

    /**
     * Returns the travel time along this edge based on the road type rather than free flow
     */
    public Duration travelTimeForFunctionalClass()
    {
        return freeFlowForFunctionalClass().timeToTravel(length());
    }

    /**
     * Returns the travel time in milliseconds
     * @see #travelTime()
     */
    public int travelTimeInMilliseconds()
    {
        return (int) travelTime().asMilliseconds();
    }

    /**
     * Returns the turn angle from this edge to the given edge in the given chirality (clockwise, counter-clockwise or
     * smallest angle)
     * @see Chirality
     */
    public Angle turnAngleTo(Edge that, Chirality chirality)
    {
        return finalHeading().difference(that.initialHeading(), chirality);
    }

    /**
     * Returns set of all turn restriction routes that this edge participates in
     */
    public Set<EdgeRelation> turnRestrictions()
    {
        var relations = relations();
        if (!relations.isEmpty())
        {
            Set<EdgeRelation> turnRestrictions = new HashSet<>();
            for (var relation : relations)
            {
                if (relation.isTurnRestriction())
                {
                    turnRestrictions.add(relation);
                }
            }
            return turnRestrictions;
        }
        return relations;
    }

    /**
     * Returns set of turn restriction routes that begin at this edge
     */
    public Set<EdgeRelation> turnRestrictionsBeginningAt()
    {
        if (supports(EdgeAttributes.get().RELATIONS))
        {
            var relations = relations();
            if (!relations.isEmpty())
            {
                Set<EdgeRelation> turnRestrictions = new HashSet<>();
                for (var relation : relations)
                {
                    if (relation.isTurnRestriction())
                    {
                        var route = relation.asRoute();
                        if (route != null && route.first().equals(this))
                        {
                            turnRestrictions.add(relation);
                        }
                    }
                }
                return turnRestrictions;
            }
        }
        return Collections.emptySet();
    }

    /**
     * Returns the type of turn from this edge to that edge, like: left, right, u-turn, hard left, etc.
     */
    public TurnType turnType(Edge that)
    {
        return turnType(that, TwoHeadingTurnClassifier.DEFAULT);
    }

    /**
     * Returns the type of turn from this edge to that edge using the given turn classifier
     */
    public TurnType turnType(Edge that, TwoHeadingTurnClassifier classifier)
    {
        return classifier.type(lastSegment(), that.firstSegment());
    }

    /**
     * Returns the type of edge
     */
    public Type type()
    {
        return Type.NORMAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Validator validator(ValidationType type)
    {
        return new ElementValidator()
        {
            @Override
            protected void onValidate()
            {
                validate(Edge.super.validator(type));

                problemIf(identifier() == null, "identifier is missing");

                if (type != GraphElement.VALIDATE_RAW)
                {
                    // Until the vertex store is committed, we can't know all of this, so we skip it when adding edges
                    problemIf(fromLocation() == null, "fromLocation is missing");
                    problemIf(toLocation() == null, "toLocation is missing");
                    problemIf(fromLocation() != null && fromLocationAsLong() == 0, "fromLocation is zero");
                    problemIf(toLocation() != null && toLocationAsLong() == 0, "toLocation is zero");
                    glitchIf(roadShape() == null, "roadShape is missing");
                    glitchIf(heading() == null, "heading is missing");
                    glitchIf(length() == null, "length is missing");
                    glitchIf(roadShape() == null, "roadShape is missing");
                    glitchIf(roadShape().length().isZero(), "roadShape is zero length");
                    glitchIf(roadShape().size() <= 1, "roadShape has only $ locations", roadShape().size());
                }

                problemIf(type() == null, "type is missing");
                glitchIf(roadState() == null, "roadState is missing");
                glitchIf(roadType() == null, "roadType is missing");
                glitchIf(roadSubType() == null, "roadSubType is missing");
                glitchIf(roadFunctionalClass() == null, "roadFunctionalClass is missing");
                warningIf(freeFlowSpeed() == null, "freeFlow is missing");
            }
        };
    }

    /**
     * Returns the vertex that connects this edge with that edge or null if the two edges are not connected at a vertex
     */
    public Vertex vertexConnecting(Edge that)
    {
        // Don't allow the case of an edge being "connected" to itself
        if (!equals(that))
        {
            // If that edge is connected to the "from" vertex
            var from = that.from();
            if (isConnectedTo(from))
            {
                // return the "from" vertex
                return from;
            }

            // If that edge is connected to the "to" vertex
            var to = that.to();
            if (isConnectedTo(to))
            {
                // return the "to" vertex
                return to;
            }
        }
        return null;
    }

    /**
     * Returns the way that this edge is a part of as a {@link Route}
     */
    public Route wayAsRoute()
    {
        try
        {
            var firstEdge = graph().edgeForIdentifier(identifier().asForward().withSequenceNumber(0));
            return firstEdge.route(new WayNavigator(firstEdge), Maximum._10_000);
        }
        catch (Exception e)
        {
            // If we are unable to get the way as a route, then just return this edge as the
            // route instead. This will occur in degenerate cases like OSM way #221097361
            // where there is no Route representation possible.
            return asRoute();
        }
    }

    /**
     * Returns the way identifier for this edge
     */
    @Override
    public PbfWayIdentifier wayIdentifier()
    {
        return identifier().asWayIdentifier();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EdgeStore store()
    {
        return subgraph().edgeStore();
    }

    CommonEntityData commonEntityData()
    {
        return new CommonEntityData(
                wayIdentifier().asLong(),
                pbfRevisionNumber().asInt(),
                new Timestamp(lastModificationTime().milliseconds()),
                new OsmUser(pbfUserIdentifier().asInt(), pbfUserName().name()),
                pbfChangeSetIdentifier().asLong(),
                tagList().asList());
    }

    /**
     * Returns illegalArgument exception for a vertex that is not connected to this edge
     */
    private IllegalArgumentException invalidVertex(Vertex vertex)
    {
        return new IllegalArgumentException("Vertex " + vertex + " is not attached to " + this);
    }
}
