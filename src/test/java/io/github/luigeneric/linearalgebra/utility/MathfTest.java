package io.github.luigeneric.linearalgebra.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MathfTest
{
    @Test
    void testpiDiv2()
    {
        String raw = "22DB0FC93F";
        float pitch = ldcR4FromIlHex(raw);
        float old = Mathf.piDiv2;

        //System.out.println(pitch);
        //System.out.println(old);

        //System.out.println(Float.toHexString(pitch));
        //System.out.println(Float.toHexString(old));


        assertEquals(Float.floatToRawIntBits(pitch), Float.floatToRawIntBits(old));
    }

    static float ldcR4FromIlHex(String ilHex)
    {
        String h = ilHex.replace("0x", "")
                .replace("0X", "")
                .replaceAll("[^0-9A-Fa-f]", "");


        if (h.length() == 10 && h.substring(0, 2).equalsIgnoreCase("22")) {
            h = h.substring(2);
        }

        if (h.length() != 8) {
            throw new IllegalArgumentException("4 bytes for ldc.r4 expected, received: " + h);
        }

        int b0 = Integer.parseInt(h.substring(0, 2), 16);
        int b1 = Integer.parseInt(h.substring(2, 4), 16);
        int b2 = Integer.parseInt(h.substring(4, 6), 16);
        int b3 = Integer.parseInt(h.substring(6, 8), 16);

        // .NET IL little endian
        int bits = (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;

        return Float.intBitsToFloat(bits);
    }

}