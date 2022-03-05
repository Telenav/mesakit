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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.language.object.KivaKitFormatProperty;
import com.telenav.kivakit.core.language.reflection.property.KivaKitIncludeProperty;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.progress.ProgressReporter;
import com.telenav.kivakit.core.progress.reporters.BroadcastingProgressReporter;
import com.telenav.kivakit.core.string.AsIndentedString;
import com.telenav.kivakit.core.string.AsStringIndenter;
import com.telenav.kivakit.core.string.Strip;
import com.telenav.kivakit.core.value.count.Bytes;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.level.Percent;
import com.telenav.kivakit.core.value.mutable.MutableValue;
import com.telenav.kivakit.core.version.Version;
import com.telenav.kivakit.data.compression.codecs.huffman.character.HuffmanCharacterCodec;
import com.telenav.kivakit.data.compression.codecs.huffman.list.HuffmanStringListCodec;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.interfaces.naming.Named;
import com.telenav.kivakit.interfaces.string.Stringable;
import com.telenav.kivakit.primitive.collections.array.scalars.IntArray;
import com.telenav.kivakit.resource.Resource;
import com.telenav.kivakit.resource.compression.archive.ZipArchive;
import com.telenav.kivakit.resource.path.FileName;
import com.telenav.kivakit.resource.resources.other.PropertyMap;
import com.telenav.kivakit.validation.BaseValidator;
import com.telenav.kivakit.validation.Validatable;
import com.telenav.kivakit.validation.ValidationType;
import com.telenav.kivakit.validation.Validator;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.metadata.DataBuild;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.metadata.DataSupplier;
import com.telenav.mesakit.graph.metadata.DataVersion;
import com.telenav.mesakit.graph.project.GraphLimits;
import com.telenav.mesakit.graph.specifications.osm.OsmDataSpecification;
import com.telenav.mesakit.map.data.formats.library.DataFormat;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfEntity;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTags;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.compression.PbfDefaultCodecs;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.compression.PbfStringListTagCodec;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.compression.PbfTagCodec;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataSource;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfStopProcessingException;
import com.telenav.mesakit.map.data.formats.pbf.processing.readers.SerialPbfReader;
import com.telenav.mesakit.map.geography.Precision;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.ensure.Ensure.unsupported;
import static com.telenav.kivakit.data.compression.codecs.huffman.character.HuffmanCharacterCodec.ESCAPE;
import static com.telenav.mesakit.graph.Metadata.CountType.ALLOW_ESTIMATE;
import static com.telenav.mesakit.graph.Metadata.CountType.REQUIRE_EXACT;
import static com.telenav.mesakit.graph.metadata.DataSupplier.OSM;
import static com.telenav.mesakit.map.data.formats.library.DataFormat.PBF;

/**
 * Information about map data in a resource like a file.
 * <p>
 * Metadata in a {@link Resource} containing map data, can be extracted with {@link #from(File)}. The structure of
 * metadata in a PBF resource is described in the link above. Partial metadata can be obtained from a metadata
 * descriptor with {@link #parseDescriptor(String)} and the descriptor for a {@link Metadata} object can be retrieved
 * with {@link #descriptor()}.
 * <p>
 * Three categories of metadata are available:
 * <p>
 * <b>Data Description</b>
 * <ul>
 *     <li>{@link #name()}</li>
 *     <li>{@link #dataSpecification()}</li>
 *     <li>{@link #dataBounds()}</li>
 *     <li>{@link #dataBuild()}</li>
 *     <li>{@link #dataFormat()}</li>
 *     <li>{@link #dataPrecision()}</li>
 *     <li>{@link #dataSize()}</li>
 *     <li>{@link #dataSupplier()}</li>
 *     <li>{@link #dataVersion()}</li>
 *     <li>{@link #descriptor()}</li>
 * </ul>
 * <p>
 * <b>Source Data Statistics</b>
 * <ul>
 *     <li>{@link #nodeCount(CountType)}</li>
 *     <li>{@link #wayCount(CountType)}</li>
 *     <li>{@link #relationCount(CountType)}</li>
 * </ul>
 * <p>
 * <b>Graph Information</b>
 * <ul>
 *     <li>{@link #vertexCount(CountType)}</li>
 *     <li>{@link #edgeCount(CountType)}</li>
 *     <li>{@link #forwardEdgeCount(CountType)}</li>
 *     <li>{@link #edgeRelationCount(CountType)}</li>
 *     <li>{@link #placeCount(CountType)}</li>
 *     <li>{@link #shapePointCount(CountType)}</li>
 *     <li>{@link #graphElementCount(CountType)}</li>
 * </ul>
 * <p>
 * {@link Metadata} objects are {@link KryoSerializable} and implement {@link Stringable}. They are also
 * {@link Validatable} and can be validated with the {@link Validator} returned by {@link #validator(ValidationType)}.
 * If no validation of statistics is desired the {@link ValidationType} VALIDATE_EXCEPT_STATISTICS can be passed as the
 * validation type. For convenience, full validation can be performed with {@link #isValid(Listener)}.
 *
 * @author jonathanl (shibo)
 * @see DataSpecification
 * @see DataBuild
 * @see DataVersion
 * @see DataFormat
 * @see DataSupplier
 * @see Rectangle
 * @see Bytes
 * @see Precision
 * @see Count
 * @see Version
 * @see KryoSerializable
 * @see Validatable
 * @see Stringable
 */
