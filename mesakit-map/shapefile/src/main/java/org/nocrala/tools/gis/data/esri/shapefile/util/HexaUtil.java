package org.nocrala.tools.gis.data.esri.shapefile.util;

@SuppressWarnings("unused")
public class HexaUtil
{
    private static final String HEXA_DIGITS = "0123456789abcdef";

    public static String byteArrayToString(byte[] b)
    {
        StringBuilder sb = new StringBuilder();
        for (byte value : b)
        {
            int v = value >= 0 ? value : value + 256;
            int left = v / 16;
            int right = v % 16;
            sb.append(decimalToHexa(left));
            sb.append(decimalToHexa(right));
        }
        return sb.toString();
    }

    // Util

    public static byte[] stringToByteArray(String orig)
    {

        String txt = orig.toLowerCase();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < txt.length(); i++)
        {
            if (txt.charAt(i) != ' ')
            {
                sb.append(txt.charAt(i));
            }
        }

        String packed = sb.toString();
        if (packed.length() % 2 != 0)
        {
            throw new RuntimeException("Must have even number of hexadigits, "
                    + "but has " + packed.length() + ".");
        }

        byte[] result = new byte[packed.length() / 2];
        for (int i = 0; i < packed.length(); i = i + 2)
        {
            int left = hexaToDecimal(packed.charAt(i));
            int right = hexaToDecimal(packed.charAt(i + 1));
            int total = left * 16 + right;
            result[i / 2] = total < 128 ? (byte) total : (byte) (total - 256);
            //      System.out.println("[" + left + ":" + right + "] -> " + result[i / 2]);
        }

        return result;
    }

    private static char decimalToHexa(int d)
    {
        return HEXA_DIGITS.charAt(d);
    }

    private static int hexaToDecimal(char c)
    {
        for (int i = 0; i < HEXA_DIGITS.length(); i++)
        {
            if (c == HEXA_DIGITS.charAt(i))
            {
                return i;
            }
        }
        throw new RuntimeException("Invalid hexa digit '" + c + "'.");
    }
}
