package org.nocrala.tools.gis.data.esri.shapefile.shape;

/**
 * The MIT License (MIT)
 * Copyright (c) 2014 measures 
 */

public enum ShapeType
{
    NULL(0), //

    POINT(1), //
    POLYLINE(3), //
    POLYGON(5), //
    MULTIPOINT(8), //

    POINT_Z(11), //
    POLYLINE_Z(13), //
    POLYGON_Z(15), //
    MULTIPOINT_Z(18), //

    POINT_M(21), //
    POLYLINE_M(23), //
    POLYGON_M(25), //
    MULTIPOINT_M(28), //

    MULTIPATCH(31); //

    public static ShapeType parse(int tid)
    {
        for (ShapeType st : values())
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

    ShapeType(int id)
    {
        this.id = id;
    }

    // Getters

    /**
     * Returns the shape type's numeric ID, as defined by the ESRI specification.
     */
    public int getId()
    {
        return this.id;
    }
}
