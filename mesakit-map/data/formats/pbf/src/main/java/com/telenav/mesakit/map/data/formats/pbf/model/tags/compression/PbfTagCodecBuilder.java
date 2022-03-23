package com.telenav.mesakit.map.data.formats.pbf.model.tags.compression;

import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.string.AsciiArt;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.core.value.count.Minimum;
import com.telenav.kivakit.core.value.count.MutableCount;
import com.telenav.kivakit.data.compression.codecs.huffman.character.CharacterFrequencies;
import com.telenav.kivakit.data.compression.codecs.huffman.character.HuffmanCharacterCodec;
import com.telenav.kivakit.data.compression.codecs.huffman.string.HuffmanStringCodec;
import com.telenav.kivakit.data.compression.codecs.huffman.string.StringFrequencies;
import com.telenav.kivakit.filesystem.File;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.mesakit.map.data.formats.pbf.lexakai.DiagramPbfModelCompression;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfEntity;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.RelationFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.WayFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.readers.SerialPbfReader;

import static com.telenav.kivakit.data.compression.codecs.huffman.character.HuffmanCharacterCodec.ESCAPE;
import static com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor.Action.ACCEPTED;
import static com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor.Action.FILTERED_OUT;

/**
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfModelCompression.class)
public class PbfTagCodecBuilder
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    @UmlAggregation
    private WayFilter wayFilter;

    private Integer sampleFrequency = 1;

    private Maximum stringsMaximum = Maximum._256;

    private Minimum stringsMinimumOccurrences = Minimum._8;

    private Maximum stringsMaximumBits = Maximum._12;

    private Minimum charactersMinimumOccurrences = Minimum._8;

    private Maximum charactersMaximumBits = Maximum._12;

    @UmlAggregation(label = "collects", referentCardinality = "2")
    private final StringFrequencies keyStringFrequencies = new StringFrequencies(Count._10_000_000, Maximum.maximum(50_000_000));

    private final StringFrequencies valueStringFrequencies = new StringFrequencies(Count._10_000_000, Maximum.maximum(50_000_000));

    @UmlAggregation(label = "collects", referentCardinality = "2")
    private final CharacterFrequencies keyCharacterFrequencies = new CharacterFrequencies();

    private final CharacterFrequencies valueCharacterFrequencies = new CharacterFrequencies();

    @UmlAggregation(label = "builds", referentCardinality = "2")
    private HuffmanCharacterCodec keyCharacterCodec;

    private HuffmanCharacterCodec valueCharacterCodec;

    @UmlAggregation(label = "builds", referentCardinality = "2")
    private HuffmanStringCodec keyStringCodec;

    private HuffmanStringCodec valueStringCodec;

    private RelationFilter relationFilter;

    public void addCharacters(PbfEntity<?> entity)
    {
        var tags = entity.tagList();
        for (var tag : tags)
        {
            var key = tag.getKey();
            if (keyStringCodec == null || !keyStringCodec.canEncode(key))
            {
                keyCharacterFrequencies.add(key);
            }
            var value = tag.getValue();
            if (valueStringCodec == null || !valueStringCodec.canEncode(value))
            {
                valueCharacterFrequencies.add(value);
            }
        }
    }

    public void build(File input)
    {
        buildStringCodecs(input);
        buildCharacterCodecs(input);
    }

    public void buildCharacterCodecs(File input)
    {
        // Then take another pass through the file to build the character codecs but without any of the
        // string values that will be compressed with the string compressors
        var reader = LOGGER.listenTo(new SerialPbfReader(input));
        var entities = new MutableCount();
        reader.phase("Building Character Codecs");
        reader.process(new PbfDataProcessor()
        {
            @Override
            public Action onRelation(PbfRelation relation)
            {
                if (entities.increment() % sampleFrequency == 0)
                {
                    addCharacters(relation);
                }
                return ACCEPTED;
            }

            @Override
            public Action onWay(PbfWay way)
            {
                if (wayFilter.accepts(way))
                {
                    if (entities.increment() % sampleFrequency == 0)
                    {
                        addCharacters(way);
                    }
                    return ACCEPTED;
                }
                return FILTERED_OUT;
            }
        });

        buildCharacterCodecs();
    }

    public void buildCharacterCodecs()
    {
        var keyEscapes = keyCharacterFrequencies
                .escaped(charactersMinimumOccurrences.asMaximum())
                .maximum(charactersMinimumOccurrences.incremented().asCount());
        keyCharacterFrequencies.frequencies().add(ESCAPE, keyEscapes);
        var keySymbols = keyCharacterFrequencies.symbols(charactersMinimumOccurrences);
        if (keySymbols.size() < 32)
        {
            keyCharacterCodec = PbfDefaultCodecs.get().defaultKeyCharacterCodec();
        }
        else
        {
            keyCharacterCodec = HuffmanCharacterCodec.from(keySymbols, charactersMaximumBits);
        }

        var valueEscapes = valueCharacterFrequencies
                .escaped(charactersMinimumOccurrences.asMaximum())
                .maximum(charactersMinimumOccurrences.incremented().asCount());
        valueCharacterFrequencies.frequencies().add(ESCAPE, valueEscapes);
        var valueSymbols = valueCharacterFrequencies.symbols(charactersMinimumOccurrences);
        if (valueSymbols.size() < 32)
        {
            valueCharacterCodec = PbfDefaultCodecs.get().defaultValueCharacterCodec();
        }
        else
        {
            valueCharacterCodec = HuffmanCharacterCodec.from(valueSymbols, charactersMaximumBits);
        }
    }

    public void buildStringCodecs(File input)
    {
        var reader = LOGGER.listenTo(new SerialPbfReader(input));
        var entities = new MutableCount();
        reader.phase("Building String Codecs");
        reader.process(new PbfDataProcessor()
        {
            @Override
            public Action onRelation(PbfRelation relation)
            {
                if (relationFilter.accepts(relation))
                {
                    if (entities.increment() % sampleFrequency == 0)
                    {
                        addStrings(relation);
                    }
                    return ACCEPTED;
                }
                return FILTERED_OUT;
            }

            @Override
            public Action onWay(PbfWay way)
            {
                if (wayFilter.accepts(way))
                {
                    if (entities.increment() % sampleFrequency == 0)
                    {
                        addStrings(way);
                    }
                    return ACCEPTED;
                }
                return FILTERED_OUT;
            }
        });

        buildStringCodecs();
    }

    public void buildStringCodecs()
    {
        // Build the string codecs using only symbols with a high enough frequency up to the given number of bits
        var keySymbols = keyStringFrequencies
                .top(stringsMaximum)
                .symbols(stringsMinimumOccurrences);
        if (keySymbols.size() < 16)
        {
            keyStringCodec = PbfDefaultCodecs.get().defaultKeyStringCodec();
        }
        else
        {
            keyStringCodec = HuffmanStringCodec.from(keySymbols, stringsMaximumBits);
        }

        var valueSymbols = valueStringFrequencies
                .top(stringsMaximum)
                .symbols(stringsMinimumOccurrences);
        if (valueSymbols.size() < 16)
        {
            valueStringCodec = PbfDefaultCodecs.get().defaultValueStringCodec();
        }
        else
        {
            valueStringCodec = HuffmanStringCodec.from(valueSymbols, stringsMaximumBits);
        }
    }

    public PbfTagCodecBuilder charactersMaximumBits(Maximum charactersMaximumBits)
    {
        this.charactersMaximumBits = charactersMaximumBits;
        return this;
    }

    public PbfTagCodecBuilder charactersMinimumOccurrences(Minimum charactersMinimumOccurrences)
    {
        this.charactersMinimumOccurrences = charactersMinimumOccurrences;
        return this;
    }

    public HuffmanCharacterCodec keyCharacterCodec()
    {
        return keyCharacterCodec;
    }

    public HuffmanStringCodec keyStringCodec()
    {
        return keyStringCodec;
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void output()
    {
        System.out.println(AsciiArt.box("The properties files output by this program can be be read by a PbfStringListTagCodec.\n"
                + "See OsmDataSpecification for an example."));
        System.out.println();

        System.out.println(AsciiArt.box("Key Character Codec"));
        System.out.println(keyCharacterCodec);
        System.out.println(AsciiArt.box("Value Character Codec"));
        System.out.println(valueCharacterCodec);

        System.out.println(AsciiArt.box("Key String Codec"));
        System.out.println(keyStringCodec);
        System.out.println(AsciiArt.box("Value String Codec"));
        System.out.println(valueStringCodec);

        keyCharacterCodec.asProperties().save(keyCharacterCodec.toString(), File.parseFile(Listener.console(), "default-key-character.codec"));
        valueCharacterCodec.asProperties().save(valueCharacterCodec.toString(), File.parseFile(Listener.console(), "default-value-character.codec"));
        keyStringCodec.asProperties().save(keyStringCodec.toString(), File.parseFile(Listener.console(), "default-key-string.codec"));
        valueStringCodec.asProperties().save(valueStringCodec.toString(), File.parseFile(Listener.console(), "default-value-string.codec"));
    }

    public PbfTagCodecBuilder relationFilter(RelationFilter relationFilter)
    {
        this.relationFilter = relationFilter;
        return this;
    }

    public void sample(PbfEntity<?> entity)
    {
        addStrings(entity);
        addCharacters(entity);
    }

    public PbfTagCodecBuilder sampleFrequency(Integer sampleFrequency)
    {
        this.sampleFrequency = sampleFrequency;
        return this;
    }

    public PbfTagCodecBuilder stringsMaximum(Maximum stringsMaximum)
    {
        this.stringsMaximum = stringsMaximum;
        return this;
    }

    public PbfTagCodecBuilder stringsMaximumBits(Maximum stringsMaximumBits)
    {
        this.stringsMaximumBits = stringsMaximumBits;
        return this;
    }

    public PbfTagCodecBuilder stringsMinimumOccurrences(Minimum stringsMinimumOccurrences)
    {
        this.stringsMinimumOccurrences = stringsMinimumOccurrences;
        return this;
    }

    public HuffmanCharacterCodec valueCharacterCodec()
    {
        return valueCharacterCodec;
    }

    public HuffmanStringCodec valueStringCodec()
    {
        return valueStringCodec;
    }

    public PbfTagCodecBuilder wayFilter(WayFilter wayFilter)
    {
        this.wayFilter = wayFilter;
        return this;
    }

    /**
     * Adds the given tags to this codec to train it.
     */
    void addStrings(PbfEntity<?> entity)
    {
        var tags = entity.tagList();
        if (!tags.isEmpty())
        {
            for (var tag : tags)
            {
                var key = tag.getKey();
                if (validPropertiesFileString(key))
                {
                    keyStringFrequencies.add(key);
                }
                var value = tag.getValue();
                if (validPropertiesFileString(value))
                {
                    valueStringFrequencies.add(value);
                }
            }
        }
    }

    private boolean validPropertiesFileString(String value)
    {
        var length = value.length();
        return length >= 4 && length < 64 && !value.contains("=");
    }
}
