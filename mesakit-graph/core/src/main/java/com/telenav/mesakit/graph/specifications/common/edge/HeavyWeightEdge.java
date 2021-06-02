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

package com.telenav.mesakit.graph.specifications.common.edge;

import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.functions.Functions;
import com.telenav.kivakit.kernel.language.objects.Objects;
import com.telenav.kivakit.kernel.language.reflection.property.filters.KivaKitExcludeProperty;
import com.telenav.kivakit.kernel.language.time.Duration;
import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.identifiers.VertexIdentifier;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfChangeSetIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfRevisionNumber;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserName;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Heading;
import com.telenav.mesakit.map.measurements.motion.Speed;
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
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensure;
import static com.telenav.mesakit.map.road.model.RoadState.ONE_WAY;
import static com.telenav.mesakit.map.road.model.RoadState.TWO_WAY;

public class HeavyWeightEdge extends Edge
{
    private int index;

    private Boolean fromVertexClipped;

    private Boolean isClosedToThroughTraffic;

    private Boolean isDoubleDigitized;

    private Boolean isTollRoad;

    private Boolean isUnderConstruction;

    private Boolean toVertexClipped;

    private BridgeType bridgeType;

    private Count hovLaneCount;

    private Count laneCount;

    private Country country;

    private County county;

    private Location fromLocation;

    private Distance length;

    private Type type;

    private EdgeIdentifier rawIdentifier;

    private MetropolitanArea metropolitanArea;

    private MapNodeIdentifier fromNodeIdentifier;

    private MapNodeIdentifier toNodeIdentifier;

    private Polyline roadShape;

    private Rectangle bounds;

    private RoadFunctionalClass roadFunctionalClass;

    private RoadState roadState;

    private RoadSubType roadSubType;

    private RoadSurface surface;

    private RoadType roadType;

    private Speed referenceSpeed;

    private Speed reverseReferenceSpeed;

    private final Map<RoadName.Type, List<RoadName>> roadNames = new HashMap<>();

    private Speed speedLimit;

    private SpeedCategory freeFlow;

    private State state;

    private Vertex from;

    private Vertex to;

    private Location toLocation;

    private PbfChangeSetIdentifier pbfChangeSetIdentifier;

    private PbfRevisionNumber pbfRevisionNumber;

    private PbfUserIdentifier pbfUserIdentifier;

    private PbfUserName pbfUserName;

    private Time lastModificationTime;

    private PbfTagList tags = PbfTagList.EMPTY;

    private GradeSeparation fromGradeSeparation;

