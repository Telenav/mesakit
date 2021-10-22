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

package com.telenav.mesakit.graph.io.archive;

import com.esotericsoftware.kryo.Kryo;
import com.telenav.kivakit.commandline.ArgumentParser;
import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.FileList;
import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.data.validation.ValidationType;
import com.telenav.kivakit.kernel.interfaces.naming.Named;
import com.telenav.kivakit.kernel.language.primitives.Doubles;
import com.telenav.kivakit.kernel.language.progress.ProgressReporter;
import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.kivakit.kernel.language.values.count.Bytes;
import com.telenav.kivakit.kernel.language.values.version.Version;
import com.telenav.kivakit.kernel.language.values.version.VersionedObject;
import com.telenav.kivakit.kernel.language.vm.JavaVirtualMachine;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Debug;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.resource.Resource;
import com.telenav.kivakit.resource.compression.archive.FieldArchive;
import com.telenav.kivakit.resource.compression.archive.ZipArchive;
import com.telenav.kivakit.resource.compression.archive.ZipEntry;
import com.telenav.kivakit.resource.path.Extension;
import com.telenav.kivakit.resource.path.FileName;
import com.telenav.kivakit.serialization.core.SerializationSession;
import com.telenav.kivakit.serialization.core.SerializationSessionFactory;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.collections.GraphList;
import com.telenav.mesakit.graph.io.load.SmartGraphLoader;
import com.telenav.mesakit.graph.metadata.DataSupplier;
import com.telenav.mesakit.graph.specifications.library.store.GraphStore;

import java.util.zip.ZipFile;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensureNotNull;
import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.unsupported;

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
    public static final Version VERSION = Version.parse("0.9.8");

    /** The extension for a graph archive */
    public static final Extension EXTENSION = Extension.GRAPH;

    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static SwitchParser.Builder<Graph> GRAPH = graphArchiveSwitchParser("graph", "Input graph file");

    public static SwitchParser.Builder<GraphList> GRAPH_LIST = graphListSwitchParser("graphs",
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
                return Resource.resolve(Listener.console(), specifier.substring(prefix.length()));
            }
        }
        return Resource.resolve(Listener.console(), specifier);
    }

    public static SwitchParser.Builder<Graph> graphArchiveSwitchParser(final String name, final String description)
    {
        return SwitchParser.builder(Graph.class).name(name).description(description).converter(new Converter(LOGGER, ProgressReporter.NULL));
    }

    public static SwitchParser.Builder<GraphList> graphListSwitchParser(final String name, final String description)
    {
        return SwitchParser.builder(GraphList.class).name(name).description(description)
                .converter(new GraphArchive.ListConverter(LOGGER));
    }

    public static class Converter extends BaseStringConverter<Graph>
    {
        private final ProgressReporter reporter;

        public Converter(final Listener listener, final ProgressReporter reporter)
        {
            super(listener);
            this.reporter = reporter;
        }

        @Override
        protected String onToString(final Graph graph)
        {
            return unsupported();
        }

        @SuppressWarnings("resource")
        @Override
        protected Graph onToValue(final String path)
        {
            final var file = new File.Converter(this).convert(path);
            if (file != null)
            {
                return new GraphArchive(this, file, ZipArchive.Mode.READ, reporter).load(this);
            }
            LOGGER.warning("Unable to load graph archive '$'", path);
            return null;
        }
    }

    public static class ListConverter extends BaseStringConverter<GraphList>
    {
        public ListConverter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected String onToString(final GraphList graph)
        {
            return unsupported();
        }

        @Override
        protected GraphList onToValue(final String value)
        {
            final var files = new FileList.Converter(this, Extension.GRAPH).convert(value);
            if (files != null)
            {
                return new GraphList(files);
            }
            LOGGER.warning("Unable to load graph file(s) '$'", value);
            return null;
        }
    }

    public GraphArchive(final Listener listener, final File file, final ZipArchive.Mode mode,
                        final ProgressReporter reporter)
    {
        super(file, SerializationSessionFactory.threadLocal(), reporter, mode);
        listener.listenTo(this);
    }

    public Time lastModified()
    {
        return resource().lastModified();
    }

    public Graph load(final Listener listener)
    {
        final var metadata = ensureNotNull(metadata());

        DEBUG.trace("Loading $ from $", metadata, resource());
        final var graph = metadata.dataSpecification().newGraph(metadata.withName(resource().fileName().name()));
        graph.addListener(listener);
        graph.load(this);

        if (DEBUG.isDebugOn() && JavaVirtualMachine.local().instrument())
        {
            graph.loadAll();
            final var memory = JavaVirtualMachine.local().sizeOfObjectGraph(graph, "GraphResource.load.graph",
                    Bytes.megabytes(1));
            final var disk = resource().sizeInBytes();
            DEBUG.trace("Graph memory = $, disk = $, memory/disk = $%", memory, disk,
                    Doubles.format((double) memory.asBytes() / (double) disk.asBytes() * 100.0, 1));
        }
        return graph;
    }

    public Graph loadAll(final Listener listener)
    {
        final var graph = load(listener);
        graph.loadAll();
        return graph;
    }

    public Metadata metadata()
    {
        final VersionedObject<Metadata> metadata = zip().load(SerializationSession.threadLocal(LOGGER), "metadata");
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
        metadata.assertValid(ValidationType.VALIDATE_ALL);
        zip().save(SerializationSession.threadLocal(LOGGER), "metadata", new VersionedObject<>(VERSION, metadata));
    }

    @Override
    public String toString()
    {
        return name();
    }
}
