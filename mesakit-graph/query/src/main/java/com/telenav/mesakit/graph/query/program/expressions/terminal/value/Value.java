package com.telenav.mesakit.graph.query.program.expressions.terminal.value;

import com.telenav.kivakit.kernel.interfaces.naming.Named;
import com.telenav.kivakit.kernel.interfaces.numeric.Quantizable;
import com.telenav.kivakit.kernel.language.string.Strings;
import com.telenav.mesakit.map.road.model.*;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.analytics.classification.classifiers.turn.TurnType;

import static com.telenav.kivakit.kernel.validation.Validate.fail;

/**
 * A scalar value used in edge attribute comparisons. One of:
 * <ul>
 *     <li><b>Boolean</b> - True of false value</li>
 *     <li><b>Number</b> - Double precision number representing double, float, int, short, char or byte value, including {@link Quantizable} values and specific Graph API enum values</li>
 *     <li><b>String</b> - Unicode string</li>
 *     <li><b>Object</b> - Some other kind of object value</li>
 * </ul>
 * <p>
 * Constant values can also be converted to an object that is one of these three types with {@link #asValue()}.
 *
 * @author jonathanl (shibo)
 */
public class Value
{
    public static Value TRUE = of(true);

    public static Value FALSE = of(false);

    public static Value NULL = of(null);

    public static Value of(final Object value)
    {
        return new Value(value);
    }

    private final Object value;

    private Value(final Object value)
    {
        this.value = value;
    }

    public Boolean asBoolean()
    {
        if (isBoolean())
        {
            return (Boolean) value;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Double asNumber()
    {
        if (value instanceof Quantizable)
        {
            return (double) ((Quantizable) value).quantum();
        }
        if (value instanceof Number)
        {
            return ((Number) value).doubleValue();
        }
        if (isString())
        {
            final var enumName = asString().toUpperCase();
            for (final var type : new Class[] { RoadType.class, RoadFunctionalClass.class, RoadSurface.class,
                    RoadSubType.class, Edge.TransportMode.class, BridgeType.class, RoadState.class, TurnType.class })
            {
                try
                {
                    final var enumValue = Enum.valueOf(type, enumName);
                    if (enumValue instanceof Quantizable)
                    {
                        return (double) ((Quantizable) enumValue).quantum();
                    }
                }
                catch (final Exception ignored)
                {
                }
            }
        }
        return null;
    }

    public String asString()
    {
        String string = null;
        if (value instanceof String)
        {
            string = value.toString();
        }
        if (value instanceof Named)
        {
            string = ((Named) value).name();
        }
        return string == null ? null : Strings.stripQuotes(string);
    }

    public Object asValue()
    {
        final var doubleValue = asNumber();
        if (doubleValue != null)
        {
            return doubleValue;
        }
        final var stringValue = asString();
        if (stringValue != null)
        {
            return stringValue;
        }
        if (isBoolean())
        {
            return asBoolean();
        }
        return value;
    }

    public boolean isBoolean()
    {
        return value instanceof Boolean;
    }

    public boolean isFalse()
    {
        return !isTrue();
    }

    public boolean isNull()
    {
        return value == null;
    }

    public boolean isNumber()
    {
        return asNumber() != null;
    }

    public boolean isString()
    {
        return value instanceof String || value instanceof Named;
    }

    public boolean isTrue()
    {
        if (isBoolean())
        {
            return asBoolean();
        }
        fail("Expected boolean value");
        return false;
    }

    @Override
    public String toString()
    {
        return value.toString();
    }
}
