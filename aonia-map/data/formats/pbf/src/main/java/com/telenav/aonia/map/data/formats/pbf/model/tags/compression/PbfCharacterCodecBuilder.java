package com.telenav.aonia.map.data.formats.pbf.model.tags.compression;

import com.telenav.aonia.map.data.formats.pbf.model.entities.PbfEntity;
import com.telenav.aonia.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfModelCompression;
import com.telenav.kivakit.core.kernel.language.values.count.Maximum;
import com.telenav.kivakit.core.kernel.language.values.count.Minimum;
import com.telenav.kivakit.data.compression.codecs.huffman.character.CharacterFrequencies;
import com.telenav.kivakit.data.compression.codecs.huffman.character.HuffmanCharacterCodec;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;

import static com.telenav.kivakit.data.compression.codecs.huffman.character.HuffmanCharacterCodec.ESCAPE;

/**
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfModelCompression.class)
public class PbfCharacterCodecBuilder
{
    @UmlRelation(label = "builds")
    private final CharacterFrequencies frequencies = new CharacterFrequencies();

    private final Minimum charactersMinimumOccurrences = Minimum._1024;

    private final Maximum charactersMaximumBits = Maximum._12;

    @UmlRelation(label = "builds")
    public HuffmanCharacterCodec build()
    {
        final var escapes = frequencies
                .escaped(charactersMinimumOccurrences.asMaximum())
                .maximum(charactersMinimumOccurrences.incremented());
        frequencies.frequencies().add(ESCAPE, escapes);
        final var symbols = frequencies.symbols(charactersMinimumOccurrences);
        return symbols.size() < 16 ? PbfDefaultCodecs.get().defaultKeyCharacterCodec() :
                HuffmanCharacterCodec.from(symbols, charactersMaximumBits);
    }

    public void sample(final PbfEntity<?> entity)
    {
        final var tags = entity.tagList();
        for (final var tag : tags)
        {
            frequencies.add(tag.getKey());
            frequencies.add(tag.getValue());
        }
    }
}
