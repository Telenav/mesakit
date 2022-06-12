package org.nocrala.tools.gis.data.esri.shapefile.shape;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;

import java.io.InputStream;

/**
 * The MIT License (MIT)
 * Copyright (c) 2014 measures 
 */

public abstract class AbstractShape
{
    protected ShapeHeader header;

    protected ShapeType shapeType;

    @SuppressWarnings("unused")
    public AbstractShape(final ShapeHeader shapeHeader,
                         final ShapeType shapeType, final InputStream is,
                         final ValidationPreferences rules)
    {
        this.header = shapeHeader;
        this.shapeType = shapeType;
    }

    // Getters

    public final ShapeHeader getHeader()
    {
        return header;
    }

    public ShapeType getShapeType()
    {
        return shapeType;
    }
}
