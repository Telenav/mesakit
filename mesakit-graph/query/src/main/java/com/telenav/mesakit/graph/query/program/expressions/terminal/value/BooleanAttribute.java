package com.telenav.mesakit.graph.query.program.expressions.terminal.value;

import com.telenav.kivakit.core.language.reflection.Type;
import com.telenav.kivakit.core.language.reflection.property.Property;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.query.program.BooleanExpression;
import com.telenav.mesakit.graph.query.program.Node;

/**
 * Extracts a value from the edge on top of the stack using the given attribute name. The attribute name must be the
 * name of a method that takes no arguments and returns a value. The method is called to evaluate the expression.
 *
 * @author jonathanl (shibo)
 */
public class BooleanAttribute extends Node implements BooleanExpression
{
    public static BooleanAttribute parse(String attributeName)
    {
        var method = Type.forClass(Edge.class).property(attributeName);
        if (method == null || (method.type().type() != Boolean.class && method.type().type() != Boolean.TYPE))
        {
            return null;
        }
        return new BooleanAttribute(method);
    }

    private final Property method;

    public BooleanAttribute(Property method)
    {
        this.method = method;
    }

    /**
     * @return The value extracted from the edge on top of the stack
     */
    @Override
    public boolean evaluate()
    {
        var value = method.get(stack().top());
        var result = value != null && (Boolean) value;
        trace("Attribute $ is $", code(), result);
        return result;
    }

    @Override
    public String toString()
    {
        return method.name();
    }
}
