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

package com.telenav.mesakit.graph.io.load;

import com.telenav.kivakit.commandline.ArgumentParser;
import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.progress.ProgressReporter;
import com.telenav.kivakit.core.progress.reporters.BroadcastingProgressReporter;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.interfaces.naming.Named;
import com.telenav.kivakit.core.time.Time;
import com.telenav.kivakit.resource.FileName;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.specifications.common.graph.loader.PbfToGraphConverter;

import static com.telenav.kivakit.core.ensure.Ensure.unsupported;
import static com.telenav.kivakit.resource.compression.archive.ZipArchive.AccessMode.READ;

/**
 * Loads any kind of resource to produce a graph, including:
 * <ul>
 *     <li>.graph - Graph archive</li>
 *     <li>.osm.pbf - Data in OpenStreetMap format</li>
 * </ul>
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings({ "unused", "SpellCheckingInspection" })
public class SmartGraphLoader extends BaseRepeater implements Named
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static boolean accepts(FileName name)
    {
        return name.endsWith(GraphArchive.EXTENSION);
    }

    public static ArgumentParser.Builder<SmartGraphLoader> graphArgumentParser(Listener listener, String description)
    {
        return graphArgumentParser(listener, description, PbfToGraphConverter.defaultConfiguration());
    }

    public static ArgumentParser.Builder<SmartGraphLoader> graphArgumentParser(Listener listener,
                                                                               String description,
                                                                               PbfToGraphConverter.Configuration configuration)
    {
        return ArgumentParser.builder(SmartGraphLoader.class)
                .description(description)
                .converter(new Converter(listener, configuration));
    }

    public static SwitchParser.Builder<SmartGraphLoader> graphSwitchParser(Listener listener, String name,
                                                                           String description)
    {
        return graphSwitchParser(listener, name, description, PbfToGraphConverter.defaultConfiguration());
    }

    public static SwitchParser.Builder<SmartGraphLoader> graphSwitchParser(Listener listener,
                                                                           String name,
                                                                           String description,
                                                                           PbfToGraphConverter.Configuration configuration)
    {
        return SwitchParser.builder(SmartGraphLoader.class)
                .name(name)
                .description(description)
                .converter(new Converter(LOGGER, configuration));
    }

    public static SwitchParser.Builder<SmartGraphLoader> graphSwitchParser(Listener listener,
                                                                           PbfToGraphConverter.Configuration configuration)
    {
        return graphSwitchParser(listener, "graph", "Graph file resource", configuration);
    }

    public static SwitchParser.Builder<SmartGraphLoader> graphSwitchParser(Listener listener)
    {
        return graphSwitchParser(listener, "graph", "Graph file resource", PbfToGraphConverter.defaultConfiguration());
    }

    public static SmartGraphLoader parse(Listener listener, String specifier)
    {
        return new Converter(listener, PbfToGraphConverter.defaultConfiguration()).convert(specifier);
    }

    /**
     * Converts to/from string representations of resources
     *
     * @author jonathanl (shibo)
     */
    public static class Converter extends BaseStringConverter<SmartGraphLoader>
    {
        private final PbfToGraphConverter.Configuration configuration;

        public Converter(Listener listener, PbfToGraphConverter.Configuration configuration)
        {
            super(listener);
            this.configuration = configuration;
        }

        @Override
        protected String onToString(SmartGraphLoader graph)
        {
            return unsupported();
        }

        @Override
        protected SmartGraphLoader onToValue(String specifier)
        {
            return new SmartGraphLoader(File.parseFile(this, specifier), configuration);
        }
    }

    private final PbfToGraphConverter.Configuration configuration;

    private final File file;

    public SmartGraphLoader(File file)
    {
        this(file, PbfToGraphConverter.defaultConfiguration());
    }

    public SmartGraphLoader(File file, PbfToGraphConverter.Configuration configuration)
    {
        this.file = file;
        this.configuration = configuration;
    }

    public Time modifiedAt()
    {
        return file.lastModified();
    }

    public Graph load()
    {
        return load(this, ProgressReporter.nullProgressReporter());
    }

    public Graph load(Listener listener)
    {
        return load(listener, BroadcastingProgressReporter.createProgressReporter(LOGGER, "bytes"));
    }

    @SuppressWarnings("resource")
    public Graph load(Listener listener, ProgressReporter reporter)
    {
        // If the file we're trying to load exists,
        if (file.exists())
        {
            // get the file extension
            var extension = file.extension();

            // and load the file accordingly
            switch (extension.toString())
            {
                case ".graph":
                    return new GraphArchive(listener, file, READ, reporter).loadAll(listener);

                case ".pbf":
                {
                    var metadata = Metadata.from(file);
                    if (metadata != null)
                    {
                        var converter = (PbfToGraphConverter) metadata.dataSpecification().newGraphConverter(metadata);
                        converter.addListener(this);
                        converter.configure(configuration);
                        return converter.convert(file);
                    }
                    problem("PBF file '$' does not contain metadata. Use mesakit-tools to add metadata.", file);
                    return null;
                }

                default:
                    throw new IllegalStateException("Unrecognized graph resource: " + file);
            }
        }
        warning("File '$' does not exist", file);
        return null;
    }

    @Override
    public String name()
    {
        return file.fileName().name();
    }

    @Override
    public String toString()
    {
        return name();
    }
}
