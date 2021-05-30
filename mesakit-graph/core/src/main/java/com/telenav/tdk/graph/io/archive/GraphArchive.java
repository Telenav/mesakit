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

package com.telenav.tdk.graph.io.archive;

import com.esotericsoftware.kryo.Kryo;
import com.telenav.tdk.core.filesystem.*;
import com.telenav.tdk.core.kernel.commandline.*;
import com.telenav.tdk.core.kernel.conversion.string.BaseStringConverter;
import com.telenav.tdk.core.kernel.debug.Debug;
import com.telenav.tdk.core.kernel.interfaces.naming.Named;
import com.telenav.tdk.core.kernel.language.io.serialization.TdkSerializer;
import com.telenav.tdk.core.kernel.language.primitive.Doubles;
import com.telenav.tdk.core.kernel.language.vm.JavaVirtualMachine;
import com.telenav.tdk.core.kernel.logging.*;
import com.telenav.tdk.core.kernel.messaging.*;
import com.telenav.tdk.core.kernel.operation.progress.ProgressReporter;
import com.telenav.tdk.core.kernel.scalars.bytes.Bytes;
import com.telenav.tdk.core.kernel.scalars.versioning.*;
import com.telenav.tdk.core.kernel.time.Time;
import com.telenav.tdk.core.kernel.validation.Validation;
import com.telenav.tdk.core.resource.Resource;
import com.telenav.tdk.core.resource.compression.archive.*;
import com.telenav.tdk.core.resource.path.*;
import com.telenav.tdk.graph.*;
import com.telenav.tdk.graph.collections.GraphList;
import com.telenav.tdk.graph.io.load.SmartGraphLoader;
import com.telenav.tdk.graph.metadata.DataSupplier;
import com.telenav.tdk.graph.specifications.library.store.GraphStore;

import java.util.zip.ZipFile;

import static com.telenav.tdk.core.kernel.validation.Validate.*;

/**
 * A graph archive is a {@link ZipArchive} which is based on the support in java.util for {@link ZipFile}s. When a
 * {@link Graph} is saved to a {@link GraphArchive}, the contents of its associated {@link GraphStore} is serialized
 * with {@link Kryo} into a set of {@link ZipEntry} streams in the zip archive. You can see these streams with "unzip
 * -v" on the command line. The graph archive can be loaded again at a later time by reading the serialized data.
 * <p>
 * This class handles the implementation details of saving and loading data from a graph archive. To save a graph, to a
 * {@link GraphArchive} it is preferable to call {@link Graph#save(GraphArchive)}. To load a graph, call {@link
 * GraphArchive#load(Listener)} or use the {@link SmartGraphLoader}, which supports loading graphs from all supported
 * formats, including graph archives.
 *
 * @author jonathanl (shibo)
 * @see Graph
 * @see SmartGraphLoader
 * @see ZipArchive
 */
public class GraphArchive extends FieldArchive implements Named
{
    /** The current graph archive version */
    public static final Version VERSION = Version.parse("8.0.0");

    /** The extension for a graph archive */
    public static final Extension EXTENSION = Extension.GRAPH;

    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static SwitchParser.Builder<Graph> GRAPH = switchParser("graph", "Input graph file");

    public static SwitchParser.Builder<GraphList> GRAPH_LIST = listSwitchParser("graphs",
            "A comma separated list of graph files and/or folders");

    private static final Debug DEBUG = new Debug(LOGGER);

    public static boolean accepts(final FileName name)
    {
        return name.endsWith(EXTENSION);
    }

    public static ArgumentParser.Builder<Graph> argumentParser(final String description)
    {
        return ArgumentParser.builder(Graph.class).description(description).converter(new GraphArchive.Converter(LOGGER, ProgressReporter.NULL));
    }

    public static Resource forSpecifier(final String specifier)
    {
        for (final var current : DataSupplier.values())
        {
            final var prefix = current + ":";
            if (specifier.toUpperCase().startsWith(prefix))
            {
                return Resource.of(specifier.substring(prefix.length()));
            }
        }
        return Resource.of(specifier);
    }

