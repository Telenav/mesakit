package com.telenav.mesakit.graph.query.program.expressions.terminal.value;

import com.telenav.mesakit.graph.query.program.Node;

/**
 * @author jonathanl (shibo)
 */
public class Constant extends Node implements ValueExpression
{
    private final Value constant;

    public Constant(final Value constant)
    {
        this.constant = constant;
    }

    @Override
    public Value evaluate()
    {
        return constant;
    }

    @Override
    public String toString()
    {
        return constant.toString();
    }
}
