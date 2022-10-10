package com.telenav.mesakit.graph.query.program.expressions.terminal.value;

import com.telenav.mesakit.graph.query.program.Expression;

/**
 * An expression that evaluates to a {@link Value}
 *
 * @author jonathanl (shibo)
 */
public interface ValueExpression extends Expression
{
    /**
     * Returns the value from evaluating the expression
     */
    Value evaluate();
}