@SuppressWarnings("DuplicateBranchesInSwitch")
public class Metadata implements Named, AsIndentedString, KryoSerializable, Validatable
{
    public static final ValidationType VALIDATE_EXCEPT_STATISTICS = new ValidationType("VALIDATE_EXCEPT_STATISTICS");

    private static final Logger LOGGER = LoggerFactory.newLogger();

    /**
     * Defaults that can be used in testing to fill in missing values that don't matter for the test
     */
    public static Metadata defaultMetadata()
    {
        return new Metadata()
                .withDataBounds(Rectangle.MAXIMUM)
                .withDataBuild(DataBuild.now())
                .withDataPrecision(Precision.DM6)
                .withDataSize(Bytes.megabytes(1))
                .withDataSpecification(OsmDataSpecification.get())
                .withDataSupplier(OSM)
                .withDataFormat(PBF)
                .withName("default")
                .withCodecFrequencies(PbfDefaultCodecs.get().keyCharacterCodecFrequencies(),
                        PbfDefaultCodecs.get().keyStringCodecFrequencies(),
                        PbfDefaultCodecs.get().valueCharacterCodecFrequencies(),
                        PbfDefaultCodecs.get().valueStringCodecFrequencies(),
                        PbfDefaultCodecs.get().roadNameCharacterCodecFrequencies());
    }

    /**
     * @return Metadata from the given input file, allowing for an estimate of entities based on file size
     */
    public static Metadata from(File input)
    {
        return from(input, ALLOW_ESTIMATE);
    }

    /**
     * @return Complete metadata read for the given input resource. For resources that don't support all of the
     * statistics values, those values may be estimates based on the size of the resource.
     */
    public static Metadata from(File input, CountType countType)
    {
        input = input.materialized(BroadcastingProgressReporter.create(LOGGER));
        var format = DataFormat.of(input);
        switch (format)
        {
            case Graph:
                try (var archive = new GraphArchive(LOGGER, input, ZipArchive.Mode.READ, ProgressReporter.none()))
                {
                    return archive.metadata();
                }

            case CSV:
                return unsupported();

            case XML:
                return unsupported();

            case PBF:
                return extractPbfMetadata(input);

            default:
                ensure(false);
                return null;
        }
    }

    public static SwitchParser.Builder<Metadata> metadataSwitchParser()
    {
        return metadataSwitchParser("metadata", "The graph meta data to use such as OSM-OSM-PBF-North_America-2007Q4-2020.04.01_04.01PM_PT");
    }

    public static SwitchParser.Builder<Metadata> metadataSwitchParser(String name, String description)
    {
        return SwitchParser.builder(Metadata.class)
                .name(name)
                .converter(new Converter(LOGGER))
                .description(description);
    }

    public static Metadata of(DataSpecification specification, DataSupplier supplier,
                              DataFormat format)
    {
        return defaultMetadata()
                .withDataSpecification(specification)
                .withDataSupplier(supplier)
                .withDataFormat(format)
                .withName(specification.objectName() + "-" + supplier.name() + "-" + format);
    }

    public static Metadata osm(DataSupplier supplier, DataFormat format)
    {
        return of(OsmDataSpecification.get(), supplier, format);
    }

    /**
     * @return Metadata from the given filename
     */
    public static Metadata parse(FileName name)
    {
        return parse(name.base().name());
    }

    /**
     * @return Metadata from the name of the given folder
     */
    public static Metadata parse(Folder folder)
    {
        return parse(folder.name());
    }

    /**
     * Parses a complete standardized metadata string of the form:
     * <ul>
     *     [data-supplier]-[data-specification]-[data-format]-[name]-[bounds]?-[data-version]?-[data-build]?.
     * </ul>
     * <ul>
     *     <li>HERE-UniDb-PBF-North_America-2007Q4-2020.04.01_04.01PM_PT</li>
     *     <li>OSM-OSM-PBF-Seattle-47.5840818_-122.2169381_47.6419581_-122.1590618</li>
     * </ul>
     * The data version and build are optional.
     */
    public static Metadata parse(String value)
    {
        value = Strip.trailing(value, ".world");

        Pattern pattern = Pattern.compile
                (
                        "(?<supplier>[A-Za-z]+)"
                                + "-"
                                + "(?<specification>[A-Za-z]+)"
                                + "-"
                                + "(?<format>[A-Za-z]+)"
                                + "-"
                                + "(?<name>[A-Za-z_]+)"
                                + "(-(?<bounds>[-0-9.]+_[-0-9.]+_[-0-9.]+_[-0-9.]+))?"
                                + "(-(?<version>20\\d\\dQ[1-4]))?"
                                + "(-(?<build>(20\\d\\d\\.\\d\\d\\.\\d\\d_\\d{1,2}\\.\\d\\d[AP]M_[A-Z]{1,3})))?"
                );

        var matcher = pattern.matcher(value);
        if (matcher.matches())
        {
            var metadata = new Metadata()
                    .withDataSupplier(DataSupplier.valueOf(matcher.group("supplier")))
                    .withDataSpecification(DataSpecification.parse(matcher.group("specification")))
                    .withDataFormat(DataFormat.valueOf(matcher.group("format")))
                    .withName(matcher.group("name"));

            var dataBounds = matcher.group("bounds");
            var dataVersion = matcher.group("version");
            var dataBuild = matcher.group("build");
            if (dataBounds != null)
            {
                metadata = metadata.withDataBounds(Rectangle.parse(dataBounds));
            }
            if (dataVersion != null)
            {
                metadata = metadata.withDataVersion(DataVersion.parse(dataVersion));
            }
            if (dataBuild != null)
            {
                metadata = metadata.withDataBuild(DataBuild.parse(dataBuild));
            }

            return metadata;
        }
        return null;
    }

