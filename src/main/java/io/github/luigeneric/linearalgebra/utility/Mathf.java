package io.github.luigeneric.linearalgebra.utility;

import java.util.Arrays;

public class Mathf
{
    public static final float radToDeg = 57.29578f;
    public static final float degToRad = 0.017453292f;
    public static final float piDiv2 = 1.5707964f;
    public static final float PI = 3.1415927f;

    public static final float EPSILON = ((MathfInternal.IsFlushToZeroEnabled) ? MathfInternal.FloatMinNormal : MathfInternal.FloatMinDenormal);

    public static float normalizeAngle(float angle)
    {
        while (angle <= -180f)
        {
            angle += 360f;
        }
        while (angle > 180f)
        {
            angle -= 360f;
        }
        return angle;
    }

    /**
     * Unsafe UnityEngine clamp call
     * @implNote Used from UnityEngine to stay as close as possible to the client calculates even if it results into errors...<br>
     * u know like if the client did some errors using this method wrongly, this could actually cause issues if I fix the usage to safe method.
     * @param value value to clamp between
     * @param min min <= max
     * @param max max >= min
     * @return a value inside [min, max] intervall as long as min/max fulfills it's contract
     */
    public static float clamp(final float value, final float min, final float max)
    {
        if (value < min)
        {
            return min;
        }
        else if (value > max)
        {
            return max;
        }

        return value;
    }



    /**
     * Safe to call even if min is greater than max, unlike UnityEngine implementation
     * @param value value to clamp between
     * @param min min might be higher than max
     * @param max might be less than min
     * @return a value between min and max!
     */
    public static float clampSafe(final float value, final float min, final float max)
    {
        if (min > max)
        {
            return clamp(value, max, min);
        }
        return clamp(value, min, max);
    }

    public static long clampSafe(final long value, final long min, final long max)
    {
        if (min > max)
            return clampSafe(value, max, min);

        if (value < min)
        {
            return min;
        }
        else if (value > max)
        {
            return max;
        }

        return value;
    }
    public static int clampSafe(final int value, final int min, final int max)
    {
        if (min > max)
            return clampSafe(value, max, min);

        if (value < min)
        {
            return min;
        }
        else if (value > max)
        {
            return max;
        }

        return value;
    }

    public static float round(final float value, final int decimals)
    {
        final float decimalFactor = Mathf.pow(10, decimals);
        return Math.round(value * decimalFactor) / decimalFactor;
    }

    /**
     * Linear interpolation between from and to.
     *
     * @param from start value of interpolation
     * @param to   end value of interpolation
     * @param t    interpolation param [0, 1]
     * @return interpolated value
     */
    public static float lerp(final float from, final float to, final float t)
    {
        return from + (to - from) * clamp01(t);
    }

    public static float clamp01(final float value)
    {
        return clamp(value, 0f, 1f);
    }

    /**
     * Same as using clamp(value, -1f, 1f);
     * @param value value to clamp between
     * @return clamped value
     */
    public static float clampMin11(final float value)
    {
        return clamp(value, -1f, 1f);
    }

    public static float abs(final float value)
    {
        return Math.abs(value);
    }

    public static float acos(final float value)
    {
        return (float) Math.acos(value);
    }


    public static float min(final float a, final float b)
    {
        return Math.min(a, b);
    }

    public static float log(final float base, final float num)
    {
        return (float) (Math.log10(num) / Math.log(base));
    }

    public static float min(final float a, final float b, final float c)
    {
        return Math.min(Math.min(a, b), c);
    }
    public static float min(final float first, final float... fs)
    {
        float min = first;

        for (final float f : fs)
        {
            min = Math.min(min, f);
        }

        return min;
    }
    public static float ceil(final float value)
    {
        return (float) Math.ceil(value);
    }

    public static float max(final float first, final float second, final float third)
    {
        return Mathf.max(Mathf.max(first, second), third);
    }
    public static float max(final float a, final float b)
    {
        return Math.max(a, b);
    }

    public static float sqrt(final double v)
    {
        return (float) Math.sqrt(v);
    }
    public static float sqrt(final float v)
    {
        return (float) Math.sqrt(v);
    }

    public static float asin(final float v)
    {
        return (float) Math.asin(v);
    }

    public static float atan2(final float x, final float y)
    {
        return (float) Math.atan2(x, y);
    }

    public static float pow(final float a, final float b)
    {
        return (float) Math.pow(a, b);
    }

    public static double avg(final double... avg)
    {
        if (avg == null)
            throw new NullPointerException("avg is null");

        final double sum = Arrays.stream(avg).sum();
        return sum / (double) avg.length;
    }
    public static float avg(final float... avg)
    {
        if (avg == null)
            throw new NullPointerException("avg is null");

        float sum = 0;
        for (float v : avg)
        {
            sum += v;
        }
        return sum / (float) avg.length;
    }

    public static boolean isInsideValues(final float num, final float min, final float max)
    {
        if (min > max)
            return isInsideValues(num, max, min);
        return min <= num && num <= max;
    }

    public static float sin(final float radians)
    {
        return (float) Math.sin(radians);
    }
    public static float sin(final double radians)
    {
        return (float) Math.sin(radians);
    }
    public static float cos(final float radians)
    {
        return (float) Math.cos(radians);
    }
    public static float cos(final double radians)
    {
        return (float) Math.cos(radians);
    }

    @SuppressWarnings("unused")
    public static boolean approxEqual(final float a, final float b, final float epsilon)
    {
        final float difference = a - b;
        return (difference < epsilon && difference > -epsilon);
    }
}
