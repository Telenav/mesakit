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

package com.telenav.kivakit.graph.traffic.roadsection.loaders.csv;

import com.telenav.kivakit.kernel.language.iteration.BaseIterator;
import com.telenav.kivakit.kernel.language.matching.All;
import com.telenav.kivakit.kernel.logging.*;
import com.telenav.kivakit.kernel.messaging.Message;
import com.telenav.kivakit.kernel.messaging.messages.MessageList;
import com.telenav.kivakit.kernel.messaging.messages.status.Problem;
import com.telenav.kivakit.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.kernel.operation.progress.ProgressReporter;
import com.telenav.kivakit.resource.Resource;
import com.telenav.kivakit.data.formats.library.csv.CsvReader;
import com.telenav.kivakit.graph.traffic.roadsection.*;

import java.util.Iterator;

import static com.telenav.kivakit.kernel.messaging.Message.Status.COMPLETED;

/**
 * Loader for a CSV road section database
 *
 * @author pierrem
 * @author matthieun
 * @author jonathanl (shibo)
 */
public class CsvRoadSectionLoader extends BaseRepeater<Message> implements RoadSectionLoader
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private final Resource resource;

    private final ProgressReporter reporter;

    public CsvRoadSectionLoader(final Resource resource, final ProgressReporter reporter)
    {
        this.resource = resource;
        this.reporter = reporter;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<RoadSection> iterator()
    {
        return new BaseIterator<>()
        {
            private final CsvReader reader = listenTo(new CsvReader(resource, reporter, RoadSection.CSV_SCHEMA, ','));

            private final MessageList<Message> messages = new MessageList<>(new All<>());

            @Override
            protected RoadSection onNext()
            {
                if (!reader.hasNext())
                {
                    // done with the file
                    reader.close();
                    return null;
                }
                else
                {
                    // read the next line
                    final var line = reader.next();
                    try
                    {
                        final var section = new RoadSection(line, messages);

                        // Force code to be loaded
                        section.identifier().asCode();

                        // If there's a failure loading the line
                        if (messages.countWorseThan(COMPLETED).isNonZero())
                        {
                            // report it
                            LOGGER.warning("Unable to load ${debug} (${debug}): ${debug}",
                                    resource.fileName(), line.lineNumber(), line);
                            for (final var message : messages)
                            {
                                LOGGER.log(message);
                            }
                        }
                        messages.clear();

                        return section;
                    }
                    catch (final Throwable e)
                    {
                        throw new Problem(e, "OperationFailed loading road sections from ${debug} at line ${debug}",
                                resource.fileName(), reader.lineNumber()).asException();
                    }
                }
            }
        };
    }
}
