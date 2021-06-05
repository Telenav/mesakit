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
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.interfaces.naming.Named;
import com.telenav.kivakit.kernel.language.progress.ProgressReporter;
import com.telenav.kivakit.kernel.language.progress.reporters.Progress;
import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.resource.path.FileName;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.specifications.common.graph.loader.PbfToGraphConverter;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.illegalState;
import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.unsupported;
import static com.telenav.kivakit.resource.compression.archive.ZipArchive.Mode.READ;

/**
 * Loads any kind of resource to produce a graph, including:
 * <ul>
 *     <li>.graph - Graph archive</li>
 *     <li>.osm.pbf - Data in OpenStreetMap format</li>
 * </ul>
 *
 * @author jonathanl (shibo)
 */
public class SmartGraphLoader extends BaseRepeater implements Named
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static boolean accepts(final FileName name)
    {
        return name.endsWith(GraphArchive.EXTENSION);
    }

    public static ArgumentParser.Builder<SmartGraphLoader> graphArgumentParser(final String description)
    {
        return graphArgumentParser(description, PbfToGraphConverter.defaultConfiguration());
    }

    public static ArgumentParser.Builder<SmartGraphLoader> graphArgumentParser(final String description,
                                                                               final PbfToGraphConverter.Configuration configuration)
    {
        return ArgumentParser.builder(SmartGraphLoader.class)
                .description(description)
                .converter(new Converter(LOGGER, configuration));
    }

    public static SwitchParser.Builder<SmartGraphLoader> graphSwitchParser(final String name, final String description)
    {
        return graphSwitchParser(name, description, PbfToGraphConverter.defaultConfiguration());
    }

    public static SwitchParser.Builder<SmartGraphLoader> graphSwitchParser(final String name, final String description,
                                                                           final PbfToGraphConverter.Configuration configuration)
    {
        return SwitchParser.builder(SmartGraphLoader.class)
                .name(name)
                .description(description)
                .converter(new Converter(LOGGER, configuration));
    }

    public static SwitchParser.Builder<SmartGraphLoader> graphSwitchParser(
            final PbfToGraphConverter.Configuration configuration)
    {
        return graphSwitchParser("graph", "Graph file resource", configuration);
    }

    public static SwitchParser.Builder<SmartGraphLoader> graphSwitchParser()
    {
        return graphSwitchParser("graph", "Graph file resource", PbfToGraphConverter.defaultConfiguration());
    }

    public static SmartGraphLoader of(final String specifier)
    {
        return new Converter(LOGGER, PbfToGraphConverter.defaultConfiguration()).convert(specifier);
    }

    /**
     * Converts to/from string representations of resources
     *
     * @author jonathanl (shibo)
     */
    public static class Converter extends BaseStringConverter<SmartGraphLoader>
    {
        private final PbfToGraphConverter.Configuration configuration;

        public Converter(final Listener listener, final PbfToGraphConverter.Configuration configuration)
        {
            super(listener);
            this.configuration = configuration;
        }

        @Override
        protected SmartGraphLoader onConvertToObject(final String specifier)
        {
            return new SmartGraphLoader(File.parse(specifier), configuration);
        }

        @Override
        protected String onConvertToString(final SmartGraphLoader graph)
        {
            return unsupported();
        }
    }

    private final File file;

    private final PbfToGraphConverter.Configuration configuration;

    public SmartGraphLoader(final File file)
    {
        this(file, PbfToGraphConverter.defaultConfiguration());
    }

    public SmartGraphLoader(final File file, final PbfToGraphConverter.Configuration configuration)
    {
        this.file = file;
        this.configuration = configuration;
    }

    public Time lastModified()
    {
        return file.lastModified();
    }

    public Graph load()
    {
        return load(this, ProgressReporter.NULL);
    }

    public Graph load(final Listener listener)
    {
        return load(listener, Progress.create(LOGGER, "bytes"));
    }

    @SuppressWarnings("resource")
    public Graph load(final Listener listener, final ProgressReporter reporter)
    {
        // If the file we're trying to load exists,
        if (file.exists())
        {
            // get the file extension
            final var extension = file.extension();

            // and load the file accordingly
            switch (extension.toString())
            {
                case ".graph":
                    return new GraphArchive(file, READ, reporter).loadAll(listener);

                case ".pbf":
                {
                    final var metadata = Metadata.from(file);
                    if (metadata != null)
                    {
                        final var converter = (PbfToGraphConverter) metadata.dataSpecification().newGraphConverter(metadata);
                        converter.addListener(this);
                        converter.configure(configuration);
                        return converter.convert(file);
                    }
                    problem("PBF file '$' does not contain metadata. Use mesakit-tools.sh to add metadata.", file);
                    return null;
                }

                default:
                    return illegalState("Unrecognized graph resource: $", file);
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
