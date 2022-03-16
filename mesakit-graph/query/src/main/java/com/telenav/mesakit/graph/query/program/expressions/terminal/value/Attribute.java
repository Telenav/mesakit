package com.telenav.mesakit.graph.query.program.expressions.terminal.value;

import com.telenav.kivakit.core.language.reflection.Type;
import com.telenav.kivakit.core.language.reflection.property.Property;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.query.program.Node;

/**
 * Extracts a value from the edge on top of the stack using the given attribute name. The attribute name must be the
 * name of a method that takes no arguments and returns a value. The method is called to evaluate the expression.
 *
 * @author jonathanl (shibo)
 */
public class Attribute extends Node implements ValueExpression
{
    private final Property method;

    public Attribute(String attributeName)
    {
        method = Type.forClass(Edge.class).property(attributeName);
        if (method == null)
        {
            fail("Unable to find edge attribute '$'", attributeName);
        }
    }

    /**
     * @return The value extracted from the edge on top of the stack
     */
    @Override
    public Value evaluate()
    {
        var result = method.get(stack().top());
        trace("Attribute $ is $", code(), result);
        return result == null ? Value.NULL : Value.of(result);
    }

    @Override
    public String toString()
    {
        return method.name();
    }
}
