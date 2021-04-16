open module mesakit.map.road.name.standardizer
{
    requires transitive mesakit.map.road.name.parser;
    requires transitive mesakit.map.region;

    exports com.telenav.mesakit.map.road.name.standardizer;
    exports com.telenav.mesakit.map.road.name.standardizer.locales.english;
    exports com.telenav.mesakit.map.road.name.standardizer.locales.indonesian;
}
