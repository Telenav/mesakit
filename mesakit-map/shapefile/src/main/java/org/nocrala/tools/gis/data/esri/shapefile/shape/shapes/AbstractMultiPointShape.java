package org.nocrala.tools.gis.data.esri.shapefile.shape.shapes;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.Const;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;
import org.nocrala.tools.gis.data.esri.shapefile.util.ISUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * The MIT License (MIT)
 * Copyright (c) 2014 measures
 */
@SuppressWarnings("unused")
public abstract class AbstractMultiPointShape extends AbstractShape
{
    /** No comment */
    protected double boxMinX;

    /** No comment */
    protected double boxMinY;

    /** No comment */
    protected double boxMaxX;

    /** No comment */
    protected double boxMaxY;

    /** No comment */
    protected int numberOfPoints;

    /** No comment */
    protected PointData[] points;

    public AbstractMultiPointShape(final ShapeHeader shapeHeader,
                                   final ShapeType shapeType, final InputStream is,
                                   final ValidationPreferences rules) throws IOException,
            InvalidShapeFileException
    {
        super(shapeHeader, shapeType, is, rules);

        this.boxMinX = ISUtil.readLeDouble(is);
        this.boxMinY = ISUtil.readLeDouble(is);
        this.boxMaxX = ISUtil.readLeDouble(is);
        this.boxMaxY = ISUtil.readLeDouble(is);

        this.numberOfPoints = ISUtil.readLeInt(is);

        if (!rules.isAllowUnlimitedNumberOfPointsPerShape())
        {
            if (this.numberOfPoints > rules.getMaxNumberOfPointsPerShape())
            {
                throw new InvalidShapeFileException("Invalid " + getShapeTypeName()
                        + " shape number of points. "
                        + "The allowed maximum number of points was "
                        + rules.getMaxNumberOfPointsPerShape() + " but found "
                        + this.numberOfPoints + ". " + Const.PREFERENCES);
            }
        }

        this.points = new PointData[this.numberOfPoints];
        for (int i = 0; i < this.numberOfPoints; i++)
        {
            double x = ISUtil.readLeDouble(is);
            double y = ISUtil.readLeDouble(is);
            this.points[i] = new PointData(x, y);
        }
    }

    public double getBoxMaxX()
    {
        return boxMaxX;
    }

    // Getters

    public double getBoxMaxY()
    {
        return boxMaxY;
    }

    public double getBoxMinX()
    {
        return boxMinX;
    }

    public double getBoxMinY()
    {
        return boxMinY;
    }

    public int getNumberOfPoints()
    {
        return numberOfPoints;
    }

    public PointData[] getPoints()
    {
        return points;
    }

    protected abstract String getShapeTypeName();
}