    /**
     * Parses a descriptor of the form:
     * <p>
     * [supplier]-[specification]-[format]-[name]-[version]?
     * <p>
     * For example:
     * <p>
     * HERE-UniDb-PBF-North_America-2020Q1
     *
     * @return Partial metadata for the descriptor
     */
    public static Metadata parseDescriptor(String value)
    {
        if (value != null)
        {
            var parts = value.split("-");
            if (parts.length == 4 || parts.length == 5)
            {
                try
                {
                    var metadata = new Metadata()
                            .withDataSupplier(DataSupplier.valueOf(parts[0]))
                            .withDataSpecification(DataSpecification.parse(parts[1]))
                            .withDataFormat(DataFormat.valueOf(parts[2]))
                            .withName(parts[3]);
                    if (parts.length == 5)
                    {
                        return metadata.withDataVersion(DataVersion.parse(parts[4]));
                    }
                    return metadata;
                }
                catch (Exception ignored)
                {
                }
            }
        }
        return null;
    }

    public enum CountType
    {
        ALLOW_ESTIMATE,
        REQUIRE_EXACT
    }

    /**
     * HERE-UniDb-PBF-North_America-2007Q4-2020.04.01_04.01PM_PT
     */
    public static class Converter extends BaseStringConverter<Metadata>
    {
        public Converter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected Metadata onToValue(String value)
        {
            return parse(value);
        }
    }

    /** Bounds of data */
    private Rectangle dataBounds;

    /** Data build */
    private DataBuild dataBuild;

    /** StringFormat of data, like PBF or CSV */
    private DataFormat dataFormat;

    /** The precision of the data like DM5, DM6 or DM7 */
    private Precision dataPrecision;

    /** The size of the resource */
    private Bytes dataSize;

    /** The specification for the data */
    private DataSpecification dataSpecification;

    /** The supplier of data */
    private DataSupplier dataSupplier;

    /** Data version */
    private DataVersion dataVersion;

    /** Number of edge relations */
    private Count edgeRelations;

    /** Number of edges in the data */
    private Count edges;

    /** Number of forward edges in the data */
    private Count forwardEdges;

    /** Frequencies for compressing keys by character */
    private PropertyMap keyCharacterCodecFrequencies;

    /** Frequencies for compressing keys */
    private PropertyMap keyStringCodecFrequencies;

    /** The name of the graph data */
    private String name;

    /** Number of nodes in the data */
    private Count nodes;

    /** Number of places in the data */
    private Count places;

    /**
     * PropertyMap defined by a {@link DataSpecification}, {@link DataFormat} or {@link DataSupplier}. For example, the
     * presence of {@link WayNode} location data in a PBF file is signaled by the property
     * WayNode.METADATA_KEY_LOCATION_INCLUDED, which is reused as a {@link Metadata }property key.
     */
    private final Map<String, String> properties = new HashMap<>();

    /** Number of relations in the data */
    private Count relations;

    /** Codec for compressing road names */
    private HuffmanCharacterCodec roadNameCharacterCodec;

    /** Codec frequencies for road names */
    private PropertyMap roadNameCharacterCodecFrequencies;

    /**
     * Number of shape points in the data. Shape points are nodes between vertexes. Graph files do not normally have
     * shape points. This feature is only used when graph files contain all node information, such as when editing OSM
     * data for upload with tools like Cygnus.
     */
    private Count shapePoints = Count._0;

    /** Tag codec */
    private PbfTagCodec tagCodec;

    /** Frequencies for compressing value by character */
    private PropertyMap valueCharacterCodecFrequencies;

    /** Frequencies for compressing values */
    private PropertyMap valueStringCodecFrequencies;

    /** Number of vertexes in the data */
    private Count vertexes;

    /** Number of ways in the data */
    private Count ways;

    public Metadata()
    {
    }

