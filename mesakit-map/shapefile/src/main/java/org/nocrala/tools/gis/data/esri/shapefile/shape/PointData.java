package org.nocrala.tools.gis.data.esri.shapefile.shape;

/**
 * The MIT License (MIT)
 * Copyright (c) 2014 measures 
 */

public class PointData
{
    private final double x;

    private final double y;

    public PointData(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }
}
