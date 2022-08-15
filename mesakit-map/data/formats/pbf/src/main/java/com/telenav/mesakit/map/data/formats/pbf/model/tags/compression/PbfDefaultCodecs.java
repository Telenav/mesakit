package com.telenav.mesakit.map.data.formats.pbf.model.tags.compression;

import com.telenav.kivakit.core.object.Lazy;
import com.telenav.kivakit.data.compression.codecs.huffman.character.HuffmanCharacterCodec;
import com.telenav.kivakit.data.compression.codecs.huffman.string.HuffmanStringCodec;
import com.telenav.kivakit.properties.PropertyMap;
import com.telenav.kivakit.resource.packages.Package;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.mesakit.map.data.formats.pbf.internal.lexakai.DiagramPbfModelCompression;
import org.jetbrains.annotations.NotNull;

import static com.telenav.kivakit.core.messaging.Listener.throwingListener;
import static com.telenav.kivakit.data.compression.codecs.huffman.character.HuffmanCharacterCodec.ESCAPE;

/**
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfModelCompression.class)
public class PbfDefaultCodecs
{
    private static final Lazy<PbfDefaultCodecs> defaultCodecs = Lazy.of(PbfDefaultCodecs::new);

    public static PbfDefaultCodecs get()
    {
        return defaultCodecs.get();
    }

    private final Lazy<PropertyMap> keyCharacterCodecFrequencies = Lazy.of(() -> load("codecs/default-key-character.codec"));

    private final Lazy<PropertyMap> keyStringCodecFrequencies = Lazy.of(() -> load("codecs/default-key-string.codec"));

    private final Lazy<PropertyMap> roadNameCharacterCodecFrequencies = Lazy.of(() -> load("codecs/default-road-name-character.codec"));

    private final Lazy<PropertyMap> valueCharacterCodecFrequencies = Lazy.of(() -> load("codecs/default-value-character.codec"));

    private final Lazy<PropertyMap> valueStringCodecFrequencies = Lazy.of(() -> load("codecs/default-value-string.codec"));

    protected PbfDefaultCodecs()
    {
    }

    @NotNull
    @UmlRelation(label = "provides", referentCardinality = "2")
    public HuffmanCharacterCodec defaultKeyCharacterCodec()
    {
        return HuffmanCharacterCodec.from(throwingListener(), keyCharacterCodecFrequencies(), ESCAPE);
    }

    @NotNull
    @UmlRelation(label = "provides", referentCardinality = "2")
    public HuffmanStringCodec defaultKeyStringCodec()
    {
        return HuffmanStringCodec.from(keyStringCodecFrequencies());
    }

    @NotNull
    public HuffmanCharacterCodec defaultValueCharacterCodec()
    {
        return HuffmanCharacterCodec.from(throwingListener(), valueCharacterCodecFrequencies(), ESCAPE);
    }

    @NotNull
    public HuffmanStringCodec defaultValueStringCodec()
    {
        return HuffmanStringCodec.from(valueStringCodecFrequencies());
    }

    public PropertyMap keyCharacterCodecFrequencies()
    {
        return keyCharacterCodecFrequencies.get();
    }

    public PropertyMap keyStringCodecFrequencies()
    {
        return keyStringCodecFrequencies.get();
    }

    public PropertyMap roadNameCharacterCodecFrequencies()
    {
        return roadNameCharacterCodecFrequencies.get();
    }

    public PropertyMap valueCharacterCodecFrequencies()
    {
        return valueCharacterCodecFrequencies.get();
    }

    public PropertyMap valueStringCodecFrequencies()
    {
        return valueStringCodecFrequencies.get();
    }

    private PropertyMap load(String codec)
    {
        return PropertyMap.load(throwingListener(), Package.packageContaining(throwingListener(), PbfDefaultCodecs.class), codec);
    }
}