    private Metadata(Metadata that)
    {
        name = that.name;

        dataSize = that.dataSize;
        dataBuild = that.dataBuild;
        dataVersion = that.dataVersion;
        dataFormat = that.dataFormat;
        dataPrecision = that.dataPrecision;
        dataSupplier = that.dataSupplier;
        dataSpecification = that.dataSpecification;
        dataBounds = that.dataBounds;

        nodes = that.nodes;
        ways = that.ways;
        relations = that.relations;

        vertexes = that.vertexes;
        edges = that.edges;
        forwardEdges = that.forwardEdges;
        edgeRelations = that.edgeRelations;
        places = that.places;
        shapePoints = that.shapePoints;

        tagCodec = that.tagCodec;
        roadNameCharacterCodec = that.roadNameCharacterCodec;

        keyCharacterCodecFrequencies = that.keyCharacterCodecFrequencies;
        keyStringCodecFrequencies = that.keyStringCodecFrequencies;

        valueCharacterCodecFrequencies = that.valueCharacterCodecFrequencies;
        valueStringCodecFrequencies = that.valueStringCodecFrequencies;

        roadNameCharacterCodecFrequencies = that.roadNameCharacterCodecFrequencies;
    }

    public Metadata add(Metadata that)
    {
        return withNodeCount(nodeCount(REQUIRE_EXACT).plus(that.nodeCount(REQUIRE_EXACT)))
                .withWayCount(wayCount(REQUIRE_EXACT).plus(that.wayCount(REQUIRE_EXACT)))
                .withRelationCount(relationCount(REQUIRE_EXACT).plus(that.relations))
                .withVertexCount(vertexCount(REQUIRE_EXACT).plus(that.vertexCount(REQUIRE_EXACT)))
                .withEdgeCount(edgeCount(REQUIRE_EXACT).plus(that.edgeCount(REQUIRE_EXACT)))
                .withForwardEdgeCount(forwardEdgeCount(REQUIRE_EXACT).plus(that.forwardEdgeCount(REQUIRE_EXACT)))
                .withEdgeRelationCount(edgeRelationCount(REQUIRE_EXACT).plus(that.edgeRelationCount(REQUIRE_EXACT)))
                .withShapePointCount(shapePointCount(REQUIRE_EXACT).plus(that.shapePointCount(REQUIRE_EXACT)))
                .withDataSize(dataSize().add(that.dataSize()));
    }

    /**
     * A filename for this metadata of the form:
     * <ul>
     *     [supplier]-[specification]-[format]-[name]-[version]-[build].
     * </ul>
     * <ul>
     *     HERE-UniDb-PBF-North_America-2007Q4-2020.04.01_04.01PM_PT
     * </ul>
     * The data version and build are optional.
     *
     * @return A filename for this metadata
     */
    public FileName asFileName()
    {
        return FileName.parse(LOGGER, dataSupplier
                + "-" + dataSpecification
                + "-" + dataFormat
                + "-" + FileName.parse(LOGGER, name.replaceAll("-", "_")).normalized()
                + (dataVersion == null ? "" : "-" + dataVersion)
                + (dataBuild == null ? "" : "-" + dataBuild.asFileName()));
    }

    /**
     * @return This metadata as a folder
     * @see #asFileName()
     */
    public Folder asFolder()
    {
        return Folder.from(asFileName());
    }

    @Override
    public AsStringIndenter asString(Stringable.Format format, AsStringIndenter indenter)
    {
        indenter.asString(this);
        indenter.labeled("nodes", nodeCount(REQUIRE_EXACT));
        indenter.labeled("ways", wayCount(REQUIRE_EXACT));
        indenter.labeled("relations", relationCount(REQUIRE_EXACT));

        if (edges != null)
        {
            indenter.labeled("vertexes", vertexCount(REQUIRE_EXACT));
            indenter.labeled("edges", edgeCount(REQUIRE_EXACT));
            indenter.labeled("forwardEdges", forwardEdgeCount(REQUIRE_EXACT));
            indenter.labeled("edgeRelations", edgeRelationCount(REQUIRE_EXACT));
            indenter.labeled("places", placeCount(REQUIRE_EXACT));
        }
        return indenter;
    }

    public Metadata assertValid(ValidationType type)
    {
        ensure(validator(type).validate(LOGGER));
        return this;
    }

    public void configure(PbfDataSource reader)
    {
        reader.expectedNodes(nodeCount(ALLOW_ESTIMATE));
        reader.expectedWays(wayCount(ALLOW_ESTIMATE));
        reader.expectedRelations(relationCount(ALLOW_ESTIMATE));
    }

    public Count count(CountType type, Class<? extends GraphElement> element)
    {
        if (element.isAssignableFrom(Edge.class))
        {
            return edgeCount(type);
        }
        if (element.isAssignableFrom(Vertex.class))
        {
            return vertexCount(type);
        }
        if (element.isAssignableFrom(EdgeRelation.class))
        {
            // Estimation of edge relations is too inaccurate based on metadata due to filtering
            return Count._131_072;
        }
        if (element.isAssignableFrom(Place.class))
        {
            return placeCount(type);
        }
        return null;
    }

    /**
     * @return Bounds encompassing all data entities
     */
    @KivaKitIncludeProperty
    public Rectangle dataBounds()
    {
        return dataBounds;
    }

    /**
     * @return The version of the data in time
     */
    @KivaKitIncludeProperty
    public DataBuild dataBuild()
    {
        return dataBuild;
    }

