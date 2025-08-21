package io.github.luigeneric.templates.utils;

public enum TournamentType
{
    Strike(1),
    Escort(2),
    Liner(3);

    public final byte value;

    TournamentType(int value)
    {
        this.value = (byte) value;
    }
}
