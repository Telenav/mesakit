open module mesakit.map.region
{
    requires transitive mesakit.core;
    requires transitive mesakit.data.formats.pbf;
    requires transitive mesakit.map.geography;
    requires transitive mesakit.map.ui.debug;
    requires transitive mesakit.map.utilities.grid;

    requires transitive kivakit.component;
    requires transitive kivakit.network.http;
    requires transitive kivakit.extraction;
    requires transitive kivakit.serialization.core;
    requires transitive kivakit.primitive.collections;

    requires kryo;
    requires junit;

    exports com.telenav.mesakit.map.region;
    exports com.telenav.mesakit.map.region.regions;
    exports com.telenav.mesakit.map.region.border;
    exports com.telenav.mesakit.map.region.border.cache;
    exports com.telenav.mesakit.map.region.continents;
    exports com.telenav.mesakit.map.region.countries;
    exports com.telenav.mesakit.map.region.countries.states;
    exports com.telenav.mesakit.map.region.countries.states.cities;
    exports com.telenav.mesakit.map.region.locale;
}
