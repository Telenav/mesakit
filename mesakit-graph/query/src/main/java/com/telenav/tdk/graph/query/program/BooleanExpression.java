package com.telenav.kivakit.graph.query.program;

import com.telenav.kivakit.graph.query.program.expressions.closure.OneOrMore;

/**
 * A boolean expression that can be evaluated with {@link #evaluate()} and may be evaluated more times so long as {@link
 * #canEvaluateAgain()} returns true.
 *
 * @author jonathanl (shibo)
 */
public interface BooleanExpression extends Expression
{
    /**
     * @return True if the expression can be evaluated again. This method is implemented by the {@link OneOrMore} class
     * to potentially evaluate the same expression multiple times as the closure includes more and more edges.
     */
    default boolean canEvaluateAgain()
    {
        return false;
    }

    /**
     * @return True if the expression evaluates to true
     */
    boolean evaluate();
}
