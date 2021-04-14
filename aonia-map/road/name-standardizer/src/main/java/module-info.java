open module aonia.map.road.name.standardizer
{
    requires transitive aonia.map.road.name.parser;
    requires transitive aonia.map.region;

    exports com.telenav.aonia.map.road.name.standardizer;
    exports com.telenav.aonia.map.road.name.standardizer.locales.english;
    exports com.telenav.aonia.map.road.name.standardizer.locales.indonesian;
}
