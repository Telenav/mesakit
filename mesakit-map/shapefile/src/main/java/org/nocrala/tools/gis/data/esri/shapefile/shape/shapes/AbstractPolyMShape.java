package org.nocrala.tools.gis.data.esri.shapefile.shape.shapes;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.shape.Const;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;
import org.nocrala.tools.gis.data.esri.shapefile.util.ISUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * The MIT License (MIT)
 * Copyright (c) 2014 measures 
 */

@SuppressWarnings({ "unused", "DuplicatedCode" })
public abstract class AbstractPolyMShape extends AbstractPolyShape
{
    private static final int BASE_CONTENT_LENGTH = (4 + 8 * 4 + 4 + 4 + 8 * 2) / 2;

    private final double minM;

    private final double maxM;

    private final double[] m;

    protected AbstractPolyMShape(ShapeHeader shapeHeader,
                                 ShapeType shapeType, InputStream is,
                                 ValidationPreferences rules) throws IOException,
            InvalidShapeFileException
    {

        super(shapeHeader, shapeType, is, rules);

        if (!rules.isAllowBadContentLength())
        {
            int expectedLength = BASE_CONTENT_LENGTH //
                    + (this.numberOfParts * (4)) / 2 //
                    + (this.numberOfPoints * (8 * 2 + 8)) / 2;
            if (this.header.getContentLength() != expectedLength)
            {
                throw new InvalidShapeFileException("Invalid " + getShapeTypeName()
                        + " shape header's content length. " + "Expected " + expectedLength
                        + " 16-bit words (for " + this.numberOfParts + " parts and "
                        + this.numberOfPoints + " points)" + " but found "
                        + this.header.getContentLength() + ". " + Const.PREFERENCES);
            }
        }

        this.minM = ISUtil.readLeDouble(is);
        this.maxM = ISUtil.readLeDouble(is);

        this.m = new double[this.numberOfPoints];
        for (int i = 0; i < this.numberOfPoints; i++)
        {
            this.m[i] = ISUtil.readLeDouble(is);
        }
    }

    public double[] getM()
    {
        return m;
    }

    // Accessors

    public double[] getMOfPart(int i)
    {
        if (i < 0 || i >= this.numberOfParts)
        {
            throw new RuntimeException("Invalid part " + i + ". Available parts [0:"
                    + this.numberOfParts + "].");
        }
        int from = this.partFirstPoints[i];
        int to = i < this.numberOfParts - 1 ? this.partFirstPoints[i + 1]
                : this.points.length;

        if (from < 0 || from > this.points.length)
        {
            throw new RuntimeException("Malformed content. Part start (" + from
                    + ") is out of range. Valid range of points is [0:"
                    + this.points.length + "].");
        }

        if (to < 0 || to > this.points.length)
        {
            throw new RuntimeException("Malformed content. Part end (" + to
                    + ") is out of range. Valid range of points is [0:"
                    + this.points.length + "].");
        }

        return Arrays.copyOfRange(this.m, from, to);
    }

    public double getMaxM()
    {
        return maxM;
    }

    public double getMinM()
    {
        return minM;
    }
}
