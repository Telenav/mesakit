package org.nocrala.tools.gis.data.esri.shapefile.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings({ "unused", "DuplicatedCode" })
public class IntSerializer
{
    private static final int BYTE_ARRAY_SIZE = 4;

    public static int deserializeBigEndian(ByteBuffer b)
    {
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
        // BAUtil.displayByteArray("deserializeBigEndian():", b.array());
        b.order(ByteOrder.BIG_ENDIAN);
        b.position(0);
        return b.getInt();
    }

    public static int deserializeLittleEndian(ByteBuffer b)
    {
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
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.position(0);
        return b.getInt();
    }

    public static void serializeBigEndian(int value, ByteBuffer b)
    {
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
        b.putInt(value);
    }

    public static void serializeLittleEndian(int value, ByteBuffer b)
    {
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
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.position(0);
        b.putInt(value);
    }
}
