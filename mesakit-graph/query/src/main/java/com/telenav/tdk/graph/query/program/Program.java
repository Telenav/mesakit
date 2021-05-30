package com.telenav.tdk.graph.query.program;

import com.telenav.tdk.core.kernel.debug.Debug;
import com.telenav.tdk.core.kernel.logging.*;
import com.telenav.tdk.core.kernel.scalars.counts.Maximum;
import com.telenav.tdk.graph.*;
import com.telenav.tdk.graph.query.program.statements.Select;

/**
 * A program is a statement, in the form of an abstract syntax tree (AST) which can be evaluated to return a {@link
 * Route} given a starting edge. For example, the {@link Select} statement can be evaluated to select a series of edges
 * matching the select statement's boolean query expression.
 *
 * @author jonathanl (shibo)
 */
public class Program
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    /** The statement to evaluate */
    private final Statement statement;

    /** The most edges that can match a closure (to avoid out-of-control queries) */
    private final Maximum maximumClosureLength;

    /** Stack of edges that are being followed in sequence by a query evaluation */
    private final EdgeStack stack = new EdgeStack("program");

    /**
     * @param statement The statement to evaluate
     * @param maximumClosureLength The maximum length of a closure to avoid lengthy queries
     */
    public Program(final Statement statement, final Maximum maximumClosureLength)
    {
        this.statement = statement;
        this.maximumClosureLength = maximumClosureLength;
        statement.visit(node -> node.program(this));
    }

    public Maximum maximumClosureLength()
    {
        return maximumClosureLength;
    }

    /**
     * Evaluates this program against a starting edge
     *
     * @param start The start edge
     * @return Any route that matches the program or null if none does
     */
    public Route run(final Edge start)
    {
        Node.traceNumber = 1;

        // If program debugging is enabled
        if (DEBUG.isEnabled())
        {
            // and the edge has a certain identifier
            if (start.identifierAsLong() == 256586832000005L)
            {
                // then turn on debugging output
                Node.DEBUG.enable();
            }
            else
            {
                // until the edge has been processed
                EdgeStack.DEBUG.disable();
                Node.DEBUG.disable();
            }
        }

        // Reset the stack and push the starting edge
        stack.clear();
        stack.push(start);

        // then if the statement evaluates to true,
        if (statement.evaluate())
        {
            // the match created by the statement evaluation can be returned
            return stack.match();
        }

        // otherwise if the statement evaluated to false, there is no match.
        return null;
    }

    public EdgeStack stack()
    {
        return stack;
    }

    @Override
    public String toString()
    {
        return statement.toString();
    }
}
