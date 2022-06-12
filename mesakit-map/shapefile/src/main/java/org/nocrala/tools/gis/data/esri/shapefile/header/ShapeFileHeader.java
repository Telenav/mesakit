package org.nocrala.tools.gis.data.esri.shapefile.header;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;
import org.nocrala.tools.gis.data.esri.shapefile.util.ISUtil;

/**
 * The MIT License (MIT)
 * Copyright (c) 2014 measures 
 */

@SuppressWarnings("unused")
public class ShapeFileHeader
{
	private static final int SHAPE_FILE_CODE = 9994;
	private static final int SHAPE_FILE_VERSION = 1000;

	private final int fileCode;

	private final int unused0;
	private final int unused1;
	private final int unused2;
	private final int unused3;
	private final int unused4;

	private final int fileLength;
	private final int version;
	private final ShapeType shapeType;

	private final double boxMinX;
	private final double boxMinY;
	private final double boxMaxX;
	private final double boxMaxY;

	private final double boxMinZ;
	private final double boxMaxZ;

	private final double boxMinM;
	private final double boxMaxM;

	public ShapeFileHeader(final InputStream is,
			final ValidationPreferences rules) throws IOException,
			InvalidShapeFileException {

		try {
			this.fileCode = ISUtil.readBeInt(is);
			if (this.fileCode != SHAPE_FILE_CODE) {
				throw new InvalidShapeFileException(
						"Invalid shape file code. Found " + this.fileCode
								+ " but expected " + SHAPE_FILE_CODE + ".");
			}

			this.unused0 = ISUtil.readBeInt(is);
			this.unused1 = ISUtil.readBeInt(is);
			this.unused2 = ISUtil.readBeInt(is);
			this.unused3 = ISUtil.readBeInt(is);
			this.unused4 = ISUtil.readBeInt(is);

			this.fileLength = ISUtil.readBeInt(is);
			this.version = ISUtil.readLeInt(is);
			if (this.version != SHAPE_FILE_VERSION) {
				throw new InvalidShapeFileException(
						"Invalid shape file version. Found " + this.version
								+ " but expected " + SHAPE_FILE_VERSION + ".");
			}

			int shapeTypeId = ISUtil.readLeInt(is);
			if (rules.getForceShapeType() == null) {
				this.shapeType = ShapeType.parse(shapeTypeId);
				if (this.shapeType == null) {
					throw new InvalidShapeFileException("Invalid shape file. "
							+ "The header's shape type has the invalid code "
							+ shapeTypeId + ".");
				}
			} else {
				this.shapeType = rules.getForceShapeType();
			}

			this.boxMinX = ISUtil.readLeDouble(is);
			this.boxMinY = ISUtil.readLeDouble(is);
			this.boxMaxX = ISUtil.readLeDouble(is);
			this.boxMaxY = ISUtil.readLeDouble(is);

			this.boxMinZ = ISUtil.readLeDouble(is);
			this.boxMaxZ = ISUtil.readLeDouble(is);

			this.boxMinM = ISUtil.readLeDouble(is);
			this.boxMaxM = ISUtil.readLeDouble(is);

		} catch (EOFException e) {
			throw new InvalidShapeFileException("Unexpected end of stream. "
					+ "The content is too short. "
					+ "It doesn't even have a complete header.");
		}
	}

	// Getters

	public int getFileCode() {
		return fileCode;
	}

	public int getUnused0() {
		return unused0;
	}

	public int getUnused1() {
		return unused1;
	}

	public int getUnused2() {
		return unused2;
	}

	public int getUnused3() {
		return unused3;
	}

	public int getUnused4() {
		return unused4;
	}

	public int getFileLength() {
		return fileLength;
	}

	public int getVersion() {
		return version;
	}

	public ShapeType getShapeType() {
		return shapeType;
	}

	public double getBoxMinX() {
		return boxMinX;
	}

	public double getBoxMinY() {
		return boxMinY;
	}

	public double getBoxMaxX() {
		return boxMaxX;
	}

	public double getBoxMaxY() {
		return boxMaxY;
	}

	public double getBoxMinZ() {
		return boxMinZ;
	}

	public double getBoxMaxZ() {
		return boxMaxZ;
	}

	public double getBoxMinM() {
		return boxMinM;
	}

	public double getBoxMaxM() {
		return boxMaxM;
	}

}
