package com.telenav.kivakit.graph.query.program.expressions.closure;

import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.graph.query.program.*;

import java.util.*;

import static com.telenav.kivakit.graph.query.program.EdgeStack.Result.PUSHED;

/**
 * Evaluates a positive closure, [boolean-expression]+
 *
 * @author jonathanl (shibo)
 */
public class OneOrMore extends Node implements BooleanExpression
{
    /** The expression that is closed */
    private final BooleanExpression expression;

    /** The edge for which the closure iterator is valid */
    private Edge top;

    /** The current iterator over the matches of the closure expression */
    private Iterator<Route> iterator;

    /** Size of stack before evaluation */
    private int stackIndex;

    public OneOrMore(final BooleanExpression expression)
    {
        this.expression = expression;
    }

    @Override
    public boolean canEvaluateAgain()
    {
        // Get the edge on top of the stack to evaluate
        final var top = stack().top();

        // and if there is no iterator or the top edge has changed
        if (iterator == null || !top.equals(this.top))
        {
            // then save the top edge
            this.top = top;

            // and create an iterator for the current top edge
            iterator = iterator(top);

            // then save the current stack size for use in evaluation
            stackIndex = stack().size();
        }

        // Finally if the iterator
        final var hasNext = iterator.hasNext();

        // does not have a next element
        if (!hasNext)
        {
            // clear the iterator so it will be recreated
            iterator = null;
        }

        return hasNext;
    }

    @Override
    public boolean evaluate()
    {
        // If we can evaluate this expression again,
        if (canEvaluateAgain())
        {
            // unwind the stack to where this iterator should start pushing elements
            stack().unwind(stackIndex);

            // push the next match onto the stack
            final var next = iterator.next();
            trace("Next CLOSURE element is $", next);
            if (stack().push(next) == PUSHED)
            {
                return true;
            }
        }

        // Unwind the stack to the original size
        trace("No more elements in CLOSURE");
        stack().unwind(stackIndex);
        return false;
    }

    @Override
    public String toString()
    {
        return expression + "+";
    }

    @Override
    public void visit(final Visitor visitor)
    {
        super.visit(visitor);
        expression.visit(visitor);
    }

    private void findMatches(final List<Route> routes, final Route at)
    {
        // If we haven't searched far enough,
        if (at.size() < program().maximumClosureLength().asInt())
        {
            // save the size of the stack
            final int size = stack().size();

            // then go through the edges that can extend the route we are at
            for (final Edge out : at.last().outEdgesWithoutReversed())
            {
                // unwind the stack and push the next edge
                if (stack().push(out) == PUSHED)
                {
                    // and if the expression evaluates to true
                    if (expression.evaluate())
                    {
                        // append the out edge to form a new route
                        final var newRoute = at.append(out);

                        // and the new route to the set of matches
                        routes.add(newRoute);

                        // then explore that route further
                        findMatches(routes, newRoute);
                    }
                }
                stack().unwind(size);
            }
        }
    }

    private Iterator<Route> iterator(final Edge start)
    {
        // Find all the matches from the start edge
        final var matches = new ArrayList<Route>();

        // if it matches the expression
        if (expression.evaluate())
        {
            // find all matching routes from the start edge
            findMatches(matches, Route.fromEdge(start));

            // then remove the start edge from each match (since it is on top of the stack)
            for (int i = 0; i < matches.size(); i++)
            {
                matches.set(i, matches.get(i).withoutFirst());
            }
        }

        // Return an iterator over the matches
        trace("Found CLOSURE matches: $", matches);
        return matches.iterator();
    }
}
