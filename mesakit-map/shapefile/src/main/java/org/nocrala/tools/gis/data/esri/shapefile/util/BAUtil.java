package org.nocrala.tools.gis.data.esri.shapefile.util;

@SuppressWarnings("unused")
public class BAUtil
{
    public static void displayByteArray(String prompt, byte[] b)
    {
        if (b == null)
        {
            System.out.print(prompt + " byte array[]: null");
        }
        else
        {
            System.out.print(prompt + " byte array[" + b.length + "]: ");
            boolean isFirst = true;
            for (byte value : b)
            {
                if (isFirst)
                {
                    isFirst = false;
                }
                else
                {
                    System.out.print(", ");
                }
                System.out.print(value);
            }
        }
        System.out.println();
    }
}
