package org.nocrala.tools.gis.data.esri.shapefile.shape;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.DataStreamEOFException;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.util.ISUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * The MIT License (MIT)
 * Copyright (c) 2014 measures 
 */

@SuppressWarnings("unused")
public class ShapeHeader
{
    private final int recordNumber;

    private final int contentLength;

    public ShapeHeader(InputStream is, ValidationPreferences rules)
            throws DataStreamEOFException, IOException, InvalidShapeFileException
    {

        this.recordNumber = ISUtil.readBeIntMaybeEOF(is);
        if (!rules.isAllowBadRecordNumbers())
        {
            if (this.recordNumber != rules.getExpectedRecordNumber())
            {
                throw new InvalidShapeFileException("Invalid record number. Expected "
                        + rules.getExpectedRecordNumber() + " but found "
                        + this.recordNumber + ".");
            }
        }

        this.contentLength = ISUtil.readBeInt(is);
    }

    public int getContentLength()
    {
        return contentLength;
    }

    public int getRecordNumber()
    {
        return recordNumber;
    }
}
