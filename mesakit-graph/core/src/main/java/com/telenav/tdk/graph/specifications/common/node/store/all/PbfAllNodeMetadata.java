////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.kivakit.graph.specifications.common.node.store.all;

import com.telenav.kivakit.collections.primitive.array.scalars.*;
import com.telenav.kivakit.collections.primitive.list.store.PackedStringStore;
import com.telenav.kivakit.kernel.scalars.counts.Estimate;
import com.telenav.kivakit.kernel.time.Time;
import com.telenav.kivakit.resource.compression.archive.KivaKitArchivedField;
import com.telenav.kivakit.data.formats.pbf.model.change.*;
import com.telenav.kivakit.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.kivakit.data.formats.pbf.model.tags.compression.PbfTagCodec;
import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.graph.io.archive.GraphArchive;
import com.telenav.kivakit.graph.specifications.common.element.GraphElementAttributes;
import com.telenav.kivakit.graph.specifications.common.element.store.TagStore;
import com.telenav.kivakit.graph.specifications.library.attributes.*;

import static com.telenav.kivakit.graph.Metadata.CountType.ALLOW_ESTIMATE;

@SuppressWarnings("unused")
public class PbfAllNodeMetadata implements AttributeStore
{
    @KivaKitArchivedField
    private SplitLongArray changeSetIdentifier;

    @KivaKitArchivedField
    private SplitLongArray lastModified;

    @KivaKitArchivedField
    private SplitCharArray revisionNumber;

    @KivaKitArchivedField
    private TagStore tags;

    private final AttributeReference<PackedStringStore> USER_NAME =
            new AttributeReference<>(this, GraphElementAttributes.get().PBF_USER_NAME, "userName",
                    () -> new PackedStringStore("userName"));

    @KivaKitArchivedField
    private PackedStringStore userName;

    @KivaKitArchivedField
    private SplitIntArray userIdentifier;

    /** Attribute loader for this store */
    private AttributeLoader loader;

    /** Needed for metadata */
    private final Graph graph;

    private final AttributeReference<SplitLongArray> CHANGE_SET_IDENTIFIER =
            new AttributeReference<>(this, GraphElementAttributes.get().PBF_CHANGE_SET_IDENTIFIER, "changeSetIdentifier",
                    () -> (SplitLongArray) new SplitLongArray("changeSetIdentifier").initialSize(elementCount()));

    private final AttributeReference<SplitLongArray> LAST_MODIFIED =
            new AttributeReference<>(this, GraphElementAttributes.get().LAST_MODIFIED, "lastModified",
                    () -> (SplitLongArray) new SplitLongArray("lastModified").initialSize(elementCount()));

    private final AttributeReference<SplitCharArray> REVISION_NUMBER =
            new AttributeReference<>(this, GraphElementAttributes.get().PBF_REVISION_NUMBER, "revisionNumber",
                    () -> (SplitCharArray) new SplitCharArray("revisionNumber").initialSize(elementCount()));

    private final AttributeReference<TagStore> TAGS =
            new AttributeReference<>(this, GraphElementAttributes.get().TAGS, "tags",
                    () -> new TagStore("tags", graph().metadata().tagCodec()))
            {
                @Override
                protected void onLoaded(final TagStore store)
                {
                    store.codec(graph().metadata().tagCodec());
                    super.onLoaded(store);
                }
            };

    private final AttributeReference<SplitIntArray> USER_IDENTIFIER =
            new AttributeReference<>(this, GraphElementAttributes.get().PBF_USER_IDENTIFIER, "userIdentifier",
                    () -> (SplitIntArray) new SplitIntArray("userIdentifier").initialSize(elementCount()));

    public PbfAllNodeMetadata(final Graph graph)
    {
        this.graph = graph;
    }

    @Override
    public GraphArchive archive()
    {
        return graph.archive();
    }

    /**
     * @return The loader for this attribute store
     */
    @Override
    public AttributeLoader attributeLoader()
    {
        if (loader == null)
        {
            loader = new AttributeLoader(objectName() + ".loader");
        }
        return loader;
    }

    @Override
    public AttributeList attributes()
    {
        return GraphElementAttributes.get();
    }

    @Override
    public Graph graph()
    {
        return graph;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Estimate initialSize()
    {
        return graph().metadata().nodeCount(ALLOW_ESTIMATE).asEstimate();
    }

    @Override
    public String objectName()
    {
        return "node.metadata";
    }

    public PbfTagCodec pbfTagCodec()
    {
        TAGS.load();
        return tags.codec();
    }

    public final void pbfTagCodec(final PbfTagCodec codec)
    {
        tags = new TagStore("PbfNodeMetadata.pbfTags", codec);
    }

    public PbfChangeSetIdentifier retrievePbfChangeSetIdentifier(final GraphElement element)
    {
        return CHANGE_SET_IDENTIFIER.retrieveObject(element, PbfChangeSetIdentifier::new);
    }

    public Time retrievePbfLastModificationTime(final GraphElement element)
    {
        return LAST_MODIFIED.retrieveObject(element, Time::milliseconds);
    }

    public PbfRevisionNumber retrievePbfRevisionNumber(final GraphElement element)
    {
        return REVISION_NUMBER.retrieveObject(element, revision -> new PbfRevisionNumber((int) revision));
    }

    public final PbfTagList retrievePbfTags(final GraphElement element)
    {
        TAGS.load();
        return tags.tagList(element);
    }

    public PbfUserIdentifier retrievePbfUserIdentifier(final GraphElement element)
    {
        return USER_IDENTIFIER.retrieveObject(element, value -> new PbfUserIdentifier((int) value));
    }

    public PbfUserName retrievePbfUserName(final GraphElement element)
    {
        return new PbfUserName(USER_NAME.retrieveString(element));
    }

    @Override
    public int size()
    {
        return lastModified.size();
    }

    /**
     * Stores all of the simple attributes of the given edge at the given edge index
     */
    public void storeAttributes(final GraphElement element)
    {
        CHANGE_SET_IDENTIFIER.storeObject(element, element.pbfChangeSetIdentifier());
        LAST_MODIFIED.storeObject(element, element.lastModificationTime());
        REVISION_NUMBER.storeObject(element, element.pbfRevisionNumber());
        USER_IDENTIFIER.storeObject(element, element.pbfUserIdentifier());
        USER_NAME.storeString(element, element.pbfUserName().name());
        tags.set(element, element.tagList());
    }

    @Override
    public boolean supports(final Attribute<?> attribute)
    {
        return attributes().contains(attribute);
    }

    private Estimate elementCount()
    {
        return graph().metadata().nodeCount(ALLOW_ESTIMATE).asEstimate();
    }
}
