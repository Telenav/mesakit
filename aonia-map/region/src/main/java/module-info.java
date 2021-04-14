open module aonia.map.region
{
    requires transitive aonia.data.formats.pbf;
    requires transitive aonia.map.geography;
    requires transitive aonia.map.ui;
    requires transitive aonia.map.utilities.grid;

    requires transitive kivakit.core.network.http;

    exports com.telenav.aonia.map.region;
    exports com.telenav.aonia.map.region.regions;
    exports com.telenav.aonia.map.region.border;
    exports com.telenav.aonia.map.region.border.cache;
    exports com.telenav.aonia.map.region.continents;
    exports com.telenav.aonia.map.region.countries;
    exports com.telenav.aonia.map.region.countries.states;
    exports com.telenav.aonia.map.region.countries.states.cities;
    exports com.telenav.aonia.map.region.locale;
    exports com.telenav.aonia.map.region.project;
}
