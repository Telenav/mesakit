package org.nocrala.tools.gis.data.esri.shapefile.shape;

/**
 * The MIT License (MIT)
 * Copyright (c) 2014 measures 
 */

public enum PartType
{
    TRIANGLE_STRIP(0), //
    TRIANGLE_FAN(1), //
    OUTER_RING(2), //
    INNER_RING(3), //
    FIRST_RING(4), //
    RING(5); //

    public static PartType parse(final int tid)
    {
        for (PartType st : PartType.values())
        {
            if (st.getId() == tid)
            {
                return st;
            }
        }
        return null;
    }

    private final int id;

    // parse

    PartType(int id)
    {
        this.id = id;
    }

    // Getters

    /**
     * Returns the part type's numeric ID, as defined by the ESRI specification.
     */
    public int getId()
    {
        return this.id;
    }
}
