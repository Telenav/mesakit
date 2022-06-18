package org.nocrala.tools.gis.data.esri.shapefile;

import org.nocrala.tools.gis.data.esri.shapefile.shape.PartType;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;

/**
 * The MIT License (MIT)Copyright (c) 2014 measures */
@SuppressWarnings({ "unused", "BooleanMethodIsAlwaysInverted" })
public class ValidationPreferences
{
    private static final int DEFAULT_MAX_NUMBER_OF_POINTS_PER_SHAPE = 10000;

    private int expectedRecordNumber = 0;

    private ShapeType forceShapeType = null;

    private boolean allowBadRecordNumbers = false;

    private boolean allowBadContentLength = false;

    private boolean allowMultipleShapeTypes = false;

    private boolean allowUnlimitedNumberOfPointsPerShape = false;

    private int maxNumberOfPointsPerShape = DEFAULT_MAX_NUMBER_OF_POINTS_PER_SHAPE;

    private PartType forcePartType = null;

    // Logic

    public int getExpectedRecordNumber()
    {
        return expectedRecordNumber;
    }

    public PartType getForcePartType()
    {
        return forcePartType;
    }

    // Accessors

    public ShapeType getForceShapeType()
    {
        return forceShapeType;
    }

    public int getMaxNumberOfPointsPerShape()
    {
        return maxNumberOfPointsPerShape;
    }

    public boolean isAllowBadContentLength()
    {
        return allowBadContentLength;
    }

    public boolean isAllowBadRecordNumbers()
    {
        return allowBadRecordNumbers;
    }

    public boolean isAllowMultipleShapeTypes()
    {
        return allowMultipleShapeTypes;
    }

    public boolean isAllowUnlimitedNumberOfPointsPerShape()
    {
        return allowUnlimitedNumberOfPointsPerShape;
    }

    /**
     * Inhibits the validation of the content length of each shape. Defaults to false.
     */
    public void setAllowBadContentLength(boolean allowBadContentLength)
    {
        this.allowBadContentLength = allowBadContentLength;
    }

    /**
     * Inhibits the validation of the record numbers; a correct shape file must have sequential record numbers, starting
     * at 1. Defaults to false.
     */

    public void setAllowBadRecordNumbers(boolean allowBadRecordNumbers)
    {
        this.allowBadRecordNumbers = allowBadRecordNumbers;
    }

    /**
     * Allows shapes of multiple types in the file; in a correct shape file all shapes must be of a single type,
     * specified on the header of the file. Defaults to false.
     */
    public void setAllowMultipleShapeTypes(boolean allowMultipleShapeTypes)
    {
        this.allowMultipleShapeTypes = allowMultipleShapeTypes;
    }

    /**
     * Allows any (positive) number of points per shape. It's strongly advised to always limit the number of points per
     * shape; otherwise, a corrupt file with a gigantic (garbage) number of points may crash the reader with an
     * OutOfMemory error. Defaults to false, with a default limit of 10000.
     */

    public void setAllowUnlimitedNumberOfPointsPerShape(
            boolean allowUnlimitedNumberOfPointsPerShape)
    {
        this.allowUnlimitedNumberOfPointsPerShape = allowUnlimitedNumberOfPointsPerShape;
    }

    /**
     * Forces the part types to a specific type, disabling its validation. Set this value to null to enable the
     * validation. Defaults to null.
     */
    public void setForcePartType(PartType forcePartType)
    {
        this.forcePartType = forcePartType;
    }

    /**
     * Forces the shape type to a specific type, disabling its validation. Set this value to null to enable the
     * validation. Defaults to null.
     */
    public void setForceShapeType(ShapeType forceShapeType)
    {
        this.forceShapeType = forceShapeType;
    }

    /**
     * Specifies the maximum number of points a shape can have. If a shape is found with a larger number of points a
     * exception is thrown showing the number of points it has. This parameter can be adjusted for different files, or
     * turned off with the method setAllowUnlimitedNumberOfPointsPerShape(). Defaults to 10000.
     */
    public void setMaxNumberOfPointsPerShape(int maxItems)
    {
        this.maxNumberOfPointsPerShape = maxItems;
    }

    void advanceOneRecordNumber()
    {
        this.expectedRecordNumber++;
    }
}
