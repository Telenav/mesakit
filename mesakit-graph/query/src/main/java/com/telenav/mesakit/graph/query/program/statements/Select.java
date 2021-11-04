package com.telenav.mesakit.graph.query.program.statements;

import com.telenav.mesakit.graph.query.program.BooleanExpression;
import com.telenav.mesakit.graph.query.program.Node;
import com.telenav.mesakit.graph.query.program.Statement;
import com.telenav.mesakit.graph.query.program.Visitor;

/**
 * A select statement evaluating to a boolean value
 *
 * @author jonathanl (shibo)
 */
public class Select extends Node implements Statement
{
    /** The query expression to evaluate */
    private final BooleanExpression query;

    public Select(BooleanExpression query)
    {
        this.query = query;
    }

    @Override
    public boolean evaluate()
    {
        if (query.evaluate())
        {
            stack().matched();
            return true;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "select " + query;
    }

    @Override
    public void visit(Visitor visitor)
    {
        super.visit(visitor);
        query.visit(visitor);
    }
}
