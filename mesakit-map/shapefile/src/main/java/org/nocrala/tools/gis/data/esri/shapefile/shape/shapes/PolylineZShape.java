package org.nocrala.tools.gis.data.esri.shapefile.shape.shapes;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;

import java.io.IOException;
import java.io.InputStream;

/**
 * The MIT License (MIT)
 * Copyright (c) 2014 measures 
 */

public class PolylineZShape extends AbstractPolyZShape
{
    public PolylineZShape(ShapeHeader shapeHeader,
                          ShapeType shapeType, InputStream is,
                          ValidationPreferences rules) throws IOException,
            InvalidShapeFileException
    {
        super(shapeHeader, shapeType, is, rules);
    }

    @Override
    protected String getShapeTypeName()
    {
        return "PolylineZ";
    }
}
