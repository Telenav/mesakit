package com.telenav.mesakit.graph.query;

import com.telenav.kivakit.kernel.interfaces.code.Callback;
import com.telenav.kivakit.kernel.language.collections.set.Sets;
import com.telenav.kivakit.kernel.language.progress.ProgressReporter;
import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.collections.EdgeSequence;
import com.telenav.mesakit.graph.query.antlr.GraphQueryLexer;
import com.telenav.mesakit.graph.query.antlr.GraphQueryParser;
import com.telenav.mesakit.graph.query.compiler.GraphQueryCompiler;
import com.telenav.mesakit.graph.query.compiler.GraphQueryErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.HashSet;
import java.util.Set;

/**
 * Evaluates a sequence of candidate edges against a query, return up to the given maximum number of matches.
 *
 * @author jonathanl (shibo)
 */
public class GraphQuery
{
    private volatile boolean stop;

    /**
     * Selects from a sequence of edges those that match the query.
     *
     * @param candidates The candidate edges to match against the query
     * @param query The query string
     * @param maximumMatches The maximum number of matching edges to return
     * @param errorHandler Callback for receiving error messages
     * @return The set of candidate edges matching the query
     */
    public Set<Route> execute(final ProgressReporter reporter,
                              final EdgeSequence candidates,
                              final String query,
                              final Maximum maximumMatches,
                              final Callback<String> errorHandler)
    {
        // Start the progress reporter,
        reporter.steps(candidates.count().asMaximum());
        reporter.start();

        // create a lexer for query,
        final var lexer = new GraphQueryLexer(CharStreams.fromString(query));

        // parse the lexer's token stream,
        final var parser = new GraphQueryParser(new CommonTokenStream(lexer));
        final var listener = new GraphQueryErrorListener(errorHandler);
        parser.addErrorListener(listener);
        final var queryParseTree = parser.select();

        // and if an error was reported
        if (listener.error())
        {
            // return nothing
            return Sets.empty();
        }

        // or if the parser ran out of input
        if (!parser.isMatchedEOF())
        {
            // report that
            errorHandler.callback("Parser did not match all input in query expression");
            return Sets.empty();
        }

        // or if there's a syntax error
        if (parser.getNumberOfSyntaxErrors() > 0)
        {
            // return an empty set
            errorHandler.callback("Syntax error");
            return Sets.empty();
        }

        // otherwise, create a query compiler,
        final var compiler = new GraphQueryCompiler();

        // build the query program to run,
        final var program = compiler.compile(queryParseTree, Maximum._8);

        // then go through all the candidate edges
        final var matches = new HashSet<Route>();
        for (final var candidate : candidates)
        {
            if (stop)
            {
                break;
            }

            // and if running the program on the candidate results in a match
            final var match = program.run(candidate);
            if (match != null)
            {
                // then add the route to the set of matches
                matches.add(match);

                // and if we have found enough matches already,
                if (matches.size() == maximumMatches.asInt())
                {
                    // then stop looking and return them
                    return matches;
                }
            }
            reporter.next();
        }
        reporter.end();

        return matches;
    }

    public void stop()
    {
        stop = true;
    }
}