    private GradeSeparation toGradeSeparation;

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public HeavyWeightEdge(final Graph graph, final EdgeIdentifier identifier)
    {
        super(graph, identifier);
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public HeavyWeightEdge(final Graph graph, final long identifier)
    {
        super(graph, identifier);
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    @SuppressWarnings("CopyConstructorMissesField")
    protected HeavyWeightEdge(final HeavyWeightEdge that)
    {
        super(that.graph(), that.identifier());
        copy(that);
    }

    public void addRoadName(final RoadName.Type type, final RoadName roadName)
    {
        ensure(type != null);
        ensure(roadName != null);
        final var names = roadNames.computeIfAbsent(type, k -> new ArrayList<>());
        names.add(roadName);
    }

    @Override
    public Rectangle bounds()
    {
        return bounds;
    }

    @Override
    public BridgeType bridgeType()
    {
        return bridgeType;
    }

    public void bridgeType(final BridgeType bridgeType)
    {
        this.bridgeType = bridgeType;
    }

    public void closedToThroughTraffic(final boolean isClosedToThroughTraffic)
    {
        this.isClosedToThroughTraffic = isClosedToThroughTraffic;
    }

    @MustBeInvokedByOverriders
    public void copy(final Edge that)
    {
        final var thatFrom = that.from();
        final var thatTo = that.to();

        index(that.index());
        type(that.type());
        from(thatFrom);
        to(thatTo);
        freeFlow(that.freeFlowSpeed());
        laneCount(that.laneCount());
        if (roadShape == null && that.isShaped())
        {
            roadShape(that.roadShape());
        }
        else
        {
            bounds = that.bounds();
        }
        fromLocation(that.fromLocation());
        toLocation(that.toLocation());
        length(that.length());
        roadState(that.roadState());
        roadSubType(that.roadSubType());
        roadType(that.roadType());
        roadFunctionalClass(that.roadFunctionalClass());

        fromGradeSeparation(that.fromGradeSeparation());
        toGradeSeparation(that.toGradeSeparation());

        tags(that.tagList());
        lastModificationTime(that.lastModificationTime());

        pbfChangeSetIdentifier(that.pbfChangeSetIdentifier());
        pbfRevisionNumber(that.pbfRevisionNumber());
        pbfUserIdentifier(that.pbfUserIdentifier());
        pbfUserName(that.pbfUserName());

        bridgeType(that.bridgeType());
        closedToThroughTraffic(that.isClosedToThroughTraffic());
        country(that.country());
        fromNodeIdentifier(that.fromNodeIdentifier());
        hovLaneCount(that.hovLaneCount());
        speedLimit(that.speedLimit());
        surface(that.roadSurface());
        tollRoad(that.isTollRoad());
        toNodeIdentifier(that.toNodeIdentifier());
        underConstruction(that.isUnderConstruction());

        if (thatFrom != null)
        {
            fromVertexClipped(thatFrom.isClipped());
        }
        if (thatTo != null)
        {
            toVertexClipped(thatTo.isClipped());
        }
        copyRoadNames(that);
    }

    public void copyRoadNames(final Edge that)
    {
        if (that.supports(EdgeAttributes.get().ROAD_NAMES))
        {
            for (final var type : RoadName.Type.values())
            {
                final var names = that.roadNames(type);
                if (names != null)
                {
                    for (final var name : names)
                    {
                        addRoadName(type, name);
                    }
                }
            }
        }
    }

    @Override
    public Country country()
    {
        return country;
    }

    public void country(final Country country)
    {
        this.country = country;
    }

    @Override
    public County county()
    {
        return county;
    }

    public void county(final County county)
    {
        this.county = county;
    }

    public void freeFlow(final SpeedCategory freeFlow)
    {
        this.freeFlow = freeFlow;
    }

    @Override
    public SpeedCategory freeFlowSpeed()
    {
        return freeFlow;
    }

    @Override
    public Vertex from()
    {
        return from;
    }

    public void from(final Vertex from)
    {
        this.from = from;
        if (from != null)
        {
            fromLocation = from.location();
        }
    }

    public void fromGradeSeparation(final GradeSeparation separation)
    {
        // Grade separation can legitimately be null here because we don't store the omnipresent
        // grade separation "GROUND"
        fromGradeSeparation = separation == null ? GradeSeparation.GROUND : separation;
    }

    @Override
    public GradeSeparation fromGradeSeparation()
    {
        return fromGradeSeparation;
    }

    @Override
    public Location fromLocation()
    {
        return fromLocation;
    }

    public void fromLocation(final Location fromLocation)
    {
        assert fromLocation != null;
        this.fromLocation = fromLocation;
    }

    @Override
    public MapNodeIdentifier fromNodeIdentifier()
    {
        return fromNodeIdentifier;
    }

    public void fromNodeIdentifier(final MapNodeIdentifier identifier)
    {
        fromNodeIdentifier = identifier;
    }

    public void fromVertexClipped(final Boolean fromVertexClipped)
    {
        this.fromVertexClipped = fromVertexClipped;
    }

    @Override
    @KivaKitExcludeProperty
    public VertexIdentifier fromVertexIdentifier()
    {
        return from != null ? from.identifier() : null;
    }

    @Override
    public Count hovLaneCount()
    {
        return hovLaneCount;
    }

    public void hovLaneCount(final Count hovLaneCount)
    {
        this.hovLaneCount = hovLaneCount;
    }

    @Override
    public int index()
    {
        return index;
    }

    @Override
    public void index(final int index)
    {
        this.index = index;
    }

    @Override
    public boolean isClosedToThroughTraffic()
    {
        return Objects.notNullOr(isClosedToThroughTraffic, false);
    }

    public void isClosedToThroughTraffic(final Boolean isClosedToThroughTraffic)
    {
        this.isClosedToThroughTraffic = isClosedToThroughTraffic;
    }

    public void isDoubleDigitized(final Boolean isDoubleDigitized)
    {
        this.isDoubleDigitized = isDoubleDigitized;
    }

    public Boolean isFromVertexClipped()
    {
        return fromVertexClipped;
    }

    @Override
    public boolean isHeavyWeight()
    {
        return true;
    }

    @Override
    public boolean isOneWay()
    {
        return roadState() == ONE_WAY;
    }

    @Override
    public boolean isSegment()
    {
        return roadShape == null;
    }

    public Boolean isToVertexClipped()
    {
        return Objects.notNullOr(toVertexClipped, false);
    }

    @Override
    public boolean isTollRoad()
    {
        return Objects.notNullOr(isTollRoad, false);
    }

    public void isTollRoad(final Boolean isTollRoad)
    {
        this.isTollRoad = isTollRoad;
    }

    @Override
    public boolean isTwoWay()
    {
        return roadState() == TWO_WAY;
    }

    @Override
    public boolean isUnderConstruction()
    {
        return Objects.notNullOr(isUnderConstruction, false);
    }

    public void isUnderConstruction(final Boolean isUnderConstruction)
    {
        this.isUnderConstruction = isUnderConstruction;
    }

    @Override
    public Count laneCount()
    {
        return laneCount;
    }

    public void laneCount(final Count laneCount)
    {
        this.laneCount = laneCount;
    }

    @Override
    public Time lastModificationTime()
    {
        return lastModificationTime;
    }

    public void lastModificationTime(final Time lastModified)
    {
        lastModificationTime = lastModified;
    }

    @Override
    public Distance length()
    {
        return length;
    }

    public void length(final Distance length)
    {
        this.length = length;
    }

    @Override
    public MetropolitanArea metropolitanArea()
    {
        return metropolitanArea;
    }

    public void metropolitanArea(final MetropolitanArea metropolitanArea)
    {
        this.metropolitanArea = metropolitanArea;
    }

    @Override
    public Boolean osmIsDoubleDigitized()
    {
        return isDoubleDigitized;
    }

    @Override
    public PbfChangeSetIdentifier pbfChangeSetIdentifier()
    {
        return pbfChangeSetIdentifier;
    }

    public void pbfChangeSetIdentifier(final PbfChangeSetIdentifier PbfChangeSetIdentifier)
    {
        pbfChangeSetIdentifier = PbfChangeSetIdentifier;
    }

    @Override
    public PbfRevisionNumber pbfRevisionNumber()
    {
        return pbfRevisionNumber;
    }

    public void pbfRevisionNumber(final PbfRevisionNumber revision)
    {
        pbfRevisionNumber = revision;
    }

    @Override
    public PbfUserIdentifier pbfUserIdentifier()
    {
        return pbfUserIdentifier;
    }

    public void pbfUserIdentifier(final PbfUserIdentifier PbfUserIdentifier)
    {
        pbfUserIdentifier = PbfUserIdentifier;
    }

    @Override
    public PbfUserName pbfUserName()
    {
        return pbfUserName;
    }

    public void pbfUserName(final PbfUserName PbfUserName)
    {
        pbfUserName = PbfUserName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populateWithTestValues()
    {
        bridgeType(BridgeType.NONE);
        closedToThroughTraffic(false);
        country(Country.UNITED_STATES);
        county(Country.UNITED_STATES.CALIFORNIA.SAN_MATEO_COUNTY);
        freeFlow(SpeedCategory.forSpeed(Speed.FIFTY_MILES_PER_HOUR));
        from(graph().newHeavyWeightVertex(new VertexIdentifier(5)));
        fromVertexClipped(false);
        hovLaneCount(Count._0);
        laneCount(Count._1);
        length(Distance.TEN_METERS);
        metropolitanArea(Country.UNITED_STATES.CALIFORNIA.SAN_FRANCISCO_OAKLAND);
        pbfChangeSetIdentifier(new PbfChangeSetIdentifier(5));
        fromNodeIdentifier(new PbfNodeIdentifier(9));
        fromLocation(Location.TELENAV_HEADQUARTERS);
        lastModificationTime(Time.now());
        pbfRevisionNumber(new PbfRevisionNumber(22));
        tags(PbfTagList.of(new Tag("person", "shibo")));
        toNodeIdentifier(new PbfNodeIdentifier(19));
        pbfUserIdentifier(new PbfUserIdentifier(1357));
        pbfUserName(new PbfUserName("shibo"));
        rawIdentifier(new EdgeIdentifier(100));
        referenceSpeed(Speed.FIFTY_MILES_PER_HOUR);
        uniDbReverseReferenceSpeed(Speed.FIFTY_MILES_PER_HOUR);
        roadFunctionalClass(RoadFunctionalClass.FIRST_CLASS);
        roadNames(RoadName.Type.OFFICIAL, ObjectList.objectList(Maximum._8, RoadName.forName("Shibo Boulevard")));
        roadShapeAndLength(testPolyline(), testPolyline().start(), testPolyline().end());
        roadState(TWO_WAY);
        roadSubType(RoadSubType.MAIN_ROAD);
        roadType(RoadType.HIGHWAY);
        speedLimit(Speed.HIGHWAY_SPEED);
        state(Country.UNITED_STATES.CALIFORNIA);
        surface(RoadSurface.PAVED);
        to(graph().newHeavyWeightVertex(new VertexIdentifier(5)));
        tollRoad(false);
        toVertexClipped(false);
        toLocation(Location.TELENAV_HEADQUARTERS.moved(Heading.EAST, Distance.meters(5)));
        type(Type.NORMAL);
        underConstruction(false);
        fromGradeSeparation(GradeSeparation.GROUND);
        toGradeSeparation(GradeSeparation.GROUND);
    }

    @Override
    public EdgeIdentifier rawIdentifier()
    {
        return rawIdentifier;
    }

    public void rawIdentifier(final EdgeIdentifier rawIdentifier)
    {
        this.rawIdentifier = rawIdentifier;
    }

    public void referenceSpeed(final Speed referenceSpeed)
    {
        this.referenceSpeed = referenceSpeed;
    }

    @Override
    public HeavyWeightEdge reversed()
    {
        final var reversed = graph().newHeavyWeightEdge(identifier());
        reversed.identifier(identifier().reversed().asLong());
        reversed.from(to());
        reversed.to(from());
        reversed.fromNodeIdentifier(toNodeIdentifier());
        reversed.toNodeIdentifier(fromNodeIdentifier());
        reversed.toVertexClipped(isFromVertexClipped());
        reversed.fromVertexClipped(isToVertexClipped());
        reversed.referenceSpeed(reverseReferenceSpeed);
        reversed.uniDbReverseReferenceSpeed(referenceSpeed);
        reversed.roadShape(roadShape().reversed());
        reversed.toGradeSeparation(fromGradeSeparation());
        reversed.fromGradeSeparation(toGradeSeparation());
        return reversed;
    }

    @Override
    public RoadFunctionalClass roadFunctionalClass()
    {
        assert roadFunctionalClass != null;
        return roadFunctionalClass;
    }

    public void roadFunctionalClass(final RoadFunctionalClass roadFunctionalClass)
    {
        assert roadFunctionalClass != null;
        this.roadFunctionalClass = roadFunctionalClass;
    }

    @Override
    public List<RoadName> roadNames(final RoadName.Type type)
    {
        var roadNames = this.roadNames.get(type);
        if (roadNames == null)
        {
            roadNames = Collections.emptyList();
        }
        return roadNames;
    }

    public void roadNames(final RoadName.Type type, final List<RoadName> roadNames)
    {
        assert roadNames.stream().noneMatch(Objects::isNull);
        this.roadNames.put(type, roadNames);
    }

    @Override
    @KivaKitExcludeProperty
    public Polyline roadShape()
    {
        if (roadShape == null)
        {
            return Polyline.fromLocations(fromLocation(), toLocation());
        }
        return roadShape;
    }

    public void roadShape(final Polyline roadShape)
    {
        this.roadShape = roadShape;
        if (roadShape != null)
        {
            bounds = roadShape.bounds();
        }
    }

    public void roadShapeAndLength(final Polyline shape, final Location from, final Location to)
    {
        if (shape != null)
        {
            roadShape(shape);
            fromLocation(shape.start());
            toLocation(shape.end());
            length(shape.length());
        }
        else
        {
            fromLocation(from);
            toLocation(to);
            length(from.distanceTo(to));
        }
    }

    @Override
    public RoadState roadState()
    {
        return roadState;
    }

    public void roadState(final RoadState roadState)
    {
        this.roadState = roadState;
    }

    @Override
    public RoadSubType roadSubType()
    {
        return roadSubType;
    }

    public void roadSubType(final RoadSubType roadSubType)
    {
        this.roadSubType = roadSubType;
    }

    @Override
    public RoadSurface roadSurface()
    {
        return surface;
    }

    @Override
    public RoadType roadType()
    {
        return roadType;
    }

    public void roadType(final RoadType roadType)
    {
        this.roadType = roadType;
    }

    @Override
    public Speed speedLimit()
    {
        return speedLimit;
    }

    public void speedLimit(final Speed speedLimit)
    {
        this.speedLimit = Functions.apply(speedLimit, speed -> speed.minimum(Speed.kilometersPerHour(160)));
    }

    @Override
    public State state()
    {
        return state;
    }

    public void state(final State state)
    {
        this.state = state;
    }

    public void surface(final RoadSurface surface)
    {
        this.surface = surface;
    }

    @Override
    public PbfTagList tagList()
    {
        return tags;
    }

    public void tags(final PbfTagList tags)
    {
        assert tags.isValid();
        this.tags = tags;
    }

    public Polyline testPolyline()
    {
        return Polyline.fromLocations(
                Location.TELENAV_HEADQUARTERS.moved(Heading.NORTHEAST, Distance._100_METERS),
                Location.TELENAV_HEADQUARTERS);
    }

    @Override
    public Vertex to()
    {
        return to;
    }

    public void to(final Vertex to)
    {
        this.to = to;
        if (to != null)
        {
            toLocation = to.location();
        }
    }

    @Override
    public GradeSeparation toGradeSeparation()
    {
        return toGradeSeparation;
    }

    public void toGradeSeparation(final GradeSeparation separation)
    {
        // Grade separation can legitimately be null here because we don't store the omnipresent
        // grade separation "GROUND"
        toGradeSeparation = separation == null ? GradeSeparation.GROUND : separation;
    }

    @Override
    public Location toLocation()
    {
        return toLocation;
    }

    public void toLocation(final Location toLocation)
    {
        assert toLocation != null;
        this.toLocation = toLocation;
    }

    @Override
    public MapNodeIdentifier toNodeIdentifier()
    {
        return toNodeIdentifier;
    }

    public void toNodeIdentifier(final MapNodeIdentifier identifier)
    {
        toNodeIdentifier = identifier;
    }

    public void toVertexClipped(final Boolean toVertexClipped)
    {
        this.toVertexClipped = toVertexClipped;
    }

    @Override
    @KivaKitExcludeProperty
    public VertexIdentifier toVertexIdentifier()
    {
        return to != null ? to.identifier() : null;
    }

    public void tollRoad(final Boolean isTollRoad)
    {
        this.isTollRoad = isTollRoad;
    }

    @Override
    public Duration travelTime()
    {
        return freeFlowSpeed().average().timeToTravel(length());
    }

    @Override
    public Type type()
    {
        return type;
    }

    public void type(final Type type)
    {
        this.type = type;
    }

    public void underConstruction(final Boolean isUnderConstruction)
    {
        this.isUnderConstruction = isUnderConstruction;
    }

    /**
     * @return The reference speed (used for historical speed calculation) in forward direction
     */
    public Speed uniDbReferenceSpeed()
    {
        return referenceSpeed;
    }

    /**
     * @return The reference speed (used for historical speed calculation) in backward direction
     */
    public Speed uniDbReverseReferenceSpeed()
    {
        return reverseReferenceSpeed;
    }

    public void uniDbReverseReferenceSpeed(final Speed reversedReferenceSpeed)
    {
        reverseReferenceSpeed = reversedReferenceSpeed;
    }
}
