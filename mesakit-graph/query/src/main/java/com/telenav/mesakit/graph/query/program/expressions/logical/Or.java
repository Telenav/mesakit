package com.telenav.mesakit.graph.query.program.expressions.logical;

import com.telenav.mesakit.graph.query.program.BooleanExpression;
import com.telenav.mesakit.graph.query.program.Node;
import com.telenav.mesakit.graph.query.program.Visitor;

/**
 * Evaluates [query] <b>OR</b> [query]
 *
 * @author jonathanl(shibo)
 */
public class Or extends Node implements BooleanExpression
{
    private final BooleanExpression left;

    private final BooleanExpression right;

    public Or(final BooleanExpression left, final BooleanExpression right)
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
            trace("OR left expression $ is $", left.code(), leftResult);
            if (leftResult)
            {
                return true;
            }
            final var rightResult = right.evaluate();
            trace("OR right expression $ is $", right.code(), rightResult);
            if (rightResult)
            {
                return true;
            }
            stack().unwind(size);
        }
        while (canEvaluateAgain());
        return false;
    }

    @Override
    public String toString()
    {
        return left + " OR " + right;
    }

    @Override
    public void visit(final Visitor visitor)
    {
        super.visit(visitor);
        left.visit(visitor);
        right.visit(visitor);
    }
}
