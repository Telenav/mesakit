open module mesakit.graph.core
{
    requires transitive java.sql;

    requires transitive kivakit.configuration;
    requires transitive kivakit.primitive.collections;
    requires transitive mesakit.data.formats.pbf;
    requires transitive mesakit.map.region;
    requires transitive mesakit.map.overpass;
    requires transitive mesakit.map.road.name.standardizer;
    requires transitive mesakit.map.ui.debug;
    requires transitive mesakit.map.geography;

    exports com.telenav.mesakit.graph.analytics.classification.classifiers.road;
    exports com.telenav.mesakit.graph.analytics.classification.classifiers.signpost;
    exports com.telenav.mesakit.graph.analytics.classification.classifiers.turn;
    exports com.telenav.mesakit.graph.analytics.classification;
    exports com.telenav.mesakit.graph.analytics.crawler;
    exports com.telenav.mesakit.graph.analytics.junction;
    exports com.telenav.mesakit.graph.analytics.ramp;
    exports com.telenav.mesakit.graph.collections;
    exports com.telenav.mesakit.graph.identifiers.collections;
    exports com.telenav.mesakit.graph.identifiers;
    exports com.telenav.mesakit.graph.io.archive;
    exports com.telenav.mesakit.graph.io.convert;
    exports com.telenav.mesakit.graph.io.load.loaders.decimation;
    exports com.telenav.mesakit.graph.io.load.loaders.region.regions;
    exports com.telenav.mesakit.graph.io.load.loaders.region;
    exports com.telenav.mesakit.graph.io.load.loaders;
    exports com.telenav.mesakit.graph.io.load;
    exports com.telenav.mesakit.graph.io.save;
    exports com.telenav.mesakit.graph.library.osm.change;
    exports com.telenav.mesakit.graph.map;
    exports com.telenav.mesakit.graph.matching.conflation;
    exports com.telenav.mesakit.graph.matching.snapping;
    exports com.telenav.mesakit.graph.metadata;
    exports com.telenav.mesakit.graph.navigation.limiters;
    exports com.telenav.mesakit.graph.navigation.navigators;
    exports com.telenav.mesakit.graph.navigation;
    exports com.telenav.mesakit.graph.project;
    exports com.telenav.mesakit.graph.relations.restrictions;
    exports com.telenav.mesakit.graph.specifications.common.edge.store;
    exports com.telenav.mesakit.graph.specifications.common.edge;
    exports com.telenav.mesakit.graph.specifications.common.element;
    exports com.telenav.mesakit.graph.specifications.common.graph.loader;
    exports com.telenav.mesakit.graph.specifications.common.node.store;
    exports com.telenav.mesakit.graph.specifications.common.node;
    exports com.telenav.mesakit.graph.specifications.common.place.store;
    exports com.telenav.mesakit.graph.specifications.common.place;
    exports com.telenav.mesakit.graph.specifications.common.relation.store;
    exports com.telenav.mesakit.graph.specifications.common.relation;
    exports com.telenav.mesakit.graph.specifications.common.shapepoint.store;
    exports com.telenav.mesakit.graph.specifications.common.shapepoint;
    exports com.telenav.mesakit.graph.specifications.common.vertex.store;
    exports com.telenav.mesakit.graph.specifications.common.vertex;
    exports com.telenav.mesakit.graph.specifications.common;
    exports com.telenav.mesakit.graph.specifications.library.attributes;
    exports com.telenav.mesakit.graph.specifications.library.pbf;
    exports com.telenav.mesakit.graph.specifications.library.properties;
    exports com.telenav.mesakit.graph.specifications.library.store;
    exports com.telenav.mesakit.graph.specifications.osm.graph.converter;
    exports com.telenav.mesakit.graph.specifications.osm.graph.edge.model.attributes;
    exports com.telenav.mesakit.graph.specifications.osm.graph.edge.model;
    exports com.telenav.mesakit.graph.specifications.osm.graph.edge.store;
    exports com.telenav.mesakit.graph.specifications.osm.graph.loader.sectioner;
    exports com.telenav.mesakit.graph.specifications.osm.graph.loader;
    exports com.telenav.mesakit.graph.specifications.osm.graph;
    exports com.telenav.mesakit.graph.specifications.osm;
    exports com.telenav.mesakit.graph.ui.debuggers.edge.sectioner;
    exports com.telenav.mesakit.graph.ui.viewer;
    exports com.telenav.mesakit.graph;
    exports com.telenav.mesakit.graph.specifications.common.graph.loader.extractors;
}