    public static SwitchParser.Builder<GraphList> listSwitchParser(final String name, final String description)
    {
        return SwitchParser.builder(GraphList.class).name(name).description(description)
                .converter(new GraphArchive.ListConverter(LOGGER));
    }

    public static SwitchParser.Builder<Graph> switchParser(final String name, final String description)
    {
        return SwitchParser.builder(Graph.class).name(name).description(description).converter(new Converter(LOGGER, ProgressReporter.NULL));
    }

    public static class Converter extends BaseStringConverter<Graph>
    {
        private final ProgressReporter reporter;

        public Converter(final Listener<Message> listener, final ProgressReporter reporter)
        {
            super(listener);
            this.reporter = reporter;
        }

        @SuppressWarnings("resource")
        @Override
        protected Graph onConvertToObject(final String path)
        {
            final var file = new File.Converter(this).convert(path);
            if (file != null)
            {
                return new GraphArchive(file, reporter, ZipArchive.Mode.READ).load(this);
            }
            LOGGER.warning("Unable to load graph archive '$'", path);
            return null;
        }

        @Override
        protected String onConvertToString(final Graph graph)
        {
            return unsupported();
        }
    }

    public static class ListConverter extends BaseStringConverter<GraphList>
    {
        public ListConverter(final Listener<Message> listener)
        {
            super(listener);
        }

        @Override
        protected GraphList onConvertToObject(final String value)
        {
            final var files = new FileList.Converter(this, Extension.GRAPH).convert(value);
            if (files != null)
            {
                return new GraphList(files);
            }
            LOGGER.warning("Unable to load graph file(s) '$'", value);
            return null;
        }

        @Override
        protected String onConvertToString(final GraphList graph)
        {
            return unsupported();
        }
    }

    public GraphArchive(final File file, final ProgressReporter reporter, final ZipArchive.Mode mode)
    {
        super(file, reporter, mode);
    }

    public Time lastModified()
    {
        return resource().lastModified();
    }

    public Graph load(final Listener<Message> listener)
    {
        final var metadata = ensureNotNull(metadata());

        DEBUG.trace("Loading $ from $", metadata, resource());
        final var graph = metadata.dataSpecification().newGraph(metadata.withName(resource().fileName().name()));
        graph.broadcastTo(listener);
        graph.load(this);

        if (DEBUG.isEnabled() && JavaVirtualMachine.local().instrument())
        {
            graph.loadAll();
            final var memory = JavaVirtualMachine.local().sizeOfObjectGraph(graph, "GraphResource.load.graph",
                    Bytes.megabytes(1));
            final var disk = resource().size();
            DEBUG.trace("Graph memory = $, disk = $, memory/disk = $%", memory, disk,
                    Doubles.toString((double) memory.asBytes() / (double) disk.asBytes() * 100.0, 1));
        }
        return graph;
    }

    public Graph loadAll(final Listener<Message> listener)
    {
        final var graph = load(listener);
        graph.loadAll();
        return graph;
    }

    public Metadata metadata()
    {
        final VersionedObject<Metadata> metadata = zip().load(TdkSerializer.threadSerializer(LOGGER), "metadata");
        return metadata == null ? null : metadata.get();
    }

    @Override
    public String name()
    {
        final var resource = resource();
        return resource == null ? objectName() : resource.fileName().name();
    }

    @Override
    public String objectName()
    {
        return "graph.archive";
    }

    public Resource resource()
    {
        final var zip = zip();
        return zip == null ? null : zip.resource();
    }

    public void saveMetadata(final Metadata metadata)
    {
        metadata.assertValid(Validation.VALIDATE_ALL);
        zip().save(TdkSerializer.threadSerializer(LOGGER), "metadata", new VersionedObject<>(VERSION, metadata));
    }

    @Override
    public String toString()
    {
        return name();
    }
}
