package com.telenav.tdk.graph.query.program.expressions.terminal.value;

import com.telenav.tdk.core.kernel.language.reflection.Type;
import com.telenav.tdk.core.kernel.language.reflection.property.Property;
import com.telenav.tdk.graph.Edge;
import com.telenav.tdk.graph.query.program.*;

/**
 * Extracts a value from the edge on top of the stack using the given attribute name. The attribute name must be the
 * name of a method that takes no arguments and returns a value. The method is called to evaluate the expression.
 *
 * @author jonathanl (shibo)
 */
public class BooleanAttribute extends Node implements BooleanExpression
{
    public static BooleanAttribute parse(final String attributeName)
    {
        final var method = Type.forClass(Edge.class).method(attributeName);
        if (method == null || (method.type() != Boolean.class && method.type() != Boolean.TYPE))
        {
            return null;
        }
        return new BooleanAttribute(method);
    }

    private final Property method;

    public BooleanAttribute(final Property method)
    {
        this.method = method;
    }

    /**
     * @return The value extracted from the edge on top of the stack
     */
    @Override
    public boolean evaluate()
    {
        final var value = method.get(stack().top());
        final var result = value != null && (Boolean) value;
        trace("Attribute $ is $", code(), result);
        return result;
    }

    @Override
    public String toString()
    {
        return method.name();
    }
}
