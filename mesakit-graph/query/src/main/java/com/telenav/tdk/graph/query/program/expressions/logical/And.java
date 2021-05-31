package com.telenav.kivakit.graph.query.program.expressions.logical;

import com.telenav.kivakit.graph.query.program.*;

/**
 * Evaluates [query] <b>AND</b> [query]
 *
 * @author jonathanl (shibo)
 */
public class And extends Node implements BooleanExpression
{
    private final BooleanExpression left;

    private final BooleanExpression right;

    public And(final BooleanExpression left, final BooleanExpression right)
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
        do
        {
            final int size = stack().size();
            final var leftResult = left.evaluate();
            trace("AND left expression $ is $", left.code(), leftResult);
            if (leftResult)
            {
                final var rightResult = right.evaluate();
                trace("AND right expression $ is $", right.code(), rightResult);
                if (rightResult)
                {
                    return true;
                }
            }
            stack().unwind(size);
        }
        while (canEvaluateAgain());
        return false;
    }

    @Override
    public String toString()
    {
        return left + " AND " + right;
    }

    @Override
    public void visit(final Visitor visitor)
    {
        super.visit(visitor);
        left.visit(visitor);
        right.visit(visitor);
    }
}
