package com.telenav.mesakit.map.data.formats.pbf.model.tags.compression;

import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.kernel.language.strings.AsciiArt;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.kivakit.kernel.language.values.count.Minimum;
import com.telenav.kivakit.kernel.language.values.count.MutableCount;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.data.compression.codecs.huffman.character.CharacterFrequencies;
import com.telenav.kivakit.data.compression.codecs.huffman.character.HuffmanCharacterCodec;
import com.telenav.kivakit.data.compression.codecs.huffman.string.HuffmanStringCodec;
import com.telenav.kivakit.data.compression.codecs.huffman.string.StringFrequencies;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfEntity;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.RelationFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.WayFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.readers.SerialPbfReader;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfModelCompression;

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

    public void addCharacters(final PbfEntity<?> entity)
    {
        final var tags = entity.tagList();
        for (final var tag : tags)
        {
            final var key = tag.getKey();
            if (keyStringCodec == null || !keyStringCodec.canEncode(key))
            {
                keyCharacterFrequencies.add(key);
            }
            final var value = tag.getValue();
            if (valueStringCodec == null || !valueStringCodec.canEncode(value))
            {
                valueCharacterFrequencies.add(value);
            }
        }
    }

    public void build(final File input)
    {
        buildStringCodecs(input);
        buildCharacterCodecs(input);
    }

    public void buildCharacterCodecs(final File input)
    {
        // Then take another pass through the file to build the character codecs but without any of the
        // string values that will be compressed with the string compressors
        final var reader = LOGGER.listenTo(new SerialPbfReader(input));
        final var entities = new MutableCount();
        reader.phase("Building Character Codecs");
        reader.process(new PbfDataProcessor()
        {
            @Override
            public Action onRelation(final PbfRelation relation)
            {
                if (entities.increment() % sampleFrequency == 0)
                {
                    addCharacters(relation);
                }
                return ACCEPTED;
            }

            @Override
            public Action onWay(final PbfWay way)
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
        final var keyEscapes = keyCharacterFrequencies
                .escaped(charactersMinimumOccurrences.asMaximum())
                .maximum(charactersMinimumOccurrences.incremented());
        keyCharacterFrequencies.frequencies().add(ESCAPE, keyEscapes);
        final var keySymbols = keyCharacterFrequencies.symbols(charactersMinimumOccurrences);
        if (keySymbols.size() < 32)
        {
            keyCharacterCodec = PbfDefaultCodecs.get().defaultKeyCharacterCodec();
        }
        else
        {
            keyCharacterCodec = HuffmanCharacterCodec.from(keySymbols, charactersMaximumBits);
        }

        final var valueEscapes = valueCharacterFrequencies
                .escaped(charactersMinimumOccurrences.asMaximum())
                .maximum(charactersMinimumOccurrences.incremented());
        valueCharacterFrequencies.frequencies().add(ESCAPE, valueEscapes);
        final var valueSymbols = valueCharacterFrequencies.symbols(charactersMinimumOccurrences);
        if (valueSymbols.size() < 32)
        {
            valueCharacterCodec = PbfDefaultCodecs.get().defaultValueCharacterCodec();
        }
        else
        {
            valueCharacterCodec = HuffmanCharacterCodec.from(valueSymbols, charactersMaximumBits);
        }
    }

    public void buildStringCodecs(final File input)
    {
        final var reader = LOGGER.listenTo(new SerialPbfReader(input));
        final var entities = new MutableCount();
        reader.phase("Building String Codecs");
        reader.process(new PbfDataProcessor()
        {
            @Override
            public Action onRelation(final PbfRelation relation)
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
            public Action onWay(final PbfWay way)
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
        final var keySymbols = keyStringFrequencies
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

        final var valueSymbols = valueStringFrequencies
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

    public PbfTagCodecBuilder charactersMaximumBits(final Maximum charactersMaximumBits)
    {
        this.charactersMaximumBits = charactersMaximumBits;
        return this;
    }

    public PbfTagCodecBuilder charactersMinimumOccurrences(final Minimum charactersMinimumOccurrences)
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

        keyCharacterCodec.asProperties().save(keyCharacterCodec.toString(), File.parse("default-key-character.codec"));
        valueCharacterCodec.asProperties().save(valueCharacterCodec.toString(), File.parse("default-value-character.codec"));
        keyStringCodec.asProperties().save(keyStringCodec.toString(), File.parse("default-key-string.codec"));
        valueStringCodec.asProperties().save(valueStringCodec.toString(), File.parse("default-value-string.codec"));
    }

    public PbfTagCodecBuilder relationFilter(final RelationFilter relationFilter)
    {
        this.relationFilter = relationFilter;
        return this;
    }

    public void sample(final PbfEntity<?> entity)
    {
        addStrings(entity);
        addCharacters(entity);
    }

    public PbfTagCodecBuilder sampleFrequency(final Integer sampleFrequency)
    {
        this.sampleFrequency = sampleFrequency;
        return this;
    }

    public PbfTagCodecBuilder stringsMaximum(final Maximum stringsMaximum)
    {
        this.stringsMaximum = stringsMaximum;
        return this;
    }

    public PbfTagCodecBuilder stringsMaximumBits(final Maximum stringsMaximumBits)
    {
        this.stringsMaximumBits = stringsMaximumBits;
        return this;
    }

    public PbfTagCodecBuilder stringsMinimumOccurrences(final Minimum stringsMinimumOccurrences)
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

    public PbfTagCodecBuilder wayFilter(final WayFilter wayFilter)
    {
        this.wayFilter = wayFilter;
        return this;
    }

    /**
     * Adds the given tags to this codec to train it.
     */
    void addStrings(final PbfEntity<?> entity)
    {
        final var tags = entity.tagList();
        if (!tags.isEmpty())
        {
            for (final var tag : tags)
            {
                final var key = tag.getKey();
                if (validPropertiesFileString(key))
                {
                    keyStringFrequencies.add(key);
                }
                final var value = tag.getValue();
                if (validPropertiesFileString(value))
                {
                    valueStringFrequencies.add(value);
                }
            }
        }
    }

    private boolean validPropertiesFileString(final String value)
    {
        final var length = value.length();
        return length >= 4 && length < 64 && !value.contains("=");
    }
}
