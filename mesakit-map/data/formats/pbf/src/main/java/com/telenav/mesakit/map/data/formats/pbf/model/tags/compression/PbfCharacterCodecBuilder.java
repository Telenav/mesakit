package com.telenav.mesakit.map.data.formats.pbf.model.tags.compression;

import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.core.value.count.Minimum;
import com.telenav.kivakit.data.compression.codecs.huffman.character.CharacterFrequencies;
import com.telenav.kivakit.data.compression.codecs.huffman.character.HuffmanCharacterCodec;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.mesakit.map.data.formats.pbf.internal.lexakai.DiagramPbfModelCompression;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfEntity;

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
        var escapes = frequencies
                .escaped(charactersMinimumOccurrences.asMaximum()).asCount()
                .maximize(charactersMinimumOccurrences.incremented().asCount());
        frequencies.frequencies().add(ESCAPE, escapes);
        var symbols = frequencies.symbols(charactersMinimumOccurrences);
        return symbols.size() < 16 ? PbfDefaultCodecs.get().defaultKeyCharacterCodec() :
                HuffmanCharacterCodec.from(symbols, charactersMaximumBits);
    }

    public void sample(PbfEntity<?> entity)
    {
        var tags = entity.tagList();
        for (var tag : tags)
        {
            frequencies.add(tag.getKey());
            frequencies.add(tag.getValue());
        }
    }
}
