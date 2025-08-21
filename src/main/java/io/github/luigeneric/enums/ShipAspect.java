package io.github.luigeneric.enums;

public enum ShipAspect
{
    /**
     * CAMS
     */
    Dogfight,
    /**
     * Stealth bsssssst
     */
    Stealth,
    /**
     * Carrier dock button
     */
    Dock,
    /**
     * This one makes the ship have percentage HP/PP for others
     */
    StatsScrambler,
    /**
     * ????
     */
    TransponderJump;

    public final byte value;

    ShipAspect()
    {
        this.value = (byte) this.ordinal();
    }
}