    /**
     * @return The {@link DataFormat} that the data comes from
     */
    @KivaKitIncludeProperty
    public DataFormat dataFormat()
    {
        return dataFormat;
    }

    @KivaKitIncludeProperty
    public Precision dataPrecision()
    {
        return dataPrecision;
    }

    /**
     * @return The size of this data
     */
    @KivaKitIncludeProperty
    public Bytes dataSize()
    {
        return dataSize;
    }

    /**
     * @return The data's specification
     */
    @KivaKitIncludeProperty
    @KivaKitFormatProperty
    public DataSpecification dataSpecification()
    {
        return dataSpecification;
    }

    /**
     * @return The supplier of the data like HERE or OSM
     */
    @KivaKitIncludeProperty
    public DataSupplier dataSupplier()
    {
        return dataSupplier;
    }

    /**
     * @return The version of this data
     */
    public DataVersion dataVersion()
    {
        return dataVersion;
    }

    /**
     * A descriptor of the data content (independent of its format or build time). A descriptor takes the form:
     * <p>
     * [supplier]-[specification]-[format]-[name]-[version]. For example:
     * <p>
     * HERE-UniDb-PBF-North_America-2020Q1
     */
    @KivaKitIncludeProperty
    public String descriptor()
    {
        return dataSupplier
                + "-" + dataSpecification
                + "-" + dataFormat
                + "-" + name
                + (dataVersion == null ? "" : "-" + dataVersion);
    }

    /**
     * @return The number of edges
     */
    public Count edgeCount(CountType type)
    {
        if (type == ALLOW_ESTIMATE && edges == null)
        {
            return wayCount(ALLOW_ESTIMATE).times(0.5).asEstimate().ceiling(3);
        }
        return edges;
    }

