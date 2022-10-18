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

package com.telenav.mesakit.map.road.model;

import com.telenav.kivakit.interfaces.value.LongValued;

import java.util.ArrayList;
import java.util.List;

/**
 * Indicates the importance of a road, based on its functional classification.
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("unused")
public enum RoadFunctionalClass implements LongValued
{
    MAIN(5),
    FIRST_CLASS(4),
    SECOND_CLASS(3),
    THIRD_CLASS(2),
    FOURTH_CLASS(1),
    UNKNOWN(0);

    public static final RoadFunctionalClass MINIMUM = FOURTH_CLASS;

    public static RoadFunctionalClass MAXIMUM = MAIN;

    /**
     * Returns the road type for the given identifier
     */
    public static RoadFunctionalClass forIdentifier(int identifier)
    {
        switch (identifier)
        {
            case 5:
                return MAIN;

            case 4:
                return FIRST_CLASS;

            case 3:
                return SECOND_CLASS;

            case 2:
                return THIRD_CLASS;

            case 1:
                return FOURTH_CLASS;

            default:
                return UNKNOWN;
        }
    }

    /**
     * Returns the road type for the given identifier
     */
    public static RoadFunctionalClass forInvertedIdentifier(int identifier)
    {
        switch (identifier)
        {
            case 1:
                return MAIN;

            case 2:
                return FIRST_CLASS;

            case 3:
                return SECOND_CLASS;

            case 4:
                return THIRD_CLASS;

            case 5:
                return FOURTH_CLASS;

            default:
                return UNKNOWN;
        }
    }

    /**
     * Returns the identifier for the given road type or NULL.identifier if the type is null
     */
    public static int identifierFor(RoadFunctionalClass type)
    {
        if (type == null)
        {
            return UNKNOWN.identifier;
        }
        return type.identifier();
    }

    private final int identifier;

    RoadFunctionalClass(int identifier)
    {
        this.identifier = identifier;
    }

    /**
     * Returns a list of road functional classes at or below this one in order of importance
     */
    public List<RoadFunctionalClass> atOrBelow()
    {
        List<RoadFunctionalClass> classes = new ArrayList<>();

        for (var i = identifier(); i > 0; i--)
        {
            classes.add(forIdentifier(i));
        }
        return classes;
    }

    public int identifier()
    {
        return identifier;
    }

    public boolean isLessImportantThan(RoadFunctionalClass that)
    {
        return identifier < that.identifier;
    }

    public boolean isLessImportantThanOrEqual(RoadFunctionalClass that)
    {
        return identifier <= that.identifier;
    }

    public boolean isMoreImportantThan(RoadFunctionalClass that)
    {
        return identifier > that.identifier;
    }

    public boolean isMoreImportantThanOrEqual(RoadFunctionalClass that)
    {
        return identifier >= that.identifier;
    }

    @Override
    public long longValue()
    {
        return identifier;
    }

    public RoadFunctionalClass maximum(RoadFunctionalClass that)
    {
        return isMoreImportantThan(that) ? this : that;
    }

    public RoadFunctionalClass nextLevel()
    {
        switch (this)
        {
            case FOURTH_CLASS:
                return THIRD_CLASS;

            case THIRD_CLASS:
                return SECOND_CLASS;

            case SECOND_CLASS:
                return FIRST_CLASS;

            case FIRST_CLASS:
                return MAIN;

            case MAIN:
            default:
                return null;
        }
    }
}
