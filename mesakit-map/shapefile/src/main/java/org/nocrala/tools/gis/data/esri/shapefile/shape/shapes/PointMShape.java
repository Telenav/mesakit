package org.nocrala.tools.gis.data.esri.shapefile.shape.shapes;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.shape.Const;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;
import org.nocrala.tools.gis.data.esri.shapefile.util.ISUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * The MIT License (MIT)
 * Copyright (c) 2014 measures 
 */

public class PointMShape extends AbstractPointShape
{
    private static final int FIXED_CONTENT_LENGTH = (4 + 8 + 8 + 8) / 2;

    private final double m;

    public PointMShape(ShapeHeader shapeHeader, ShapeType shapeType,
                       InputStream is, ValidationPreferences rules)
            throws IOException, InvalidShapeFileException
    {
        super(shapeHeader, shapeType, is, rules);

        if (!rules.isAllowBadContentLength()
                && this.header.getContentLength() != FIXED_CONTENT_LENGTH)
        {
            throw new InvalidShapeFileException(
                    "Invalid PointM shape header's content length. " + "Expected "
                            + FIXED_CONTENT_LENGTH + " 16-bit words but found "
                            + this.header.getContentLength() + ". " + Const.PREFERENCES);
        }

        this.m = ISUtil.readLeDouble(is);
    }

    // Getters

    @SuppressWarnings("unused")
    public double getM()
    {
        return m;
    }
}
