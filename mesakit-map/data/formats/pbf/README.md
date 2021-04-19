# mesakit-map-data-formats pbf &nbsp;&nbsp;<img src="https://www.kivakit.org/images/bits-32.png" srcset="https://www.kivakit.org/images/bits-32-2x.png 2x"></img>

This module contains packages for processing data in OpenStreetMap PBF format.

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512-2x.png 2x"></img>

### Index

[**Summary**](#summary)
[**Modeling**](#modeling)
[**Reading Data**](#reading-data)
[**Writing Data**](#writing-data)

[**Dependencies**](#dependencies) | [**Class Diagrams**](#class-diagrams) | [**Package Diagrams**](#package-diagrams) | [**Javadoc**](#javadoc)

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512-2x.png 2x"></img>

### Dependencies <a name="dependencies"></a> &nbsp;&nbsp; <img src="https://www.kivakit.org/images/dependencies-32.png" srcset="https://www.kivakit.org/images/dependencies-32-2x.png 2x"></img>

[*Dependency Diagram*](documentation/diagrams/dependencies.svg)

#### Maven Dependency

    <dependency>
        <groupId>com.telenav.mesakit</groupId>
        <artifactId></artifactId>
        <version>0.9.0-SNAPSHOTc</version>
    </dependency>

<img src="https://www.kivakit.org/images/short-horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"></img>

[//]: # (start-user-text)

### Summary <a name = "summary"></a>

This module provides classes that model OSM and Telenav UniDb map data in PBF format.
The *model* package contains classes for node, way and relation entities and identifiers, as
well as entity metadata. The *tags* package supplies efficient data structures for storing
and retrieving the *Tag* key/value pairs that can be associated with all entities. The *processing*
package provides data processing classes in the form of PBF readers and writers. Finally,
the *osm* package contains OSM-specific utility methods, and the *unidb* package does the
same for UniDb data.

### Modeling <a name = "modeling"></a>

The [*PbfEntity*](documentation/diagrams/diagram-pbf-model-entities.svg) class serves as a base class for *PbfNode*, *PbfWay* and *
PbfRelation*, providing
easy access to entity metadata, most particularly key/value pair *Tag*s. Compression of tags
is accomplished through use of the *Huffman* codec in the [*kivakit-data-compression*](../../compression/README.md) project. Other
metadata is supported with *PbfChangeSetIdentifier*, *PbfRevisionNumber*, *PbfUserIdentifier*,
and *PbfUserName*.

### Reading Data <a name = "reading-data"></a>

Both *SerialPbfReader* and *ParallelPbfReader* implement the *PbfDataSource* interface,
which looks roughly like this:

    public interface PbfDataSource extends Resourced, Broadcaster
    {
        default Map<String, String> metadata()

        Count nodes()
        Count ways()
        Count relations()

        default PbfDataStatistics process(PbfDataProcessor processor)

        void onStart()
        PbfDataStatistics onProcess(PbfDataProcessor processor)
        void onEnd()
    }

The *process()* method initiates reading data from the data source, and as the data is read,
the *PbfDataProcessor* method is called with nodes, ways and relations. This interface looks
roughly like this:

    public interface PbfDataProcessor
    {
        enum Action
        {
            ACCEPTED,
            DISCARDED,
            FILTERED_OUT
        }

        default void onStartNodes()
        default void onStartWays()
        default void onStartRelations()

        default void onBounds(Bound bounds)
        default void onEntity(PbfEntity<?> entity)
        default void onMetadata(Map<String, String> metadata)

        default Action onNode(PbfNode node)
        default Action onWay(PbfWay way)
        default Action onRelation(PbfRelation relation)

        default void onEndNodes()
        default void onEndWays()
        default void onEndRelations()
    }

The *onStartX()* and *onEndX()* methods are called as different kinds of entities are encountered
in the data source. In OSM PBF format, the nodes are first, the ways second and relations are last.
At the start of data reading, the *onBounds(Bound) method is called along with *onMetadata()*.
Then each of the *onNode()*, *onWay()* and *onRelation()* methods is called until data of that
entity type is exhausted. The *Action* return values are used to gather statistical information
about how many ways were accepted, discarded or filtered out.

Use of the PBF data processing API generally looks similar to this:

    PbfDataStatistics process(final Resource resource)
    {
        return new SerialPbfReader(resource).process(new PbfDataProcessor()
        {
            public Action onNode(PbfNode node)
            {
                [...]
            }

            public Action onWay(PbfWay way)
            {
                [...]
            }

            public Action onRelation(final PbfRelation relation)
            {
                [...]
            }
        });
    }

### Writing Data <a name = "writing-data"></a>

The *PbfWriter* class provides a simple wrapper around Osmosis data serialization, allowing
an application to write an arbitrary set of entities to a PBF file.

[//]: # (end-user-text)

<img src="https://www.kivakit.org/images/short-horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"></img>

### Class Diagrams <a name="class-diagrams"></a> &nbsp; &nbsp; <img src="https://www.kivakit.org/images/diagram-32.png" srcset="https://www.kivakit.org/images/diagram-32-2x.png 2x"></img>

[*diagram-pbf-model-compression*](documentation/diagrams/diagram-pbf-model-compression.svg)
[*diagram-pbf-model-entities*](documentation/diagrams/diagram-pbf-model-entities.svg)
[*diagram-pbf-model-identifiers*](documentation/diagrams/diagram-pbf-model-identifiers.svg)
[*diagram-pbf-model-metadata*](documentation/diagrams/diagram-pbf-model-metadata.svg)
[*diagram-pbf-model-tags*](documentation/diagrams/diagram-pbf-model-tags.svg)
[*diagram-pbf-osm*](documentation/diagrams/diagram-pbf-osm.svg)
[*diagram-pbf-processing*](documentation/diagrams/diagram-pbf-processing.svg)
[*diagram-pbf-processing-filters*](documentation/diagrams/diagram-pbf-processing-filters.svg)
[*diagram-pbf-uni-db*](documentation/diagrams/diagram-pbf-uni-db.svg)

<img src="https://www.kivakit.org/images/short-horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"></img>

### Package Diagrams <a name="package-diagrams"></a> &nbsp;&nbsp; <img src="https://www.kivakit.org/images/box-32.png" srcset="https://www.kivakit.org/images/box-32-2x.png 2x"></img>

[*com.telenav.mesakit.map.data.formats.pbf.model.entities*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.model.entities.svg)
[*com.telenav.mesakit.map.data.formats.pbf.model.extractors*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.model.extractors.svg)
[*com.telenav.mesakit.map.data.formats.pbf.model.identifiers*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.model.identifiers.svg)
[*com.telenav.mesakit.map.data.formats.pbf.model.metadata*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.model.metadata.svg)
[*com.telenav.mesakit.map.data.formats.pbf.model.tags*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.model.tags.svg)
[*com.telenav.mesakit.map.data.formats.pbf.model.tags.compression*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.model.tags.compression.svg)
[*com.telenav.mesakit.map.data.formats.pbf.osm*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.osm.svg)
[*com.telenav.mesakit.map.data.formats.pbf.processing*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.processing.svg)
[*com.telenav.mesakit.map.data.formats.pbf.processing.filters*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.processing.filters.svg)
[*com.telenav.mesakit.map.data.formats.pbf.processing.filters.all*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.processing.filters.all.svg)
[*com.telenav.mesakit.map.data.formats.pbf.processing.filters.navteam*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.processing.filters.navteam.svg)
[*com.telenav.mesakit.map.data.formats.pbf.processing.filters.osm*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.processing.filters.osm.svg)
[*com.telenav.mesakit.map.data.formats.pbf.processing.filters.osmteam*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.processing.filters.osmteam.svg)
[*com.telenav.mesakit.map.data.formats.pbf.processing.filters.unidb*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.processing.filters.unidb.svg)
[*com.telenav.mesakit.map.data.formats.pbf.processing.readers*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.processing.readers.svg)
[*com.telenav.mesakit.map.data.formats.pbf.processing.writers*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.processing.writers.svg)
[*com.telenav.mesakit.map.data.formats.pbf.project*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.project.svg)
[*com.telenav.mesakit.map.data.formats.pbf.unidb*](documentation/diagrams/com.telenav.mesakit.map.data.formats.pbf.unidb.svg)

<img src="https://www.kivakit.org/images/short-horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"></img>

### Javadoc <a name="javadoc"></a> &nbsp;&nbsp; <img src="https://www.kivakit.org/images/books-32.png" srcset="https://www.kivakit.org/images/books-32-2x.png 2x"></img>

Javadoc coverage for this project is 43.0%.

&nbsp; &nbsp;  <img src="https://www.kivakit.org/images/meter-40-12.png" srcset="https://www.kivakit.org/images/meter-40-12-2x.png 2x"></img>

The following significant classes are undocumented:

- BasePbfReader
- OsmHighwayTag
- ParallelPbfReader
- PbfDefaultCodecs
- PbfEntity
- PbfNodeIdentifier
- PbfTagCodecBuilder
- PbfTagList
- PbfTagMap
- PbfTagPatternFilter
- PbfWriter
- RelationFilter
- WayFilter

| Class | Documentation Sections |
|---|---|
| [*AllRelationsFilter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/all/AllRelationsFilter.html) |  |
| [*AllWaysFilter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/all/AllWaysFilter.html) |  |
| [*BasePbfReader*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/readers/BasePbfReader.html) |  |
| [*DataFormatsPbfLimits*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/project/DataFormatsPbfLimits.html) |  |
| [*DataFormatsPbfProject*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/project/DataFormatsPbfProject.html) |  |
| [*EmptyPbfTagMap*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/tags/EmptyPbfTagMap.html) |  |
| [*IntegerExtractor*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/extractors/IntegerExtractor.html) |  |
| [*NavTeamWayFilter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/navteam/NavTeamWayFilter.html) |  |
| [*Osm*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/osm/Osm.html) |  |
| [*OsmExcludeNonDrivableWaysFilter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/osm/OsmExcludeNonDrivableWaysFilter.html) |  |
| [*OsmHighwayTag*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/osm/OsmHighwayTag.html) |  |
| [*OsmIgnoredTags*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/osm/OsmIgnoredTags.html) |  |
| [*OsmMajorRoadsWayFilter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/osm/OsmMajorRoadsWayFilter.html) |  |
| [*OsmNavigableWayFilter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/osm/OsmNavigableWayFilter.html) |  |
| [*OsmRelationTag*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/osm/OsmRelationTag.html) |  |
| [*OsmRelationsFilter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/osm/OsmRelationsFilter.html) |  |
| [*OsmTeamDataPipelineWayFilter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/osmteam/OsmTeamDataPipelineWayFilter.html) |  |
| [*OsmTeamWayFilter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/osmteam/OsmTeamWayFilter.html) |  |
| [*OsmWaysFilter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/osm/OsmWaysFilter.html) |  |
| [*ParallelPbfReader*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/readers/ParallelPbfReader.html) |  |
| [*PbfChangeSetIdentifier*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/metadata/PbfChangeSetIdentifier.html) |  |
| [*PbfCharacterCodecBuilder*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/tags/compression/PbfCharacterCodecBuilder.html) |  |
| [*PbfDataProcessor*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/PbfDataProcessor.html) |  |
| [*PbfDataProcessor.Action*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/PbfDataProcessor.Action.html) |  |
| [*PbfDataSource*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/PbfDataSource.html) |  |
| [*PbfDataStatistics*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/PbfDataStatistics.html) |  |
| [*PbfDefaultCodecs*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/tags/compression/PbfDefaultCodecs.html) |  |
| [*PbfEntity*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/entities/PbfEntity.html) |  |
| [*PbfFilters*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/PbfFilters.html) |  |
| [*PbfIdentifierType*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/identifiers/PbfIdentifierType.html) |  |
| [*PbfNode*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/entities/PbfNode.html) |  |
| [*PbfNodeIdentifier*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/identifiers/PbfNodeIdentifier.html) |  |
| [*PbfNodeIdentifier.Converter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/identifiers/PbfNodeIdentifier.Converter.html) |  |
| [*PbfNodeIdentifier.Factory*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/identifiers/PbfNodeIdentifier.Factory.html) |  |
| [*PbfRelation*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/entities/PbfRelation.html) |  |
| [*PbfRelationIdentifier*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/identifiers/PbfRelationIdentifier.html) |  |
| [*PbfRelationIdentifier.Converter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/identifiers/PbfRelationIdentifier.Converter.html) |  |
| [*PbfRevisionNumber*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/metadata/PbfRevisionNumber.html) |  |
| [*PbfStopProcessingException*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/PbfStopProcessingException.html) |  |
| [*PbfStringListTagCodec*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/tags/compression/PbfStringListTagCodec.html) |  |
| [*PbfTagCodec*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/tags/compression/PbfTagCodec.html) |  |
| [*PbfTagCodecBuilder*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/tags/compression/PbfTagCodecBuilder.html) |  |
| [*PbfTagFilter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/tags/PbfTagFilter.html) |  |
| [*PbfTagList*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/tags/PbfTagList.html) |  |
| [*PbfTagMap*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/tags/PbfTagMap.html) |  |
| [*PbfTagPatternFilter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/tags/PbfTagPatternFilter.html) |  |
| [*PbfTagPatternFilter.Converter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/tags/PbfTagPatternFilter.Converter.html) |  |
| [*PbfTags*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/tags/PbfTags.html) |  |
| [*PbfUserIdentifier*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/metadata/PbfUserIdentifier.html) |  |
| [*PbfUserName*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/metadata/PbfUserName.html) |  |
| [*PbfWay*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/entities/PbfWay.html) |  |
| [*PbfWayIdentifier*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/identifiers/PbfWayIdentifier.html) |  |
| [*PbfWayIdentifier.Converter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/identifiers/PbfWayIdentifier.Converter.html) |  |
| [*PbfWriter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/writers/PbfWriter.html) |  |
| [*PbfWriter.Phase*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/writers/PbfWriter.Phase.html) |  |
| [*RelationFilter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/RelationFilter.html) |  |
| [*RelationFilter.Converter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/RelationFilter.Converter.html) |  |
| [*RevisionNumberExtractor*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/extractors/RevisionNumberExtractor.html) |  |
| [*SerialPbfReader*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/readers/SerialPbfReader.html) |  |
| [*TimestampExtractor*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/model/extractors/TimestampExtractor.html) |  |
| [*UniDb*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/unidb/UniDb.html) |  |
| [*UniDbExcludeAdasRelationFilter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/unidb/UniDbExcludeAdasRelationFilter.html) |  |
| [*UniDbNavigableWayFilter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/unidb/UniDbNavigableWayFilter.html) |  |
| [*UniDbRelationTag*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/unidb/UniDbRelationTag.html) |  |
| [*UniDbRelationsFilter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/unidb/UniDbRelationsFilter.html) |  |
| [*WayFilter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/WayFilter.html) |  |
| [*WayFilter.Converter*](https://telenav.github.io/mesakit-data/javadoc/mesakit.map.cutter/com/telenav/mesakit/map/data/formats/pbf/processing/filters/WayFilter.Converter.html) |  |

[//]: # (start-user-text)



[//]: # (end-user-text)

<br/>

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512-2x.png 2x"></img>

<sub>Copyright &#169; 2011-2021 [Telenav](http://telenav.com), Inc. Distributed under [Apache License, Version 2.0](LICENSE)</sub>
<sub>This documentation was generated by [Lexakai](https://github.com/Telenav/lexakai) on 2021.04.15. UML diagrams courtesy
of [PlantUML](http://plantuml.com).</sub>

