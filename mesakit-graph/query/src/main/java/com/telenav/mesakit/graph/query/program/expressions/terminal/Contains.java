package com.telenav.mesakit.graph.query.program.expressions.terminal;

import com.telenav.kivakit.core.string.Strings;
import com.telenav.mesakit.graph.query.program.BooleanExpression;
import com.telenav.mesakit.graph.query.program.Node;
import com.telenav.mesakit.graph.query.program.Visitor;
import com.telenav.mesakit.graph.query.program.expressions.terminal.value.ValueExpression;

/**
 * @author jonathanl (shibo)
 */
public class Contains extends Node implements BooleanExpression
{
    private final ValueExpression left;

    private final ValueExpression right;

    public Contains(ValueExpression left, ValueExpression right)
    {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean evaluate()
    {
        var left = this.left.evaluate();
        var right = this.right.evaluate();
        if (left.isString() && right.isString())
        {
            var result = Strings.containsIgnoreCase(left.asString(), right.asString());
            trace("Evaluates to $", result);
            return result;
        }
        return fail("Left and right sides of 'CONTAINS' operator must both be strings");
    }

    @Override
    public String toString()
    {
        return left + " CONTAINS " + right;
    }

    @Override
    public void visit(Visitor visitor)
    {
        super.visit(visitor);
        left.visit(visitor);
        right.visit(visitor);
    }
}
