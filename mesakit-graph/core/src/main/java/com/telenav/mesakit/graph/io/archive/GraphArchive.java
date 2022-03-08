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
import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.language.primitive.Doubles;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.progress.ProgressReporter;
import com.telenav.kivakit.core.time.Time;
import com.telenav.kivakit.core.value.count.Bytes;
import com.telenav.kivakit.core.version.Version;
import com.telenav.kivakit.core.version.VersionedObject;
import com.telenav.kivakit.core.vm.JavaVirtualMachine;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.FileList;
import com.telenav.kivakit.interfaces.naming.Named;
import com.telenav.kivakit.resource.Resource;
import com.telenav.kivakit.resource.compression.archive.FieldArchive;
import com.telenav.kivakit.resource.compression.archive.ZipArchive;
import com.telenav.kivakit.resource.compression.archive.ZipEntry;
import com.telenav.kivakit.resource.path.Extension;
import com.telenav.kivakit.resource.path.FileName;
import com.telenav.kivakit.serialization.core.SerializationSession;
import com.telenav.kivakit.serialization.core.SerializationSessionFactory;
import com.telenav.kivakit.validation.ValidationType;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.collections.GraphList;
import com.telenav.mesakit.graph.io.load.SmartGraphLoader;
import com.telenav.mesakit.graph.metadata.DataSupplier;
import com.telenav.mesakit.graph.specifications.library.store.GraphStore;

import java.util.zip.ZipFile;

import static com.telenav.kivakit.core.ensure.Ensure.ensureNotNull;
import static com.telenav.kivakit.core.ensure.Ensure.unsupported;

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
    public static final Version VERSION = Version.version("0.9.8");

    /** The extension for a graph archive */
    public static final Extension EXTENSION = Extension.GRAPH;

    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static SwitchParser.Builder<Graph> GRAPH = graphArchiveSwitchParser("graph", "Input graph file");

    public static SwitchParser.Builder<GraphList> GRAPH_LIST = graphListSwitchParser("graphs",
            "A comma separated list of graph files and/or folders");

    private static final Debug DEBUG = new Debug(LOGGER);

    public static boolean accepts(FileName name)
    {
        return name.endsWith(EXTENSION);
    }

    public static ArgumentParser.Builder<Graph> argumentParser(String description)
    {
        return ArgumentParser.builder(Graph.class).description(description).converter(new GraphArchive.Converter(LOGGER, ProgressReporter.none()));
    }

    public static Resource forSpecifier(Listener listener, String specifier)
    {
        for (var current : DataSupplier.values())
        {
            var prefix = current + ":";
            if (specifier.toUpperCase().startsWith(prefix))
            {
                return Resource.resolve(listener, specifier.substring(prefix.length()));
            }
        }
        return Resource.resolve(listener, specifier);
    }

    public static SwitchParser.Builder<Graph> graphArchiveSwitchParser(String name, String description)
    {
        return SwitchParser.builder(Graph.class)
                .name(name)
                .description(description)
                .converter(new Converter(LOGGER, ProgressReporter.none()));
    }

    public static SwitchParser.Builder<GraphList> graphListSwitchParser(String name, String description)
    {
        return SwitchParser.builder(GraphList.class)
                .name(name)
                .description(description)
                .converter(new GraphArchive.ListConverter(LOGGER));
    }

    public static class Converter extends BaseStringConverter<Graph>
    {
        private final ProgressReporter reporter;

        public Converter(Listener listener, ProgressReporter reporter)
        {
            super(listener);
            this.reporter = reporter;
        }

        @Override
        protected String onToString(Graph graph)
        {
            return unsupported();
        }

        @SuppressWarnings("resource")
        @Override
        protected Graph onToValue(String path)
        {
            var file = new File.Converter(this).convert(path);
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
        public ListConverter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected String onToString(GraphList graph)
        {
            return unsupported();
        }

        @Override
        protected GraphList onToValue(String value)
        {
            var files = new FileList.Converter(this, Extension.GRAPH).convert(value);
            if (files != null)
            {
                return new GraphList(files);
            }
            LOGGER.warning("Unable to load graph file(s) '$'", value);
            return null;
        }
    }

    public GraphArchive(Listener listener, File file, ZipArchive.Mode mode,
                        ProgressReporter reporter)
    {
        super(file, SerializationSessionFactory.threadLocal(), reporter, mode);
        listener.listenTo(this);
    }

    public Time lastModified()
    {
        return resource().lastModified();
    }

    public Graph load(Listener listener)
    {
        var metadata = ensureNotNull(metadata());

        DEBUG.trace("Loading $ from $", metadata, resource());
        var graph = metadata.dataSpecification().newGraph(metadata.withName(resource().fileName().name()));
        graph.addListener(listener);
        graph.load(this);

        if (DEBUG.isDebugOn() && JavaVirtualMachine.local().instrument())
        {
            graph.loadAll();
            var memory = JavaVirtualMachine.local().sizeOfObjectGraph(graph, "GraphResource.load.graph",
                    Bytes.megabytes(1));
            var disk = resource().sizeInBytes();
            DEBUG.trace("Graph memory = $, disk = $, memory/disk = $%", memory, disk,
                    Doubles.format((double) memory.asBytes() / (double) disk.asBytes() * 100.0, 1));
        }
        return graph;
    }

    public Graph loadAll(Listener listener)
    {
        var graph = load(listener);
        graph.loadAll();
        return graph;
    }

    public Metadata metadata()
    {
        VersionedObject<Metadata> metadata = zip().load("metadata", SerializationSession.threadLocal(LOGGER));
        return metadata == null ? null : metadata.object();
    }

    @Override
    public String name()
    {
        var resource = resource();
        return resource == null ? objectName() : resource.fileName().name();
    }

    @Override
    public String objectName()
    {
        return "graph.archive";
    }

    public Resource resource()
    {
        var zip = zip();
        return zip == null ? null : zip.resource();
    }

    public void saveMetadata(Metadata metadata)
    {
        metadata.assertValid(ValidationType.VALIDATE_ALL);
        zip().save("metadata", SerializationSession.threadLocal(LOGGER), new VersionedObject<>(VERSION, metadata));
    }

    @Override
    public String toString()
    {
        return name();
    }
}
