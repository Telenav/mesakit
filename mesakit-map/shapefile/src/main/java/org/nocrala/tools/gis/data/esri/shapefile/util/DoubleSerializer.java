package org.nocrala.tools.gis.data.esri.shapefile.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused") public class DoubleSerializer
{
    private static final int BYTE_ARRAY_SIZE = 8;

    public static double deserializeBigEndian(ByteBuffer b)
    {
        //noinspection DuplicatedCode
        if (b == null)
        {
            throw new RuntimeException("Cannot deserialize null byte buffer.");
        }
        if (b.array().length < BYTE_ARRAY_SIZE)
        {
            throw new RuntimeException(
                    "Cannot deserialize. Byte buffer must have at least "
                            + BYTE_ARRAY_SIZE + " bytes.");
        }
        b.order(ByteOrder.BIG_ENDIAN);
        b.position(0);
        return b.getDouble();
    }

    public static double deserializeLittleEndian(ByteBuffer b)
    {
        if (b == null)
        {
            throw new RuntimeException("Cannot deserialize null buffer.");
        }
        if (b.array().length < BYTE_ARRAY_SIZE)
        {
            throw new RuntimeException(
                    "Cannot deserialize. Byte buffer must have at least "
                            + BYTE_ARRAY_SIZE + " bytes.");
        }
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.position(0);
        return b.getDouble();
    }

    public static void serializeBigEndian(double value, ByteBuffer b)
    {
      //noinspection DuplicatedCode
      if (b == null)
        {
            throw new RuntimeException("Cannot serialize into null byte buffer.");
        }
        if (b.array().length < BYTE_ARRAY_SIZE)
        {
            throw new RuntimeException(
                    "Cannot serialize. Byte buffer must have at least " + BYTE_ARRAY_SIZE
                            + " bytes.");
        }
        b.order(ByteOrder.BIG_ENDIAN);
        b.position(0);
        b.putDouble(value);
    }

    public static void serializeLittleEndian(double value,
                                             ByteBuffer b)
    {
        if (b == null)
        {
            throw new RuntimeException("Cannot serialize into a null byte buffer.");
        }
        if (b.array().length < BYTE_ARRAY_SIZE)
        {
            throw new RuntimeException(
                    "Cannot serialize. Byte buffer must have at least " + BYTE_ARRAY_SIZE
                            + " bytes.");
        }
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.position(0);
        b.putDouble(value);
    }
}
