package org.nocrala.tools.gis.data.esri.shapefile.shape.shapes;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.shape.Const;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;

import java.io.IOException;
import java.io.InputStream;

/**
 * The MIT License (MIT)
 * Copyright (c) 2014 measures 
 */

public class MultiPointPlainShape extends AbstractMultiPointShape
{
    private static final int BASE_CONTENT_LENGTH = (4 + 8 * 4 + 4) / 2;

    public MultiPointPlainShape(ShapeHeader shapeHeader,
                                ShapeType shapeType, InputStream is,
                                ValidationPreferences rules) throws IOException,
            InvalidShapeFileException
    {

        super(shapeHeader, shapeType, is, rules);

        if (!rules.isAllowBadContentLength())
        {
            int expectedLength = BASE_CONTENT_LENGTH
                    + (this.numberOfPoints * (8 * 2)) / 2;
            if (this.header.getContentLength() != expectedLength)
            {
                throw new InvalidShapeFileException("Invalid " + getShapeTypeName()
                        + " shape header's content length. " + "Expected " + expectedLength
                        + " 16-bit words (for " + this.numberOfPoints + " points)"
                        + " but found " + this.header.getContentLength() + ". "
                        + Const.PREFERENCES);
            }
        }
    }

    @Override
    protected String getShapeTypeName()
    {
        return "MultiPoint";
    }
}
