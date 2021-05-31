package com.telenav.kivakit.graph.query.program.expressions.terminal.value;

import com.telenav.kivakit.kernel.language.reflection.Type;
import com.telenav.kivakit.kernel.language.reflection.property.Property;
import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.graph.query.program.Node;

/**
 * Extracts a value from the edge on top of the stack using the given attribute name. The attribute name must be the
 * name of a method that takes no arguments and returns a value. The method is called to evaluate the expression.
 *
 * @author jonathanl (shibo)
 */
public class Attribute extends Node implements ValueExpression
{
    private final Property method;

    public Attribute(final String attributeName)
    {
        method = Type.forClass(Edge.class).method(attributeName);
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
        final var result = method.get(stack().top());
        trace("Attribute $ is $", code(), result);
        return result == null ? Value.NULL : Value.of(result);
    }

    @Override
    public String toString()
    {
        return method.name();
    }
}
