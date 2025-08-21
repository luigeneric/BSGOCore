package io.github.luigeneric.enums;

public enum RankingType
{
    Regular((short) 0),
    Delta((short) 1);

    public final short value;

    RankingType(short value)
    {
        this.value = value;
    }


    public static RankingType forValue(final short value)
    {
        switch (value)
        {
            case 0 ->
            {
                return Regular;
            }
            case 1 ->
            {
                return Delta;
            }
            default ->
            {
                return null;
            }
        }
    }
}

