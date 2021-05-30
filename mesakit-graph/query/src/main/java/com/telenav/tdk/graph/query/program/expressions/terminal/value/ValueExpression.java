package com.telenav.tdk.graph.query.program.expressions.terminal.value;

import com.telenav.tdk.graph.query.program.Expression;

/**
 * An expression that evaluates to a {@link Value}
 *
 * @author jonathanl (shibo)
 */
public interface ValueExpression extends Expression
{
    /**
     * @return The value from evaluating the expression
     */
    Value evaluate();
}
