open module aonia.map.road.name.parser
{
    requires transitive aonia.map.road.model;
    requires transitive aonia.map.region;

    exports com.telenav.aonia.map.road.name.parser;
    exports com.telenav.aonia.map.road.name.parser.locales.english;
    exports com.telenav.aonia.map.road.name.parser.locales.indonesian;
    exports com.telenav.aonia.map.road.name.parser.tokenizer;
    exports com.telenav.aonia.map.road.name.parser.tokenizer.symbols;
}
