package com.telenav.mesakit.graph.query.program.expressions.logical;

import com.telenav.mesakit.graph.query.program.BooleanExpression;
import com.telenav.mesakit.graph.query.program.Node;
import com.telenav.mesakit.graph.query.program.Visitor;

/**
 * Evaluates [query] <b>AND</b> [query]
 *
 * @author jonathanl (shibo)
 */
public class Not extends Node implements BooleanExpression
{
    private final BooleanExpression expression;

    public Not(final BooleanExpression expression)
    {
        this.expression = expression;
    }

    @Override
    public boolean canEvaluateAgain()
    {
        return expression.canEvaluateAgain();
    }

    @Override
    public boolean evaluate()
    {
        do
        {
            final int size = stack().size();
            final var result = !expression.evaluate();
            trace("NOT expression $ is $", expression.code(), result);
            if (result)
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
        return "NOT " + expression;
    }

    @Override
    public void visit(final Visitor visitor)
    {
        super.visit(visitor);
        expression.visit(visitor);
    }
}
