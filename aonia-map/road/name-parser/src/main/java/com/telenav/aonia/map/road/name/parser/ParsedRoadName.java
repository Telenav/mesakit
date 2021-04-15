////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.parser;

import com.telenav.aonia.map.measurements.geographic.Direction;
import com.telenav.aonia.map.road.model.RoadName;
import com.telenav.kivakit.core.kernel.language.objects.Hash;
import com.telenav.kivakit.core.kernel.language.objects.Objects;

public class ParsedRoadName
{
    /**
     * The format of the road name's direction
     *
     * @author jonathanl (shibo)
     */
    public enum DirectionFormat
    {
        /** The direction is prefixed, as in North 125th Street */
        PREFIXED,

        /** The direction is suffixed, as in 125th Street NW */
        SUFFIXED,

        /** There is no direction, as in Main Street */
        NONE
    }

    public enum TypePosition
    {
        FIRST,
        LAST
    }

    public static class Builder
    {
        private final ParsedRoadName parsed;

        public Builder()
        {
            parsed = new ParsedRoadName();
        }

        public Builder(final ParsedRoadName name)
        {
            parsed = new ParsedRoadName(name);
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder baseName(final String baseName, final String rawName)
        {
            parsed.baseName = baseName;
            parsed.rawBaseName = rawName;
            return this;
        }

        public ParsedRoadName build()
        {
            if (parsed.isValid())
            {
                return parsed;
            }
            return null;
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder direction(final String direction, final String rawDirection)
        {
            parsed.direction = direction;
            parsed.rawDirection = rawDirection;
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder directionFormat(final DirectionFormat directionFormat)
        {
            parsed.directionFormat = directionFormat;
            return this;
        }

        public Builder exception(final Exception exception)
        {
            parsed.exception = exception;
            return this;
        }

        public void position(final TypePosition position)
        {
            parsed.position = position;
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder type(final String type, final String rawType)
        {
            parsed.type = type;
            parsed.rawType = rawType;
            return this;
        }
    }

    public String rawBaseName;

    public String rawType;

    public String rawDirection;

    private String baseName;

    private String type;

    private String direction;

    private DirectionFormat directionFormat;

    private Exception exception;

    private TypePosition position = TypePosition.LAST;

    private ParsedRoadName()
    {
    }

    private ParsedRoadName(final ParsedRoadName that)
    {
        assert that != null;
        baseName = that.baseName;
        type = that.type;
        direction = that.direction;
        directionFormat = that.directionFormat;
        exception = that.exception;
        rawBaseName = that.rawBaseName;
        rawType = that.rawType;
        rawDirection = that.rawDirection;
        position = that.position;
    }

    /**
     * @return The road name reconstructed from the raw (non-standardized) parsed data
     */
    public RoadName asRawRoadName()
    {
        final var builder = new StringBuilder();
        if (rawType != null && position == TypePosition.FIRST)
        {
            builder.append(rawType);
            builder.append(' ');
        }
        if (directionFormat == DirectionFormat.PREFIXED)
        {
            builder.append(rawDirection);
            builder.append(' ');
        }
        builder.append(rawBaseName);
        if (rawType != null && position == TypePosition.LAST)
        {
            builder.append(' ');
            builder.append(rawType);
        }
        if (directionFormat == DirectionFormat.SUFFIXED)
        {
            builder.append(' ');
            builder.append(rawDirection);
        }
        return RoadName.forName(builder.toString());
    }

    /**
     * @return The parsed and (partially) standardized road name
     */
    public RoadName asRoadName()
    {
        final var builder = new StringBuilder();
        if (type != null && position == TypePosition.FIRST)
        {
            builder.append(type);
            builder.append(' ');
        }
        if (directionFormat == DirectionFormat.PREFIXED)
        {
            builder.append(direction);
            builder.append(' ');
        }
        builder.append(baseName);
        if (type != null && position == TypePosition.LAST)
        {
            builder.append(' ');
            builder.append(type);
        }
        if (directionFormat == DirectionFormat.SUFFIXED)
        {
            builder.append(' ');
            builder.append(direction);
        }
        return RoadName.forName(builder.toString());
    }

    public String baseName()
    {
        return baseName;
    }

    public Direction direction()
    {
        if (direction != null)
        {
            return Direction.parse(direction);
        }
        return null;
    }

    public DirectionFormat directionFormat()
    {
        return directionFormat;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof ParsedRoadName)
        {
            final var that = (ParsedRoadName) object;
            return Objects.equalPairs(baseName, that.baseName, type, that.type, direction,
                    that.direction, directionFormat, that.directionFormat);
        }
        return false;
    }

    public Exception exception()
    {
        return exception;
    }

    public boolean failed()
    {
        return exception != null;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(baseName, type, direction, directionFormat);
    }

    public boolean isValid()
    {
        return baseName != null && (directionFormat == DirectionFormat.NONE || direction != null);
    }

    public String rawBaseName()
    {
        return rawBaseName;
    }

    public String rawDirection()
    {
        return rawDirection;
    }

    public String rawType()
    {
        return rawType;
    }

    @Override
    public String toString()
    {
        return asRoadName().name();
    }

    public String type()
    {
        return type;
    }
}