    /**
     * @return The exact or estimated number of edge relations
     */
    public Count edgeRelationCount(CountType type)
    {
        if (type == ALLOW_ESTIMATE && edgeRelations == null && dataSize() != null)
        {
            // Empirical value from OSM data analysis
            return dataSize().percent(Percent.of(0.25)).asEstimate().ceiling(3);
        }
        return edgeRelations;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof Metadata)
        {
            var that = (Metadata) object;
            return descriptor().equals(that.descriptor());
        }
        return false;
    }

    /**
     * @return The number of forward edges (without considering bi-directional edges)
     */
    public Count forwardEdgeCount(CountType type)
    {
        if (type == ALLOW_ESTIMATE && forwardEdges == null)
        {
            return wayCount(ALLOW_ESTIMATE).times(GraphLimits.Estimated.EDGES_PER_WAY).asEstimate().ceiling(3);
        }
        return forwardEdges;
    }

    /**
     * @return The total number of edges, vertexes and relations in this data
     */
    public Count graphElementCount(CountType type)
    {
        if (edgeCount(type) != null && vertexCount(type) != null && edgeRelationCount(type) != null)
        {
            var count = edgeCount(type).plus(vertexCount(type)).plus(edgeRelationCount(type));
            return type == ALLOW_ESTIMATE ? count.asEstimate().ceiling(3) : count;
        }
        return null;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(descriptor());
    }

    public boolean isOsm()
    {
        return dataSpecification().isOsm();
    }

    public boolean isUniDb()
    {
        return dataSpecification().isUniDb();
    }

    /**
     * The common name of this data
     */
    @Override
    @KivaKitIncludeProperty
    public String name()
    {
        return name;
    }

    /**
     * @return Creates a new graph based on this metadata
     */
    public Graph newGraph()
    {
        return dataSpecification.newGraph(this);
    }

    /**
     * @return The exact or estimated (OSM) number of nodes in OSM data. Not relevant to UniDb.
     */
    public Count nodeCount(CountType type)
    {
        if (type == ALLOW_ESTIMATE && nodes == null && dataSize() != null)
        {
            // Empirical value from OSM data analysis
            return dataSize().percent(Percent.of(12.5)).asEstimate().ceiling(3);
        }
        return nodes;
    }

    public Count placeCount(CountType type)
    {
        if (type == ALLOW_ESTIMATE && places == null)
        {
            return nodeCount(ALLOW_ESTIMATE).percent(Percent.of(0.01)).asEstimate().ceiling(3);
        }
        return places;
    }

    public String property(String key)
    {
        return properties.get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Kryo kryo, Input input)
    {
        name = kryo.readObject(input, String.class);
        nodes = kryo.readObject(input, Count.class);
        ways = kryo.readObject(input, Count.class);
        relations = kryo.readObject(input, Count.class);
        vertexes = kryo.readObject(input, Count.class);
        edges = kryo.readObject(input, Count.class);
        forwardEdges = kryo.readObject(input, Count.class);
        edgeRelations = kryo.readObject(input, Count.class);
        places = kryo.readObject(input, Count.class);
        shapePoints = kryo.readObject(input, Count.class);

        dataSize = kryo.readObject(input, Bytes.class);
        dataBounds = kryo.readObject(input, Rectangle.class);
        dataBuild = kryo.readObject(input, DataBuild.class);
        dataVersion = kryo.readObjectOrNull(input, DataVersion.class);
        dataPrecision = kryo.readObject(input, Precision.class);
        dataFormat = kryo.readObject(input, DataFormat.class);
        dataSupplier = kryo.readObject(input, DataSupplier.class);

        // Read the data specification class name
        var dataSpecificationClassName = kryo.readObject(input, String.class);
        try
        {
            // load the specification class
            var specification = (Class<? extends DataSpecification>) Class.forName(dataSpecificationClassName);

            // and get the DataSpecification by calling the get() method
            var get = specification.getMethod("get");
            dataSpecification = (DataSpecification) get.invoke(null);

            var supportedAttributes = kryo.readObject(input, IntArray.class);
            dataSpecification.supportedAttributes(supportedAttributes);
        }
        catch (Exception e)
        {
            LOGGER.problem(e, "Unable to read data specification");
        }

        keyCharacterCodecFrequencies = kryo.readObject(input, PropertyMap.class);
        keyStringCodecFrequencies = kryo.readObject(input, PropertyMap.class);
        valueCharacterCodecFrequencies = kryo.readObject(input, PropertyMap.class);
        valueStringCodecFrequencies = kryo.readObject(input, PropertyMap.class);
        roadNameCharacterCodecFrequencies = kryo.readObject(input, PropertyMap.class);
    }

    /**
     * @return The exact or estimated number of relations
     */
    public Count relationCount(CountType type)
    {
        if (type == ALLOW_ESTIMATE && relations == null && dataSize() != null)
        {
            // Empirical value from OSM data analysis
            return dataSize().percent(Percent.of(0.25)).asEstimate().ceiling(3);
        }
        return relations;
    }

    public HuffmanCharacterCodec roadNameCharacterCodec()
    {
        if (roadNameCharacterCodec == null)
        {
            roadNameCharacterCodec = HuffmanCharacterCodec.from(Listener.throwing(), roadNameCharacterCodecFrequencies, ESCAPE);
        }
        return roadNameCharacterCodec;
    }

    public Count shapePointCount(CountType type)
    {
        return shapePoints;
    }

    public PbfTagCodec tagCodec()
    {
        if (tagCodec == null)
        {
            var keyCharacterCodec = PbfDefaultCodecs.get().defaultKeyCharacterCodec();
            var keyStringCodec = PbfDefaultCodecs.get().defaultKeyStringCodec();

            var valueCharacterCodec = PbfDefaultCodecs.get().defaultValueCharacterCodec();
            var valueStringCodec = PbfDefaultCodecs.get().defaultValueStringCodec();

            var keyStringListCodec = new HuffmanStringListCodec(keyStringCodec, keyCharacterCodec);
            var valueStringListCodec = new HuffmanStringListCodec(valueStringCodec, valueCharacterCodec);

            tagCodec = new PbfStringListTagCodec(keyStringListCodec, valueStringListCodec);
        }

        return tagCodec;
    }

    @Override
    public String toString()
    {
        return asString();
    }

    @Override
    public Validator validator(ValidationType type)
    {
        return new BaseValidator()
        {
            @Override
            protected void onValidate()
            {
                problemIf(name() == null, "Name is missing");
                problemIf(dataSize() == null, "Data size is missing");
                problemIf(dataSpecification() == null, "Data specification is missing");
                problemIf(dataBuild() == null, "Data build is missing");
                problemIf(dataFormat() == null, "Data format is missing");
                problemIf(dataSupplier() == null, "Data supplier is missing");

                if (type == ValidationType.VALIDATE_ALL)
                {
                    problemIf(dataBounds() == null, "Data bounds is missing");
                    problemIf(isZero(nodeCount(REQUIRE_EXACT)), "No nodes");
                    problemIf(isZero(wayCount(REQUIRE_EXACT)), "No ways");
                    problemIf(isZero(vertexCount(REQUIRE_EXACT)), "No vertexes");
                    problemIf(isZero(forwardEdgeCount(REQUIRE_EXACT)), "No forward edges");
                    problemIf(isZero(edgeCount(REQUIRE_EXACT)), "No edges");

                    // Relations and places can have a zero count, but the count cannot be missing
                    problemIf(relationCount(REQUIRE_EXACT) == null, "No relations");
                    problemIf(edgeRelationCount(REQUIRE_EXACT) == null, "No edge relations");
                    problemIf(placeCount(REQUIRE_EXACT) == null, "No places");
                }
            }
        };
    }

    /**
     * @return The exact (UniDb) or estimated (OSM) number of vertexes
     */
    public Count vertexCount(CountType type)
    {
        if (type == ALLOW_ESTIMATE && vertexes == null)
        {
            return edgeCount(ALLOW_ESTIMATE).asEstimate().ceiling(3);
        }
        return vertexes;
    }

    /**
     * @return The exact or estimated (OSM) number of ways in this data. Not relevant to UniDb.
     */
    public Count wayCount(CountType type)
    {
        if (type == ALLOW_ESTIMATE && ways == null && dataSize() != null)
        {
            // Empirical value for navigable ways from OSM data analysis
            return dataSize().percent(Percent.of(0.5)).asEstimate().ceiling(3);
        }
        return ways;
    }

    public Metadata withCodecFrequencies(PropertyMap keyCharacterCodecFrequencies,
                                         PropertyMap keyStringCodecFrequencies,
                                         PropertyMap valueCharacterCodecFrequencies,
                                         PropertyMap valueStringCodecFrequencies,
                                         PropertyMap roadNameCharacterCodecFrequencies)
    {
        var copy = new Metadata(this);

        copy.keyCharacterCodecFrequencies = keyCharacterCodecFrequencies;
        copy.keyStringCodecFrequencies = keyStringCodecFrequencies;
        copy.valueCharacterCodecFrequencies = valueCharacterCodecFrequencies;
        copy.valueStringCodecFrequencies = valueStringCodecFrequencies;
        copy.roadNameCharacterCodecFrequencies = roadNameCharacterCodecFrequencies;

        return copy;
    }

    public Metadata withDataBounds(Rectangle bounds)
    {
        var copy = new Metadata(this);
        copy.dataBounds = bounds;
        return copy;
    }

    public Metadata withDataBuild(DataBuild build)
    {
        var copy = new Metadata(this);
        copy.dataBuild = build;
        return copy;
    }

    public Metadata withDataFormat(DataFormat format)
    {
        var copy = new Metadata(this);
        copy.dataFormat = format;
        return copy;
    }

    public Metadata withDataPrecision(Precision precision)
    {
        var copy = new Metadata(this);
        copy.dataPrecision = precision;
        return copy;
    }

    public Metadata withDataSize(Bytes dataSize)
    {
        var copy = new Metadata(this);
        copy.dataSize = dataSize;
        return copy;
    }

    public Metadata withDataSpecification(DataSpecification specification)
    {
        var copy = new Metadata(this);
        copy.dataSpecification = specification;
        return copy;
    }

    public Metadata withDataSupplier(DataSupplier supplier)
    {
        var copy = new Metadata(this);
        copy.dataSupplier = supplier;
        return copy;
    }

    public Metadata withDataVersion(DataVersion version)
    {
        var copy = new Metadata(this);
        copy.dataVersion = version;
        return copy;
    }

    public Metadata withEdgeCount(Count edges)
    {
        var copy = new Metadata(this);
        copy.edges = edges;
        return copy;
    }

    public Metadata withEdgeRelationCount(Count edgeRelations)
    {
        var copy = new Metadata(this);
        copy.edgeRelations = edgeRelations;
        return copy;
    }

    public Metadata withForwardEdgeCount(Count forwardEdges)
    {
        var copy = new Metadata(this);
        copy.forwardEdges = forwardEdges;
        return copy;
    }

    public Metadata withMetadata(Metadata metadata)
    {
        var copy = new Metadata(this);

        if (copy.name == null)
        {
            copy.name = metadata.name;
        }
        if (copy.dataBounds == null)
        {
            copy.dataBounds = metadata.dataBounds;
        }
        if (copy.dataBuild == null)
        {
            copy.dataBuild = metadata.dataBuild;
        }
        if (copy.dataVersion == null)
        {
            copy.dataVersion = metadata.dataVersion;
        }
        if (copy.dataFormat == null)
        {
            copy.dataFormat = metadata.dataFormat;
        }
        if (copy.dataPrecision == null)
        {
            copy.dataPrecision = metadata.dataPrecision;
        }
        if (copy.dataSize == null)
        {
            copy.dataSize = metadata.dataSize;
        }
        if (copy.dataSpecification == null)
        {
            copy.dataSpecification = metadata.dataSpecification;
        }
        if (copy.dataSupplier == null)
        {
            copy.dataSupplier = metadata.dataSupplier;
        }

        if (copy.nodes == null)
        {
            copy.nodes = metadata.nodes;
        }
        if (copy.ways == null)
        {
            copy.ways = metadata.ways;
        }
        if (copy.relations == null)
        {
            copy.relations = metadata.relations;
        }

        if (copy.vertexes == null)
        {
            copy.vertexes = metadata.vertexes;
        }
        if (copy.edges == null)
        {
            copy.edges = metadata.edges;
        }
        if (copy.forwardEdges == null)
        {
            copy.forwardEdges = metadata.forwardEdges;
        }
        if (copy.edgeRelations == null)
        {
            copy.edgeRelations = metadata.edgeRelations;
        }

        if (copy.shapePoints == null)
        {
            copy.shapePoints = metadata.shapePoints;
        }

        return copy;
    }

    public Metadata withName(String name)
    {
        var copy = new Metadata(this);
        copy.name = name;
        return copy;
    }

    public Metadata withNodeCount(Count nodes)
    {
        var copy = new Metadata(this);
        copy.nodes = nodes;
        return copy;
    }

    public Metadata withPlaceCount(Count places)
    {
        var copy = new Metadata(this);
        copy.places = places;
        return copy;
    }

    public Metadata withProperties(Map<String, String> properties)
    {
        var copy = new Metadata(this);
        copy.properties.putAll(properties);
        return copy;
    }

    public Metadata withRelationCount(Count relations)
    {
        var copy = new Metadata(this);
        copy.relations = relations;
        return copy;
    }

    public Metadata withShapePointCount(Count shapePoints)
    {
        var copy = new Metadata(this);
        copy.shapePoints = shapePoints;
        return copy;
    }

    public Metadata withVertexCount(Count vertexes)
    {
        var copy = new Metadata(this);
        copy.vertexes = vertexes;
        return copy;
    }

    public Metadata withWayCount(Count ways)
    {
        var copy = new Metadata(this);
        copy.ways = ways;
        return copy;
    }

    @Override
    public void write(Kryo kryo, Output output)
    {
        kryo.writeObject(output, name());
        kryo.writeObject(output, nodes);
        kryo.writeObject(output, ways);
        kryo.writeObject(output, relations);
        kryo.writeObject(output, vertexes);
        kryo.writeObject(output, edges);
        kryo.writeObject(output, forwardEdges);
        kryo.writeObject(output, edgeRelations);
        kryo.writeObject(output, places);
        kryo.writeObject(output, shapePoints);

        kryo.writeObject(output, dataSize());
        kryo.writeObject(output, dataBounds());
        kryo.writeObject(output, dataBuild());
        kryo.writeObjectOrNull(output, dataVersion(), DataVersion.class);
        kryo.writeObject(output, dataPrecision());
        kryo.writeObject(output, dataFormat());
        kryo.writeObject(output, dataSupplier());
        kryo.writeObject(output, dataSpecification().getClass().getName());
        kryo.writeObject(output, dataSpecification().supportedAttributes());

        kryo.writeObject(output, keyCharacterCodecFrequencies);
        kryo.writeObject(output, keyStringCodecFrequencies);
        kryo.writeObject(output, valueCharacterCodecFrequencies);
        kryo.writeObject(output, valueStringCodecFrequencies);
        kryo.writeObject(output, roadNameCharacterCodecFrequencies);
    }

    private static Metadata extractPbfMetadata(File file)
    {
        var reader = new SerialPbfReader(file);
        reader.silence();
        var metadata = new MutableValue<Metadata>();
        try
        {
            reader.process(new PbfDataProcessor()
            {
                @Override
                public Action onNode(PbfNode node)
                {
                    metadata.set(metadata(node));
                    throw new PbfStopProcessingException();
                }
            });
        }
        catch (Exception ignored)
        {
        }
        return metadata.get();
    }

    private static Metadata metadata(PbfEntity<?> entity)
    {
        var tags = entity.tagMap();

        // [supplier]-[specification]-[format]-[name]-[version]?
        var descriptor = tags.get("telenav-data-descriptor");

        var build = tags.get("telenav-data-build");
        var size = Bytes.parseBytes(Listener.none(), tags.get("telenav-data-size"));
        var precision = tags.get("telenav-data-precision");
        var bounds = tags.get("telenav-data-bounds");
        var nodes = tags.get("telenav-data-nodes");
        var ways = tags.get("telenav-data-ways");
        var relations = tags.get("telenav-data-relations");

        if (build != null && size != null && bounds != null && nodes != null && ways != null && relations != null)
        {
            var keyCharacterCodecFrequencies = PbfTags.tags("telenav-key-character-codec-", entity.get().getTags());
            var keyStringCodecFrequencies = PbfTags.tags("telenav-key-string-codec-", entity.get().getTags());
            var valueCharacterCodecFrequencies = PbfTags.tags("telenav-value-character-codec-", entity.get().getTags());
            var valueStringCodecFrequencies = PbfTags.tags("telenav-value-string-codec-", entity.get().getTags());
            var roadNameCharacterCodecFrequencies = PbfTags.tags("telenav-road-name-character-codec-", entity.get().getTags());

            if (!keyCharacterCodecFrequencies.isEmpty()
                    && !keyStringCodecFrequencies.isEmpty()
                    && !valueCharacterCodecFrequencies.isEmpty()
                    && !valueStringCodecFrequencies.isEmpty()
                    && !roadNameCharacterCodecFrequencies.isEmpty())
            {
                var metadata = parseDescriptor(descriptor);
                if (metadata != null)
                {
                    return metadata.withDataBuild(DataBuild.parse(build))
                            .withDataSize(size)
                            .withDataPrecision(Precision.valueOf(precision))
                            .withDataBounds(Rectangle.parse(bounds))
                            .withNodeCount(Count.parseCount(LOGGER, nodes))
                            .withWayCount(Count.parseCount(LOGGER, ways))
                            .withRelationCount(Count.parseCount(LOGGER, relations))
                            .withCodecFrequencies(keyCharacterCodecFrequencies,
                                    keyStringCodecFrequencies,
                                    valueCharacterCodecFrequencies,
                                    valueStringCodecFrequencies,
                                    roadNameCharacterCodecFrequencies);
                }
                else
                {
                    LOGGER.warning("Input file metadata has an invalid data metadata descriptor. Re-process the file with the pbf metadata application.");
                }
            }
            else
            {
                LOGGER.warning("Input file metadata is missing codec information. Re-process the file with the pbf metadata application.");
            }
        }
        else
        {
            LOGGER.warning("Input file has not been pre-processed with the pbf metadata application.");
        }
        return null;
    }
}
