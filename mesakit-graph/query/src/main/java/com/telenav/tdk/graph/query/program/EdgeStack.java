package com.telenav.kivakit.graph.query.program;

import com.telenav.kivakit.kernel.debug.Debug;
import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.string.conversion.StringFormat;
import com.telenav.kivakit.kernel.logging.*;
import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.graph.collections.EdgeSet;

import java.util.*;

import static com.telenav.kivakit.graph.query.program.EdgeStack.Result.*;

/**
 * A stack of edges used when evaluating graph query expressions
 *
 * @author jonathanl (shibo)
 */
public class EdgeStack
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    static final Debug DEBUG = new Debug(LOGGER);

    public enum Result
    {
        /** The edge was pushed onto the stack */
        PUSHED,

        /** The edge was already pushed onto the stack at some point making exploration of edges circular */
        ALREADY_ON_STACK
    }

    /** The stack of edges */
    private final LinkedList<Edge> stack = new LinkedList<>();

    /** Any matching route formed from the stack of edges */
    private Route match;

    /** The name of this edge stack */
    private final String name;

    public EdgeStack(final String name)
    {
        this.name = name;
    }

    public void clear()
    {
        stack.clear();
    }

    /**
     * @return Returns the route that matched the query
     */
    public Route match()
    {
        return match;
    }

    /**
     * Saves the stack as a match
     */
    public void matched()
    {
        match = new EdgeSet(new HashSet<>(stack)).asRoute();
        DEBUG.trace("$: matched $", name, match);
    }

    /**
     * Pushes an edge on the stack, performing a check as to whether the edge was already pushed onto the stack at a
     * previous point. This can be used to detect cycles when recursively traversing edges.
     *
     * @param edge The edge to push onto the stack
     * @return The result of pushing the edge, either {@link Result#PUSHED} if successful or {@link
     * Result#ALREADY_ON_STACK} if the edge has already been pushed on the stack in the past
     */
    public Result push(final Edge edge)
    {
        if (stack.contains(edge))
        {
            return ALREADY_ON_STACK;
        }
        stack.push(edge);
        DEBUG.trace("$: push(${long}) => $", name, edge.identifierAsLong(), toString());
        return PUSHED;
    }

    /**
     * Push all the edges in the given route, in order
     */
    public Result push(final Route route)
    {
        for (final var edge : route)
        {
            if (stack.contains(edge))
            {
                return ALREADY_ON_STACK;
            }
            stack.push(edge);
        }
        DEBUG.trace("$: push(${long}) => $", name, route, toString());
        return PUSHED;
    }

    /**
     * @return The edges reachable from the to vertex of the edge on top of the stack
     */
    public EdgeSet reachableEdges()
    {
        return top().outEdgesWithoutReversed();
    }

    /**
     * @return The route from the given stack index to the top of the stack
     */
    public Route route(final int index)
    {
        final var set = new EdgeSet();
        final var iterator = stack.listIterator(index);
        while (iterator.hasNext())
        {
            set.add(iterator.next());
        }
        return set.asRoute();
    }

    /**
     * @return The number of elements on the stack
     */
    public int size()
    {
        return stack.size();
    }

    @Override
    public String toString()
    {
        if (size() == 0)
        {
            return "[empty]";
        }
        return ObjectList.from(stack).reversed().mapped(edge -> edge.asString(StringFormat.PROGRAMMATIC)).join(":") + " [top]";
    }

    /**
     * @return The edge on top of the stack
     */
    public Edge top()
    {
        return stack.getFirst();
    }

    /**
     * Removes all elements on the stack beyond the given index
     */
    public void unwind(final int index)
    {
        while (stack.size() > index)
        {
            stack.pop();
        }
        DEBUG.trace("$: unwind(${integer}) => $", name, index, toString());
    }
}
