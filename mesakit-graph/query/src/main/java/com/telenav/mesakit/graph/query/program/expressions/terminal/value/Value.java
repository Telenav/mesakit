package com.telenav.mesakit.graph.query.program.expressions.terminal.value;

import com.telenav.kivakit.core.string.Strip;
import com.telenav.kivakit.interfaces.naming.Named;
import com.telenav.kivakit.interfaces.value.LongValued;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.analytics.classification.classifiers.turn.TurnType;
import com.telenav.mesakit.map.road.model.BridgeType;
import com.telenav.mesakit.map.road.model.RoadFunctionalClass;
import com.telenav.mesakit.map.road.model.RoadState;
import com.telenav.mesakit.map.road.model.RoadSubType;
import com.telenav.mesakit.map.road.model.RoadSurface;
import com.telenav.mesakit.map.road.model.RoadType;

import static com.telenav.kivakit.core.ensure.Ensure.fail;

/**
 * A scalar value used in edge attribute comparisons. One of:
 * <ul>
 *     <li><b>Boolean</b> - True of false value</li>
 *     <li><b>Number</b> - Double precision number representing double, float, int, short, char or byte value, including {@link LongValued} values and specific Graph API enum values</li>
 *     <li><b>String</b> - Unicode string</li>
 *     <li><b>Object</b> - Some other kind of object value</li>
 * </ul>
 * <p>
 * Constant values can also be converted to an object that is one of these three types with {@link #asValue()}.
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("unused") public class Value
{
    public static Value TRUE = of(true);

    public static Value FALSE = of(false);

    public static final Value NULL = of(null);

    public static Value of(Object value)
    {
        return new Value(value);
    }

    private final Object value;

    private Value(Object value)
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
        if (value instanceof LongValued)
        {
            return (double) ((LongValued) value).longValue();
        }
        if (value instanceof Number)
        {
            return ((Number) value).doubleValue();
        }
        if (isString())
        {
            var enumName = asString().toUpperCase();
            for (var type : new Class[] { RoadType.class, RoadFunctionalClass.class, RoadSurface.class,
                    RoadSubType.class, Edge.TransportMode.class, BridgeType.class, RoadState.class, TurnType.class })
            {
                try
                {
                    var enumValue = Enum.valueOf(type, enumName);
                    if (enumValue instanceof LongValued)
                    {
                        return (double) ((LongValued) enumValue).longValue();
                    }
                }
                catch (Exception ignored)
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
        return string == null ? null : Strip.stripQuotes(string);
    }

    public Object asValue()
    {
        var doubleValue = asNumber();
        if (doubleValue != null)
        {
            return doubleValue;
        }
        var stringValue = asString();
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
