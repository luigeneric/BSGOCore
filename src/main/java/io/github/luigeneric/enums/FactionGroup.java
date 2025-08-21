package io.github.luigeneric.enums;


public enum FactionGroup
{
    Group0(0x00000000L),
    Group1(0x20000000L);

    public static final int SIZE = Integer.SIZE;

    public final long mask;

    FactionGroup(final long l)
    {
        this.mask = l;
    }


    public static FactionGroup extractFactionGroup(final long spaceObjectID)
    {
        final long num = spaceObjectID & 0x20000000L;
        if (num == 0)
        {
            return Group0;
        }
        return Group1;
    }
}

