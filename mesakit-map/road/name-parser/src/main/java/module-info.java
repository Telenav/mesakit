open module mesakit.map.road.name.parser
{
    requires transitive mesakit.map.road.model;
    requires transitive mesakit.map.region;

    exports com.telenav.mesakit.map.road.name.parser;
    exports com.telenav.mesakit.map.road.name.parser.locales.english;
    exports com.telenav.mesakit.map.road.name.parser.locales.indonesian;
    exports com.telenav.mesakit.map.road.name.parser.tokenizer;
    exports com.telenav.mesakit.map.road.name.parser.tokenizer.symbols;
}
