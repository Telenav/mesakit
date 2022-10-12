package org.nocrala.tools.gis.data.esri.shapefile.shape.shapes;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;
import org.nocrala.tools.gis.data.esri.shapefile.util.ISUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * The MIT License (MIT)
 * Copyright (c) 2014 measures 
 */

public abstract class AbstractPointShape extends AbstractShape
{
    private final double x;

    private final double y;

    protected AbstractPointShape(ShapeHeader shapeHeader,
                                 ShapeType shapeType, InputStream is,
                                 ValidationPreferences rules) throws IOException
    {
        super(shapeHeader, shapeType, is, rules);

        this.x = ISUtil.readLeDouble(is);
        this.y = ISUtil.readLeDouble(is);
    }

    // Getters

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }
}
