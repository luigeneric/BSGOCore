package io.github.luigeneric.utils;

public class FreeUint16Counter
{
    private int uShortId;

    public final int MIN_VALUE;
    public final int MAX_VALUE;


    public FreeUint16Counter(final int initial)
    {
        this.uShortId = initial;
        this.MIN_VALUE = 0;
        this.MAX_VALUE = Short.MAX_VALUE * 2 + 1;
    }

    public FreeUint16Counter()
    {
        this(0);
    }

    public int getFreeId()
    {
        int newValue = this.uShortId++;
        if (newValue >= MAX_VALUE)
        {
            newValue = MIN_VALUE;
        }
        return newValue;
    }
}
