package com.telenav.kivakit.graph.query.program.expressions.relational;

import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.graph.query.program.*;

import static com.telenav.kivakit.graph.query.program.EdgeStack.Result.PUSHED;

/**
 * Evaluates [query] <b>THEN</b> [query] against the edge on top of the stack. The expression is true if the left side
 * of the <b>THEN</b> expression is true and some edge reachable from the 'to' vertex of the matched edge matches the
 * right side of the <b>THEN</b> expression
 *
 * @author jonathanl (shibo)
 */
public class Then extends Node implements BooleanExpression
{
    private final BooleanExpression left;

    private final BooleanExpression right;

    public Then(final BooleanExpression left, final BooleanExpression right)
    {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean canEvaluateAgain()
    {
        return left.canEvaluateAgain() || right.canEvaluateAgain();
    }

    @Override
    public boolean evaluate()
    {
        final int size = stack().size();
        do
        {
            trace("Evaluating THEN left expression $", left.code());
            if (left.evaluate())
            {
                trace("THEN left expression $ is true", left.code());
                for (final Edge edge : stack().reachableEdges())
                {
                    final int sizeBeforeEdge = stack().size();
                    if (stack().push(edge) == PUSHED)
                    {
                        trace("Evaluating THEN right expression $ against ${long}", right.code(), edge.identifierAsLong());
                        final var result = right.evaluate();
                        trace("THEN right expression $ is $", right.code(), result);
                        if (result)
                        {
                            return true;
                        }
                    }
                    stack().unwind(sizeBeforeEdge);
                }
            }
            else
            {
                trace("THEN left expression $ is false", left.code());
            }
        }
        while (left.canEvaluateAgain());
        stack().unwind(size);
        return false;
    }

    @Override
    public String toString()
    {
        return left + " THEN " + right;
    }

    @Override
    public void visit(final Visitor visitor)
    {
        super.visit(visitor);
        left.visit(visitor);
        right.visit(visitor);
    }
}
